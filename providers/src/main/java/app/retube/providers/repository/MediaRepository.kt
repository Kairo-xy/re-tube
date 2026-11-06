package app.retube.providers.repository

import app.retube.core.database.ReTubeDatabase
import app.retube.core.model.ChannelDetail
import app.retube.core.model.CommentItem
import app.retube.core.model.SearchFilter
import app.retube.core.model.SearchResult
import app.retube.core.model.SponsorSegment
import app.retube.core.model.VideoItem
import app.retube.providers.fallback.OpenMediaCatalog
import app.retube.providers.piped.PipedApiClient
import app.retube.providers.ryd.ReturnYouTubeDislikeClient
import app.retube.providers.sponsorblock.SponsorBlockClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class FeedResponse(
    val videos: List<VideoItem>,
    val isLiveInstance: Boolean,
    val activeSourceLabel: String
)

data class ResolvedVideoBundle(
    val video: VideoItem,
    val sponsorSegments: List<SponsorSegment>,
    val comments: List<CommentItem>
)

class MediaRepository(
    private val pipedClient: PipedApiClient = PipedApiClient(),
    private val sponsorBlockClient: SponsorBlockClient = SponsorBlockClient(),
    private val rydClient: ReturnYouTubeDislikeClient = ReturnYouTubeDislikeClient(),
    private val database: ReTubeDatabase
) {

    suspend fun getHomeFeed(
        instanceUrl: String,
        region: String,
        category: String = "All"
    ): FeedResponse {
        val remoteTrending = pipedClient.fetchTrending(instanceUrl, region)
        val combined = if (remoteTrending.isNotEmpty()) {
            // Prepend OpenMediaCatalog items when category is "Blender & Open Movies" so open movies are always accessible
            if (category == "Blender & Open Movies") {
                OpenMediaCatalog.curatedVideos + remoteTrending
            } else {
                remoteTrending
            }
        } else {
            OpenMediaCatalog.curatedVideos
        }

        val filtered = if (category == "All" || remoteTrending.isNotEmpty() && category != "Blender & Open Movies") {
            combined
        } else {
            combined.filter { it.category.equals(category, ignoreCase = true) }.ifEmpty { combined }
        }

        return FeedResponse(
            videos = filtered,
            isLiveInstance = remoteTrending.isNotEmpty(),
            activeSourceLabel = if (remoteTrending.isNotEmpty()) {
                "Live Piped ($region)"
            } else {
                "Built-in Open Media Catalog"
            }
        )
    }

    suspend fun searchMedia(
        instanceUrl: String,
        query: String,
        filter: SearchFilter
    ): SearchResult {
        val remote = pipedClient.search(instanceUrl, query, filter)
        if (remote.videos.isNotEmpty() || remote.channels.isNotEmpty() || remote.playlists.isNotEmpty()) {
            return remote
        }
        val q = query.trim().lowercase()
        val localMatches = OpenMediaCatalog.curatedVideos.filter {
            it.title.lowercase().contains(q) ||
                it.uploaderName.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
        }
        return SearchResult(
            videos = localMatches.ifEmpty { OpenMediaCatalog.curatedVideos },
            channels = listOf(OpenMediaCatalog.getFallbackChannel("UCB0W8N8V0B-blender")),
            playlists = OpenMediaCatalog.getFallbackPlaylists()
        )
    }

    suspend fun getSuggestions(instanceUrl: String, query: String): List<String> {
        val remote = pipedClient.fetchSuggestions(instanceUrl, query)
        if (remote.isNotEmpty()) return remote
        val presets = listOf(
            "Big Buck Bunny 4K",
            "Blender Studio Open Movie",
            "Sintel Fantasy Short",
            "Tears of Steel Sci-Fi",
            "Linux & Open Source Android",
            "NASA UHD Space"
        )
        return presets.filter { it.contains(query.trim(), ignoreCase = true) }
    }

    suspend fun resolveVideoBundle(
        instanceUrl: String,
        videoId: String,
        sponsorBlockEnabled: Boolean,
        sponsorCategories: Set<String>,
        rydEnabled: Boolean
    ): ResolvedVideoBundle = coroutineScope {
        val videoDeferred = async {
            val catalogHit = OpenMediaCatalog.curatedVideos.firstOrNull { it.id == videoId }
            if (catalogHit != null) {
                OpenMediaCatalog.getFallbackVideoById(videoId)
            } else {
                pipedClient.fetchVideoDetails(instanceUrl, videoId)
                    ?: OpenMediaCatalog.getFallbackVideoById(videoId)
            }
        }

        val sponsorDeferred = async {
            if (!sponsorBlockEnabled) {
                emptyList()
            } else {
                val remoteSegments = sponsorBlockClient.fetchSegments(videoId, sponsorCategories)
                remoteSegments.ifEmpty {
                    OpenMediaCatalog.getFallbackSponsorSegments(videoId).filter {
                        sponsorCategories.contains(it.category.apiKey)
                    }
                }
            }
        }

        val rydDeferred = async {
            if (rydEnabled) rydClient.fetchVotes(videoId) else null
        }

        val commentsDeferred = async {
            val remoteComments = pipedClient.fetchComments(instanceUrl, videoId)
            remoteComments.ifEmpty { OpenMediaCatalog.getFallbackComments(videoId) }
        }

        val baseVideo = videoDeferred.await()
        val rydVotes = rydDeferred.await()
        val enrichedVideo = if (rydVotes != null && rydVotes.dislikes > 0L) {
            baseVideo.copy(
                dislikeCount = rydVotes.dislikes,
                likeCount = if (baseVideo.likeCount > 0L) baseVideo.likeCount else rydVotes.likes
            )
        } else {
            baseVideo
        }

        ResolvedVideoBundle(
            video = enrichedVideo,
            sponsorSegments = sponsorDeferred.await(),
            comments = commentsDeferred.await()
        )
    }

    suspend fun getChannelDetails(instanceUrl: String, channelId: String): ChannelDetail {
        return pipedClient.fetchChannel(instanceUrl, channelId)
            ?: OpenMediaCatalog.getFallbackChannel(channelId)
    }

    suspend fun getSubscriptionsFeed(instanceUrl: String): List<VideoItem> {
        val subs = database.subscriptionDao().getAllSubscriptionsList()
        if (subs.isEmpty()) return emptyList()
        val feed = mutableListOf<VideoItem>()
        for (sub in subs.take(8)) {
            val channel = getChannelDetails(instanceUrl, sub.channelId)
            feed.addAll(channel.videos.take(5))
        }
        return feed.distinctBy { it.id }
    }
}