package app.retube.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAtTimestamp DESC")
    fun observeAll(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE title LIKE '%' || :query || '%' OR uploaderName LIKE '%' || :query || '%' ORDER BY watchedAtTimestamp DESC")
    fun searchHistory(query: String): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun getByVideoId(videoId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM local_playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<LocalPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: LocalPlaylistEntity): Long

    @Query("DELETE FROM local_playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId")
    suspend fun clearPlaylistVideos(playlistId: Long)

    @Query("SELECT * FROM playlist_videos WHERE playlistId = :playlistId ORDER BY positionIndex ASC, addedAt DESC")
    fun observePlaylistVideos(playlistId: Long): Flow<List<PlaylistVideoCrossRef>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVideoToPlaylist(crossRef: PlaylistVideoCrossRef)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: String)

    @Query("SELECT COUNT(*) FROM playlist_videos WHERE playlistId = :playlistId")
    suspend fun getVideoCount(playlistId: Long): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE videoId = :videoId)")
    fun observeIsBookmarked(videoId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE videoId = :videoId")
    suspend fun removeBookmark(videoId: String)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY channelName ASC")
    fun observeSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    suspend fun getAllSubscriptionsList(): List<SubscriptionEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelId = :channelId)")
    fun observeIsSubscribed(channelId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun subscribe(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE channelId = :channelId")
    suspend fun unsubscribe(channelId: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeDownloads(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM downloads WHERE videoId = :videoId LIMIT 1")
    suspend fun getDownloadById(videoId: String): DownloadTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(task: DownloadTaskEntity)

    @Query("UPDATE downloads SET downloadedBytes = :downloadedBytes, totalBytes = :totalBytes, status = :status WHERE videoId = :videoId")
    suspend fun updateProgress(videoId: String, downloadedBytes: Long, totalBytes: Long, status: String)

    @Query("UPDATE downloads SET status = :status, errorMessage = :errorMessage WHERE videoId = :videoId")
    suspend fun updateStatus(videoId: String, status: String, errorMessage: String? = null)

    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun deleteDownload(videoId: String)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 25")
    fun observeRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuery(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}