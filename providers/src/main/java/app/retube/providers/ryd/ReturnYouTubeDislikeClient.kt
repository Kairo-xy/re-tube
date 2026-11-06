package app.retube.providers.ryd

import app.retube.core.model.ReturnYouTubeDislikeVotes
import app.retube.core.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class ReturnYouTubeDislikeClient(
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchVotes(videoId: String): ReturnYouTubeDislikeVotes? = withContext(Dispatchers.IO) {
        val cleanId = Formatters.extractYoutubeVideoId(videoId)
        val url = "https://returnyoutubedislikeapi.com/votes?videoId=$cleanId"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ReTube-Android/1.0")
                .get()
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val obj = json.parseToJsonElement(body).jsonObject
                ReturnYouTubeDislikeVotes(
                    id = obj["id"]?.jsonPrimitive?.content ?: cleanId,
                    likes = obj["likes"]?.jsonPrimitive?.longOrNull ?: 0L,
                    dislikes = obj["dislikes"]?.jsonPrimitive?.longOrNull ?: 0L,
                    rating = obj["rating"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    viewCount = obj["viewCount"]?.jsonPrimitive?.longOrNull ?: 0L
                )
            }
        } catch (_: Exception) {
            null
        }
    }
}