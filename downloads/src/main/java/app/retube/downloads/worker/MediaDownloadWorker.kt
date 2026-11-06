package app.retube.downloads.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.retube.core.database.DownloadStatus
import app.retube.core.database.ReTubeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class MediaDownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoId = inputData.getString("videoId") ?: return@withContext Result.failure()
        val dao = ReTubeDatabase.getInstance(applicationContext).downloadDao()
        val task = dao.getDownloadById(videoId) ?: return@withContext Result.failure()

        try {
            val file = File(task.filePath)
            file.parentFile?.mkdirs()
            dao.updateStatus(videoId, DownloadStatus.DOWNLOADING.name)

            val client = OkHttpClient()
            val request = Request.Builder().url(task.streamUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    dao.updateStatus(videoId, DownloadStatus.FAILED.name, "HTTP ${response.code}")
                    return@withContext Result.retry()
                }
                val body = response.body ?: return@withContext Result.failure()
                val total = body.contentLength().coerceAtLeast(0L)
                var downloaded = 0L

                FileOutputStream(file, false).use { out ->
                    body.byteStream().use { input ->
                        val buf = ByteArray(16 * 1024)
                        var read: Int
                        while (input.read(buf).also { read = it } != -1) {
                            out.write(buf, 0, read)
                            downloaded += read
                        }
                    }
                }
                dao.updateProgress(videoId, downloaded, total.coerceAtLeast(downloaded), DownloadStatus.COMPLETED.name)
            }
            Result.success()
        } catch (e: Exception) {
            dao.updateStatus(videoId, DownloadStatus.FAILED.name, e.localizedMessage)
            Result.failure()
        }
    }
}