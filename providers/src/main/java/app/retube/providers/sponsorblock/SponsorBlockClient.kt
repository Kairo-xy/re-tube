package app.retube.providers.sponsorblock

import app.retube.core.model.SponsorCategory
import app.retube.core.model.SponsorSegment
import app.retube.core.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class SponsorBlockClient(
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchSegments(
        videoId: String,
        enabledCategories: Set<String>
    ): List<SponsorSegment> = withContext(Dispatchers.IO) {
        if (enabledCategories.isEmpty()) return@withContext emptyList()
        val cleanId = Formatters.extractYoutubeVideoId(videoId)
        val categoriesJson = enabledCategories.joinToString(prefix = "["", separator = "","", postfix = ""]")
        val encodedCategories = URLEncoder.encode(categoriesJson, "UTF-8")
        val url = "https://sponsor.ajay.app/api/skipSegments?videoID=$cleanId&categories=$encodedCategories"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ReTube-Android/1.0")
                .get()
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                return@withContext parseSegmentsJson(body)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    internal fun parseSegmentsJson(rawJson: String): List<SponsorSegment> {
        return try {
            val arr = json.parseToJsonElement(rawJson).jsonArray
            arr.mapNotNull { el ->
                val obj = el.jsonObject
                val segmentArr = obj["segment"]?.jsonArray ?: return@mapNotNull null
                if (segmentArr.size < 2) return@mapNotNull null
                val start = segmentArr[0].jsonPrimitive.floatOrNull ?: return@mapNotNull null
                val end = segmentArr[1].jsonPrimitive.floatOrNull ?: return@mapNotNull null
                val catKey = obj["category"]?.jsonPrimitive?.content ?: "sponsor"
                val uuid = obj["UUID"]?.jsonPrimitive?.content ?: "$catKey-$start"
                val actionType = obj["actionType"]?.jsonPrimitive?.content ?: "skip"
                SponsorSegment(
                    uuid = uuid,
                    category = SponsorCategory.fromApiKey(catKey),
                    startTimeSeconds = start,
                    endTimeSeconds = end,
                    actionType = actionType
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}