package app.retube.downloads

import android.content.Context
import android.os.Environment
import app.retube.core.database.DownloadDao
import app.retube.core.database.DownloadStatus
import app.retube.core.database.DownloadTaskEntity
import app.retube.core.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadManagerController(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val downloadsFlow: Flow<List<DownloadTaskEntity>> = downloadDao.observeDownloads()

    fun enqueueDownload(
        video: VideoItem,
        qualityLabel: String,
        isAudioOnly: Boolean
    ) {
        scope.launch {
            val streamUrl = if (isAudioOnly) {
                video.audioStreams.firstOrNull()?.url ?: video.bestPlayableVideoUrl(qualityLabel)
            } else {
                video.bestPlayableVideoUrl(qualityLabel)
            } ?: return@launch

            val ext = if (isAudioOnly) "m4a" else "mp4"
            val sanitizedId = video.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val targetDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                "ReTubeDownloads"
            ).apply { mkdirs() }
            val targetFile = File(targetDir, "${sanitizedId}_${qualityLabel.replace(" ", "")}.$ext")

            val entity = DownloadTaskEntity(
                videoId = video.id,
                title = video.title,
                uploaderName = video.uploaderName,
                thumbnailUrl = video.thumbnailUrl,
                durationSeconds = video.durationSeconds,
                streamUrl = streamUrl,
                qualityLabel = if (isAudioOnly) "Audio ($qualityLabel)" else qualityLabel,
                isAudioOnly = isAudioOnly,
                filePath = targetFile.absolutePath,
                totalBytes = 0L,
                downloadedBytes = if (targetFile.exists()) targetFile.length() else 0L,
                status = DownloadStatus.QUEUED.name,
                createdAt = System.currentTimeMillis()
            )
            downloadDao.upsertDownload(entity)
            startDownloadJob(entity)
        }
    }

    fun pauseDownload(videoId: String) {
        activeJobs.remove(videoId)?.cancel()
        scope.launch {
            downloadDao.updateStatus(videoId, DownloadStatus.PAUSED.name)
        }
    }

    fun resumeDownload(videoId: String) {
        scope.launch {
            val task = downloadDao.getDownloadById(videoId) ?: return@launch
            startDownloadJob(task)
        }
    }

    fun deleteDownload(videoId: String) {
        activeJobs.remove(videoId)?.cancel()
        scope.launch {
            val task = downloadDao.getDownloadById(videoId)
            if (task != null) {
                runCatching { File(task.filePath).delete() }
            }
            downloadDao.deleteDownload(videoId)
        }
    }

    suspend fun getCompletedLocalFilePath(videoId: String): String? {
        val task = downloadDao.getDownloadById(videoId) ?: return null
        if (task.status == DownloadStatus.COMPLETED.name) {
            val file = File(task.filePath)
            if (file.exists() && file.length() > 0L) {
                return file.absolutePath
            }
        }
        return null
    }

    private fun startDownloadJob(task: DownloadTaskEntity) {
        activeJobs.remove(task.videoId)?.cancel()
        val job = scope.launch {
            try {
                val destFile = File(task.filePath)
                destFile.parentFile?.mkdirs()
                val existingBytes = if (destFile.exists()) destFile.length() else 0L

                downloadDao.updateStatus(task.videoId, DownloadStatus.DOWNLOADING.name)

                val reqBuilder = Request.Builder()
                    .url(task.streamUrl)
                    .header("User-Agent", "ReTube-Android/1.0")
                if (existingBytes > 0L) {
                    reqBuilder.header("Range", "bytes=$existingBytes-")
                }

                httpClient.newCall(reqBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful && response.code != 206) {
                        downloadDao.updateStatus(
                            task.videoId,
                            DownloadStatus.FAILED.name,
                            "HTTP ${response.code}"
                        )
                        return@launch
                    }

                    val body = response.body ?: run {
                        downloadDao.updateStatus(task.videoId, DownloadStatus.FAILED.name, "Empty body")
                        return@launch
                    }

                    val isResumed = response.code == 206
                    val contentLength = body.contentLength().coerceAtLeast(0L)
                    val totalBytes = if (isResumed) existingBytes + contentLength else contentLength
                    var downloaded = if (isResumed) existingBytes else 0L

                    FileOutputStream(destFile, isResumed).use { output ->
                        body.byteStream().use { input ->
                            val buffer = ByteArray(32 * 1024)
                            var bytesRead: Int
                            var lastUpdateMs = 0L
                            while (isActive && input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloaded += bytesRead
                                val now = System.currentTimeMillis()
                                if (now - lastUpdateMs > 400L) {
                                    lastUpdateMs = now
                                    downloadDao.updateProgress(
                                        videoId = task.videoId,
                                        downloadedBytes = downloaded,
                                        totalBytes = totalBytes.coerceAtLeast(downloaded),
                                        status = DownloadStatus.DOWNLOADING.name
                                    )
                                }
                            }
                            output.flush()
                        }
                    }

                    if (isActive) {
                        downloadDao.updateProgress(
                            videoId = task.videoId,
                            downloadedBytes = downloaded,
                            totalBytes = totalBytes.coerceAtLeast(downloaded),
                            status = DownloadStatus.COMPLETED.name
                        )
                    }
                }
            } catch (e: Exception) {
                downloadDao.updateStatus(
                    task.videoId,
                    DownloadStatus.FAILED.name,
                    e.localizedMessage ?: "Download interrupted"
                )
            } finally {
                activeJobs.remove(task.videoId)
            }
        }
        activeJobs[task.videoId] = job
    }
}