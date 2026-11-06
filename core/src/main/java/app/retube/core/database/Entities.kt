package app.retube.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploaderName: String,
    val uploaderId: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val watchedPositionMs: Long,
    val watchedAtTimestamp: Long
)

@Serializable
@Entity(tableName = "local_playlists")
data class LocalPlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0L,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val coverThumbnailUrl: String = ""
)

@Serializable
@Entity(
    tableName = "playlist_videos",
    primaryKeys = ["playlistId", "videoId"]
)
data class PlaylistVideoCrossRef(
    val playlistId: Long,
    val videoId: String,
    val positionIndex: Int,
    val addedAt: Long,
    val title: String,
    val uploaderName: String,
    val uploaderId: String,
    val thumbnailUrl: String,
    val durationSeconds: Long
)

@Serializable
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploaderName: String,
    val uploaderId: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val viewCount: Long,
    val bookmarkedAt: Long
)

@Serializable
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelId: String,
    val channelName: String,
    val avatarUrl: String,
    val subscriberCount: Long,
    val subscribedAt: Long = System.currentTimeMillis(),
    val notificationsEnabled: Boolean = true
)

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

@Serializable
@Entity(tableName = "downloads")
data class DownloadTaskEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val uploaderName: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val streamUrl: String,
    val qualityLabel: String,
    val isAudioOnly: Boolean,
    val filePath: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: String,
    val createdAt: Long,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (totalBytes > 0L) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

@Serializable
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)