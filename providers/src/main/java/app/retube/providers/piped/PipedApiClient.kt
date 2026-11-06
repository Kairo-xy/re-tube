package app.retube.providers.piped

import app.retube.core.model.AudioStream
import app.retube.core.model.ChannelDetail
import app.retube.core.model.CommentItem
import app.retube.core.model.PlaylistSummary
import app.retube.core.model.SearchFilter
import app.retube.core.model.SearchResult
import app.retube.core.model.SubtitleTrack
import app.retube.core.model.VideoChapter
import app.retube.core.model.VideoItem
import app.retube.core.model.VideoStream
import app.retube.core.util.Formatters
import app.retube.providers.fallback.OpenMediaCatalog
import app.retube.providers.invidious.InvidiousFallbackParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class PipedApiClient(
    private val httpClient: OkHttpClient = defaultHttpClient()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    companion object {
        fun defaultHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .followRedirects(true)
                .build()
        }
    }

    suspend fun fetchTrending(primaryInstanceUrl: String, region: String = "US"): List<VideoItem> =
        withContext(Dispatchers.IO) {
            val candidates = buildInstanceCandidateList(primaryInstanceUrl)
            for (baseUrl in candidates) {
                try {
                    val isInvidious = baseUrl.contains("yewtu.be") || baseUrl.contains("nadeko") || baseUrl.contains("/api/v1")
                    val endpoint = if (isInvidious) {
                        "${baseUrl.removeSuffix("/").removeSuffix("/api/v1")}/api/v1/popular"
                    } else {
                        "${baseUrl.removeSuffix("/")}/trending?region=${URLEncoder.encode(region, "UTF-8")}"
                    }
                    val body = executeGet(endpoint) ?: continue
                    val parsed = if (isInvidious) {
                        InvidiousFallbackParser.parseVideoList(json, body)
                    } else {
                        parseStreamItemList(json.parseToJsonElement(body).jsonArray)
                    }
                    if (parsed.isNotEmpty()) return@withContext parsed
                } catch (_: Exception) {
                    // Continue to next failover instance
                }
            }
            emptyList()
        }

    suspend fun fetchVideoDetails(primaryInstanceUrl: String, videoId: String): VideoItem? =
        withContext(Dispatchers.IO) {
            val cleanId = Formatters.extractYoutubeVideoId(videoId)
            val candidates = buildInstanceCandidateList(primaryInstanceUrl)
            for (baseUrl in candidates) {
                try {
                    val isInvidious = baseUrl.contains("yewtu.be") || baseUrl.contains("nadeko") || baseUrl.contains("/api/v1")
                    val endpoint = if (isInvidious) {
                        "${baseUrl.removeSuffix("/").removeSuffix("/api/v1")}/api/v1/videos/$cleanId"
                    } else {
                        "${baseUrl.removeSuffix("/")}/streams/$cleanId"
                    }
                    val body = executeGet(endpoint) ?: continue
                    val item = if (isInvidious) {
                        InvidiousFallbackParser.parseVideoDetail(json, cleanId, body)
                    } else {
                        parseVideoDetailJson(cleanId, json.parseToJsonElement(body).jsonObject)
                    }
                    if (item != null && (item.videoStreams.isNotEmpty() || !item.hlsUrl.isNullOrBlank())) {
                        return@withContext item
                    }
                } catch (_: Exception) {
                    // Failover to next instance
                }
            }
            null
        }

    suspend fun search(
        primaryInstanceUrl: String,
        query: String,
        filter: SearchFilter = SearchFilter.ALL
    ): SearchResult = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val candidates = buildInstanceCandidateList(primaryInstanceUrl)
        for (baseUrl in candidates) {
            try {
                val isInvidious = baseUrl.contains("yewtu.be") || baseUrl.contains("nadeko") || baseUrl.contains("/api/v1")
                val endpoint = if (isInvidious) {
                    "${baseUrl.removeSuffix("/").removeSuffix("/api/v1")}/api/v1/search?q=$encodedQuery"
                } else {
                    "${baseUrl.removeSuffix("/")}/search?q=$encodedQuery&filter=${filter.apiParam}"
                }
                val body = executeGet(endpoint) ?: continue
                if (isInvidious) {
                    val videos = InvidiousFallbackParser.parseVideoList(json, body)
                    if (videos.isNotEmpty()) return@withContext SearchResult(videos = videos)
                } else {
                    val root = json.parseToJsonElement(body).jsonObject
                    val items = root["items"]?.jsonArray ?: JsonArray(emptyList())
                    val videos = mutableListOf<VideoItem>()
                    val channels = mutableListOf<ChannelDetail>()
                    val playlists = mutableListOf<PlaylistSummary>()

                    for (el in items) {
                        val obj = el.jsonObject
                        when (obj["type"]?.jsonPrimitive?.content ?: "stream") {
                            "stream" -> parseStreamItem(obj)?.let { videos.add(it) }
                            "channel" -> parseChannelSummary(obj)?.let { channels.add(it) }
                            "playlist" -> parsePlaylistSummary(obj)?.let { playlists.add(it) }
                        }
                    }
                    if (videos.isNotEmpty() || channels.isNotEmpty() || playlists.isNotEmpty()) {
                        return@withContext SearchResult(
                            videos = videos,
                            channels = channels,
                            playlists = playlists,
                            nextpage = root["nextpage"]?.jsonPrimitive?.content
                        )
                    }
                }
            } catch (_: Exception) {
                // Next instance
            }
        }
        SearchResult()
    }

    suspend fun fetchSuggestions(primaryInstanceUrl: String, query: String): List<String> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val candidates = buildInstanceCandidateList(primaryInstanceUrl)
            for (baseUrl in candidates) {
                try {
                    if (baseUrl.contains("yewtu.be") || baseUrl.contains("nadeko")) continue
                    val endpoint = "${baseUrl.removeSuffix("/")}/suggestions?query=$encoded"
                    val body = executeGet(endpoint) ?: continue
                    val arr = json.parseToJsonElement(body).jsonArray
                    return@withContext arr.mapNotNull { it.jsonPrimitive.content }.take(8)
                } catch (_: Exception) {
                }
            }
            emptyList()
        }

    suspend fun fetchComments(primaryInstanceUrl: String, videoId: String): List<CommentItem> =
        withContext(Dispatchers.IO) {
            val cleanId = Formatters.extractYoutubeVideoId(videoId)
            val candidates = buildInstanceCandidateList(primaryInstanceUrl)
            for (baseUrl in candidates) {
                try {
                    if (baseUrl.contains("yewtu.be") || baseUrl.contains("nadeko")) continue
                    val endpoint = "${baseUrl.removeSuffix("/")}/comments/$cleanId"
                    val body = executeGet(endpoint) ?: continue
                    val root = json.parseToJsonElement(body).jsonObject
                    val commentsArr = root["comments"]?.jsonArray ?: continue
                    val result = commentsArr.mapNotNull { el ->
                        val obj = el.jsonObject
                        val id = obj["commentId"]?.jsonPrimitive?.content ?: return@mapNotNull null
                        CommentItem(
                            id = id,
                            authorName = obj["author"]?.jsonPrimitive?.content ?: "Anonymous",
                            authorAvatar = obj["thumbnail"]?.jsonPrimitive?.content ?: "",
                            authorId = obj["commentorUrl"]?.jsonPrimitive?.content?.removePrefix("/channel/") ?: "",
                            text = obj["commentText"]?.jsonPrimitive?.content ?: "",
                            publishedText = obj["commentedTime"]?.jsonPrimitive?.content ?: "",
                            likeCount = obj["likeCount"]?.jsonPrimitive?.longOrNull ?: 0L,
                            isPinned = obj["pinned"]?.jsonPrimitive?.booleanOrNull ?: false,
                            isHearted = obj["hearted"]?.jsonPrimitive?.booleanOrNull ?: false,
                            isVerified = obj["verified"]?.jsonPrimitive?.booleanOrNull ?: false,
                            replyCount = obj["replyCount"]?.jsonPrimitive?.intOrNull ?: 0,
                            repliesPage = obj["repliesPage"]?.jsonPrimitive?.content
                        )
                    }
                    if (result.isNotEmpty()) return@withContext result
                } catch (_: Exception) {
                }
            }
            emptyList()
        }

    suspend fun fetchChannel(primaryInstanceUrl: String, channelId: String): ChannelDetail? =
        withContext(Dispatchers.IO) {
            val cleanId = channelId.removePrefix("/channel/").trim()
            val candidates = buildInstanceCandidateList(primaryInstanceUrl)
            for (baseUrl in candidates) {
                try {
                    if (baseUrl.contains("yewtu.be") || baseUrl.contains("nadeko")) continue
                    val endpoint = "${baseUrl.removeSuffix("/")}/channel/$cleanId"
                    val body = executeGet(endpoint) ?: continue
                    val root = json.parseToJsonElement(body).jsonObject
                    val related = root["relatedStreams"]?.jsonArray ?: JsonArray(emptyList())
                    return@withContext ChannelDetail(
                        id = root["id"]?.jsonPrimitive?.content ?: cleanId,
                        name = root["name"]?.jsonPrimitive?.content ?: "Channel",
                        avatarUrl = root["avatarUrl"]?.jsonPrimitive?.content ?: "",
                        bannerUrl = root["bannerUrl"]?.jsonPrimitive?.content ?: "",
                        description = root["description"]?.jsonPrimitive?.content ?: "",
                        subscriberCount = root["subscriberCount"]?.jsonPrimitive?.longOrNull ?: 0L,
                        isVerified = root["verified"]?.jsonPrimitive?.booleanOrNull ?: false,
                        videos = parseStreamItemList(related),
                        nextpage = root["nextpage"]?.jsonPrimitive?.content
                    )
                } catch (_: Exception) {
                }
            }
            null
        }

    internal fun parseVideoDetailJson(videoId: String, root: JsonObject): VideoItem {
        val videoStreams = (root["videoStreams"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { el ->
            val obj = el.jsonObject
            val url = obj["url"]?.jsonPrimitive?.content ?: return@mapNotNull null
            VideoStream(
                url = url,
                format = obj["format"]?.jsonPrimitive?.content ?: "MPEG_4",
                qualityLabel = obj["quality"]?.jsonPrimitive?.content ?: "720p",
                width = obj["width"]?.jsonPrimitive?.intOrNull ?: 1280,
                height = obj["height"]?.jsonPrimitive?.intOrNull ?: 720,
                fps = obj["fps"]?.jsonPrimitive?.intOrNull ?: 30,
                bitrate = obj["bitrate"]?.jsonPrimitive?.intOrNull ?: 0,
                mimeType = obj["mimeType"]?.jsonPrimitive?.content ?: "video/mp4",
                isVideoOnly = obj["videoOnly"]?.jsonPrimitive?.booleanOrNull ?: false,
                codec = obj["codec"]?.jsonPrimitive?.content ?: "avc1"
            )
        }

        val audioStreams = (root["audioStreams"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { el ->
            val obj = el.jsonObject
            val url = obj["url"]?.jsonPrimitive?.content ?: return@mapNotNull null
            AudioStream(
                url = url,
                format = obj["format"]?.jsonPrimitive?.content ?: "M4A",
                qualityLabel = obj["quality"]?.jsonPrimitive?.content ?: "128 kbps",
                bitrate = obj["bitrate"]?.jsonPrimitive?.intOrNull ?: 128000,
                mimeType = obj["mimeType"]?.jsonPrimitive?.content ?: "audio/mp4",
                codec = obj["codec"]?.jsonPrimitive?.content ?: "mp4a",
                audioTrackLocale = obj["audioTrackLocale"]?.jsonPrimitive?.content
            )
        }

        val subtitles = (root["subtitles"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { el ->
            val obj = el.jsonObject
            val url = obj["url"]?.jsonPrimitive?.content ?: return@mapNotNull null
            SubtitleTrack(
                url = url,
                languageCode = obj["code"]?.jsonPrimitive?.content ?: "en",
                languageName = obj["name"]?.jsonPrimitive?.content ?: "English",
                isAutoGenerated = obj["autoGenerated"]?.jsonPrimitive?.booleanOrNull ?: false,
                mimeType = obj["mimeType"]?.jsonPrimitive?.content ?: "text/vtt"
            )
        }

        val chapters = (root["chapters"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { el ->
            val obj = el.jsonObject
            VideoChapter(
                title = obj["title"]?.jsonPrimitive?.content ?: "Chapter",
                startTimeSeconds = obj["start"]?.jsonPrimitive?.longOrNull ?: 0L,
                thumbnailUrl = obj["image"]?.jsonPrimitive?.content ?: ""
            )
        }

        val related = parseStreamItemList(root["relatedStreams"]?.jsonArray ?: JsonArray(emptyList()))

        return VideoItem(
            id = videoId,
            title = root["title"]?.jsonPrimitive?.content ?: "Untitled Video",
            uploaderName = root["uploader"]?.jsonPrimitive?.content ?: "Unknown Channel",
            uploaderId = (root["uploaderUrl"]?.jsonPrimitive?.content ?: "").removePrefix("/channel/"),
            uploaderAvatar = root["uploaderAvatar"]?.jsonPrimitive?.content ?: "",
            uploaderVerified = root["uploaderVerified"]?.jsonPrimitive?.booleanOrNull ?: false,
            thumbnailUrl = root["thumbnailUrl"]?.jsonPrimitive?.content ?: "",
            durationSeconds = root["duration"]?.jsonPrimitive?.longOrNull ?: 0L,
            viewCount = root["views"]?.jsonPrimitive?.longOrNull ?: 0L,
            uploadedText = root["uploadDate"]?.jsonPrimitive?.content ?: "",
            description = root["description"]?.jsonPrimitive?.content ?: "",
            likeCount = root["likes"]?.jsonPrimitive?.longOrNull ?: 0L,
            dislikeCount = root["dislikes"]?.jsonPrimitive?.longOrNull ?: 0L,
            subscriberCount = root["uploaderSubscriberCount"]?.jsonPrimitive?.longOrNull ?: 0L,
            isLive = root["livestream"]?.jsonPrimitive?.booleanOrNull ?: false,
            category = root["category"]?.jsonPrimitive?.content ?: "General",
            hlsUrl = root["hls"]?.jsonPrimitive?.content,
            dashUrl = root["dash"]?.jsonPrimitive?.content,
            videoStreams = videoStreams,
            audioStreams = audioStreams,
            subtitleTracks = subtitles,
            chapters = chapters,
            relatedVideos = related
        )
    }

    internal fun parseStreamItemList(array: JsonArray): List<VideoItem> {
        return array.mapNotNull { el ->
            if (el is JsonObject) parseStreamItem(el) else null
        }
    }

    internal fun parseStreamItem(obj: JsonObject): VideoItem? {
        val rawUrl = obj["url"]?.jsonPrimitive?.content ?: return null
        val id = Formatters.extractYoutubeVideoId(rawUrl)
        if (id.isBlank()) return null
        return VideoItem(
            id = id,
            title = obj["title"]?.jsonPrimitive?.content ?: "Untitled",
            uploaderName = obj["uploaderName"]?.jsonPrimitive?.content ?: "Unknown Channel",
            uploaderId = (obj["uploaderUrl"]?.jsonPrimitive?.content ?: "").removePrefix("/channel/"),
            uploaderAvatar = obj["uploaderAvatar"]?.jsonPrimitive?.content ?: "",
            uploaderVerified = obj["uploaderVerified"]?.jsonPrimitive?.booleanOrNull ?: false,
            thumbnailUrl = obj["thumbnail"]?.jsonPrimitive?.content ?: "",
            durationSeconds = obj["duration"]?.jsonPrimitive?.longOrNull ?: 0L,
            viewCount = obj["views"]?.jsonPrimitive?.longOrNull ?: 0L,
            uploadedText = obj["uploadedDate"]?.jsonPrimitive?.content ?: "",
            uploadedTimestamp = obj["uploaded"]?.jsonPrimitive?.longOrNull ?: 0L,
            isShort = obj["isShort"]?.jsonPrimitive?.booleanOrNull ?: false
        )
    }

    private fun parseChannelSummary(obj: JsonObject): ChannelDetail? {
        val rawUrl = obj["url"]?.jsonPrimitive?.content ?: return null
        val id = rawUrl.removePrefix("/channel/")
        return ChannelDetail(
            id = id,
            name = obj["name"]?.jsonPrimitive?.content ?: "Channel",
            avatarUrl = obj["thumbnail"]?.jsonPrimitive?.content ?: "",
            description = obj["description"]?.jsonPrimitive?.content ?: "",
            subscriberCount = obj["subscribers"]?.jsonPrimitive?.longOrNull ?: 0L,
            isVerified = obj["verified"]?.jsonPrimitive?.booleanOrNull ?: false
        )
    }

    private fun parsePlaylistSummary(obj: JsonObject): PlaylistSummary? {
        val rawUrl = obj["url"]?.jsonPrimitive?.content ?: return null
        val id = rawUrl.substringAfter("list=", rawUrl.removePrefix("/playlist?list="))
        return PlaylistSummary(
            id = id,
            name = obj["name"]?.jsonPrimitive?.content ?: "Playlist",
            thumbnailUrl = obj["thumbnail"]?.jsonPrimitive?.content ?: "",
            uploaderName = obj["uploaderName"]?.jsonPrimitive?.content ?: "",
            videoCount = obj["videos"]?.jsonPrimitive?.intOrNull ?: 0
        )
    }

    private fun buildInstanceCandidateList(primaryUrl: String): List<String> {
        val result = mutableListOf<String>()
        if (primaryUrl.isNotBlank()) result.add(primaryUrl.trim().removeSuffix("/"))
        for (inst in OpenMediaCatalog.defaultInstances) {
            val clean = inst.apiUrl.removeSuffix("/")
            if (!result.contains(clean)) {
                result.add(clean)
            }
        }
        return result
    }

    private fun executeGet(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "ReTube-Android/1.0 (Mozilla/5.0; Android 14)")
            .header("Accept", "application/json")
            .get()
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            return response.body?.string()
        }
    }
}