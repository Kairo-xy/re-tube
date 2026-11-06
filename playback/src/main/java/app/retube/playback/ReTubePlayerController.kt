package app.retube.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import app.retube.core.database.WatchHistoryDao
import app.retube.core.database.WatchHistoryEntity
import app.retube.core.model.CommentItem
import app.retube.core.model.SponsorSegment
import app.retube.core.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class ReTubePlayerController(
    context: Context,
    private val watchHistoryDao: WatchHistoryDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressPollingJob: Job? = null
    private var sleepTimerJob: Job? = null
    private val skippedSegmentIds = mutableSetOf<String>()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressPolling()
                } else {
                    persistCurrentWatchProgress()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val buffering = playbackState == Player.STATE_BUFFERING
                val duration = exoPlayer.duration.coerceAtLeast(0L)
                _uiState.update {
                    it.copy(
                        isBuffering = buffering,
                        durationMs = if (duration > 0L) duration else (it.currentVideo?.durationSeconds ?: 0L) * 1000L
                    )
                }
                if (playbackState == Player.STATE_ENDED) {
                    playNextInQueueOrRelated()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _uiState.update {
                    it.copy(errorMessage = error.localizedMessage ?: "Playback error occurred")
                }
            }
        })
    }

    fun loadAndPlayVideo(
        video: VideoItem,
        sponsorSegments: List<SponsorSegment>,
        comments: List<CommentItem>,
        preferredQuality: String = "1080p",
        defaultSpeed: Float = 1.0f,
        localFilePath: String? = null,
        resumePositionMs: Long = 0L,
        isHistoryPaused: Boolean = false
    ) {
        skippedSegmentIds.clear()
        val qualities = video.videoStreams
            .filter { !it.isVideoOnly }
            .map { it.qualityLabel }
            .distinct()
            .ifEmpty { listOf("1080p", "720p", "480p") }

        val chosenQuality = if (qualities.contains(preferredQuality)) preferredQuality else qualities.first()
        val mediaUri = resolveMediaUri(video, chosenQuality, _uiState.value.isAudioOnlyMode, localFilePath)

        val currentQueue = _uiState.value.queue.toMutableList()
        val queueIndex = currentQueue.indexOfFirst { it.id == video.id }.let { idx ->
            if (idx >= 0) idx else {
                currentQueue.add(0, video)
                video.relatedVideos.take(6).forEach { rel ->
                    if (currentQueue.none { it.id == rel.id }) currentQueue.add(rel)
                }
                0
            }
        }

        _uiState.update {
            it.copy(
                currentVideo = video,
                sponsorSegments = sponsorSegments,
                comments = comments,
                selectedQualityLabel = chosenQuality,
                availableQualities = qualities,
                playbackSpeed = defaultSpeed,
                queue = currentQueue,
                currentQueueIndex = queueIndex,
                isMinimized = false,
                errorMessage = null,
                lastSkippedSponsorNotice = null
            )
        }

        if (mediaUri != null) {
            val metadata = MediaMetadata.Builder()
                .setTitle(video.title)
                .setArtist(video.uploaderName)
                .setArtworkUri(Uri.parse(video.thumbnailUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaId(video.id)
                .setUri(mediaUri)
                .setMediaMetadata(metadata)
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playbackParameters = PlaybackParameters(defaultSpeed)
            exoPlayer.prepare()
            if (resumePositionMs > 2_000L && resumePositionMs < (video.durationSeconds * 950L)) {
                exoPlayer.seekTo(resumePositionMs)
            }
            exoPlayer.playWhenReady = true
        }

        if (!isHistoryPaused) {
            scope.launch(Dispatchers.IO) {
                watchHistoryDao.upsert(
                    WatchHistoryEntity(
                        videoId = video.id,
                        title = video.title,
                        uploaderName = video.uploaderName,
                        uploaderId = video.uploaderId,
                        thumbnailUrl = video.thumbnailUrl,
                        durationSeconds = video.durationSeconds,
                        watchedPositionMs = resumePositionMs,
                        watchedAtTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun resolveMediaUri(
        video: VideoItem,
        qualityLabel: String,
        audioOnly: Boolean,
        localFilePath: String?
    ): Uri? {
        if (!localFilePath.isNullOrBlank()) {
            val file = File(localFilePath)
            if (file.exists() && file.length() > 0L) {
                return Uri.fromFile(file)
            }
        }
        if (audioOnly) {
            val audioUrl = video.audioStreams.firstOrNull()?.url ?: video.bestPlayableVideoUrl(qualityLabel)
            return audioUrl?.let { Uri.parse(it) }
        }
        val streamUrl = video.bestPlayableVideoUrl(qualityLabel)
        return streamUrl?.let { Uri.parse(it) }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceAtLeast(0L)
        exoPlayer.seekTo(target)
        _uiState.update { it.copy(currentPositionMs = target) }
    }

    fun seekRelative(deltaMs: Long) {
        val target = (exoPlayer.currentPosition + deltaMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        exoPlayer.playbackParameters = PlaybackParameters(clamped)
        _uiState.update { it.copy(playbackSpeed = clamped) }
    }

    fun changeQuality(qualityLabel: String) {
        val video = _uiState.value.currentVideo ?: return
        val currentPos = exoPlayer.currentPosition
        val wasPlaying = exoPlayer.isPlaying
        val uri = resolveMediaUri(video, qualityLabel, _uiState.value.isAudioOnlyMode, null) ?: return
        val metadata = exoPlayer.mediaMetadata
        val item = MediaItem.Builder()
            .setMediaId(video.id)
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
        exoPlayer.setMediaItem(item, currentPos)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = wasPlaying
        _uiState.update { it.copy(selectedQualityLabel = qualityLabel) }
    }

    fun toggleAudioOnlyMode() {
        val nextAudioOnly = !_uiState.value.isAudioOnlyMode
        _uiState.update { it.copy(isAudioOnlyMode = nextAudioOnly) }
        changeQuality(_uiState.value.selectedQualityLabel)
    }

    fun setMinimized(minimized: Boolean) {
        _uiState.update { it.copy(isMinimized = minimized) }
    }

    fun closePlayer() {
        persistCurrentWatchProgress()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _uiState.update { PlayerUiState() }
    }

    fun addToQueue(video: VideoItem) {
        _uiState.update { state ->
            if (state.queue.any { it.id == video.id }) state
            else state.copy(queue = state.queue + video)
        }
    }

    fun playNextInQueueOrRelated(onSelectVideo: ((VideoItem) -> Unit)? = null) {
        val state = _uiState.value
        val nextIndex = state.currentQueueIndex + 1
        val nextVideo = state.queue.getOrNull(nextIndex) ?: state.currentVideo?.relatedVideos?.firstOrNull()
        if (nextVideo != null) {
            if (onSelectVideo != null) {
                onSelectVideo(nextVideo)
            } else {
                loadAndPlayVideo(
                    video = nextVideo,
                    sponsorSegments = emptyList(),
                    comments = emptyList(),
                    preferredQuality = state.selectedQualityLabel,
                    defaultSpeed = state.playbackSpeed
                )
            }
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _uiState.update { it.copy(sleepTimerRemainingMs = 0L) }
            return
        }
        val totalMs = minutes * 60_000L
        _uiState.update { it.copy(sleepTimerRemainingMs = totalMs) }
        sleepTimerJob = scope.launch {
            var remaining = totalMs
            while (isActive && remaining > 0L) {
                delay(1_000L)
                remaining -= 1_000L
                _uiState.update { it.copy(sleepTimerRemainingMs = remaining) }
            }
            if (remaining <= 0L) {
                exoPlayer.pause()
                _uiState.update { it.copy(sleepTimerRemainingMs = 0L) }
            }
        }
    }

    fun clearSponsorNotice() {
        _uiState.update { it.copy(lastSkippedSponsorNotice = null) }
    }

    private fun startProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = scope.launch {
            var tickCounter = 0
            while (isActive && exoPlayer.isPlaying) {
                val posMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                val durMs = exoPlayer.duration.coerceAtLeast(
                    (_uiState.value.currentVideo?.durationSeconds ?: 0L) * 1000L
                )
                val buffered = exoPlayer.bufferedPercentage

                _uiState.update {
                    it.copy(
                        currentPositionMs = posMs,
                        durationMs = durMs,
                        bufferedPercentage = buffered
                    )
                }

                checkAndSkipSponsorSegment(posMs)

                tickCounter++
                if (tickCounter % 10 == 0) {
                    persistCurrentWatchProgress()
                }
                delay(500L)
            }
        }
    }

    private fun checkAndSkipSponsorSegment(positionMs: Long) {
        val posSec = positionMs / 1000f
        val segments = _uiState.value.sponsorSegments
        for (segment in segments) {
            if (posSec >= segment.startTimeSeconds &&
                posSec < (segment.endTimeSeconds - 0.5f) &&
                !skippedSegmentIds.contains(segment.uuid)
            ) {
                skippedSegmentIds.add(segment.uuid)
                val targetMs = (segment.endTimeSeconds * 1000L).toLong()
                exoPlayer.seekTo(targetMs)
                _uiState.update {
                    it.copy(
                        currentPositionMs = targetMs,
                        lastSkippedSponsorNotice = "Skipped ${segment.category.displayName}"
                    )
                }
                break
            }
        }
    }

    private fun persistCurrentWatchProgress() {
        val video = _uiState.value.currentVideo ?: return
        val posMs = exoPlayer.currentPosition.coerceAtLeast(0L)
        scope.launch(Dispatchers.IO) {
            val existing = watchHistoryDao.getByVideoId(video.id)
            if (existing != null) {
                watchHistoryDao.upsert(
                    existing.copy(
                        watchedPositionMs = posMs,
                        watchedAtTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun release() {
        progressPollingJob?.cancel()
        sleepTimerJob?.cancel()
        exoPlayer.release()
    }
}