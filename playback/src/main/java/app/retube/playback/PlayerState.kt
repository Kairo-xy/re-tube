package app.retube.playback

import app.retube.core.model.CommentItem
import app.retube.core.model.SponsorSegment
import app.retube.core.model.VideoItem

data class PlayerUiState(
    val currentVideo: VideoItem? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPercentage: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: Int = 0,
    val isAudioOnlyMode: Boolean = false,
    val isFullscreen: Boolean = false,
    val isMinimized: Boolean = false,
    val selectedQualityLabel: String = "1080p",
    val availableQualities: List<String> = listOf("1080p", "720p", "480p"),
    val sponsorSegments: List<SponsorSegment> = emptyList(),
    val comments: List<CommentItem> = emptyList(),
    val lastSkippedSponsorNotice: String? = null,
    val queue: List<VideoItem> = emptyList(),
    val currentQueueIndex: Int = 0,
    val sleepTimerRemainingMs: Long = 0L,
    val errorMessage: String? = null
)