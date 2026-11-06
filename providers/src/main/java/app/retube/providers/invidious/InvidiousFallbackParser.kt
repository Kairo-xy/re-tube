package app.retube.providers.invidious

import app.retube.core.model.AudioStream
import app.retube.core.model.VideoItem
import app.retube.core.model.VideoStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

object InvidiousFallbackParser {

    fun parseVideoList(json: Json, body: String): List<VideoItem> {
        val element = json.parseToJsonElement(body)
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val videoId = obj["videoId"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val thumbs = obj["videoThumbnails"]?.jsonArray
            val thumbUrl = thumbs?.firstOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.content
                ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
            VideoItem(
                id = videoId,
                title = obj["title"]?.jsonPrimitive?.content ?: "Untitled",
                uploaderName = obj["author"]?.jsonPrimitive?.content ?: "Channel",
                uploaderId = obj["authorId"]?.jsonPrimitive?.content ?: "",
                uploaderVerified = obj["authorVerified"]?.jsonPrimitive?.booleanOrNull ?: false,
                thumbnailUrl = thumbUrl,
                durationSeconds = obj["lengthSeconds"]?.jsonPrimitive?.longOrNull ?: 0L,
                viewCount = obj["viewCount"]?.jsonPrimitive?.longOrNull ?: 0L,
                uploadedText = obj["publishedText"]?.jsonPrimitive?.content ?: ""
            )
        }
    }

    fun parseVideoDetail(json: Json, videoId: String, body: String): VideoItem? {
        val obj = json.parseToJsonElement(body).jsonObject
        val formatStreams = (obj["formatStreams"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { el ->
            val streamObj = el.jsonObject
            val url = streamObj["url"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val qualityLabel = streamObj["qualityLabel"]?.jsonPrimitive?.content ?: "720p"
            val height = qualityLabel.filter { it.isDigit() }.toIntOrNull() ?: 720
            VideoStream(
                url = url,
                format = streamObj["container"]?.jsonPrimitive?.content?.uppercase() ?: "MPEG_4",
                qualityLabel = qualityLabel,
                width = (height * 16) / 9,
                height = height,
                fps = streamObj["fps"]?.jsonPrimitive?.intOrNull ?: 30,
                mimeType = "video/mp4",
                isVideoOnly = false
            )
        }

        val adaptiveFormats = obj["adaptiveFormats"]?.jsonArray ?: JsonArray(emptyList())
        val audioStreams = adaptiveFormats.mapNotNull { el ->
            val aObj = el.jsonObject
            val type = aObj["type"]?.jsonPrimitive?.content ?: ""
            if (!type.startsWith("audio/")) return@mapNotNull null
            val url = aObj["url"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val bitrate = aObj["bitrate"]?.jsonPrimitive?.intOrNull ?: 128000
            AudioStream(
                url = url,
                format = aObj["container"]?.jsonPrimitive?.content?.uppercase() ?: "M4A",
                qualityLabel = "${bitrate / 1000} kbps",
                bitrate = bitrate,
                mimeType = type.substringBefore(";")
            )
        }

        val recommended = (obj["recommendedVideos"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { el ->
            val rObj = el.jsonObject
            val rId = rObj["videoId"]?.jsonPrimitive?.content ?: return@mapNotNull null
            VideoItem(
                id = rId,
                title = rObj["title"]?.jsonPrimitive?.content ?: "Video",
                uploaderName = rObj["author"]?.jsonPrimitive?.content ?: "Channel",
                uploaderId = rObj["authorId"]?.jsonPrimitive?.content ?: "",
                thumbnailUrl = "https://i.ytimg.com/vi/$rId/hqdefault.jpg",
                durationSeconds = rObj["lengthSeconds"]?.jsonPrimitive?.longOrNull ?: 0L,
                viewCount = rObj["viewCount"]?.jsonPrimitive?.longOrNull ?: 0L
            )
        }

        return VideoItem(
            id = videoId,
            title = obj["title"]?.jsonPrimitive?.content ?: "Untitled",
            uploaderName = obj["author"]?.jsonPrimitive?.content ?: "Unknown Channel",
            uploaderId = obj["authorId"]?.jsonPrimitive?.content ?: "",
            thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
            durationSeconds = obj["lengthSeconds"]?.jsonPrimitive?.longOrNull ?: 0L,
            viewCount = obj["viewCount"]?.jsonPrimitive?.longOrNull ?: 0L,
            uploadedText = obj["publishedText"]?.jsonPrimitive?.content ?: "",
            description = obj["description"]?.jsonPrimitive?.content ?: "",
            likeCount = obj["likeCount"]?.jsonPrimitive?.longOrNull ?: 0L,
            hlsUrl = obj["hlsUrl"]?.jsonPrimitive?.content,
            dashUrl = obj["dashUrl"]?.jsonPrimitive?.content,
            videoStreams = formatStreams,
            audioStreams = audioStreams,
            relatedVideos = recommended
        )
    }
}