package app.retube.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.retube.core.model.SponsorCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "retube_settings")

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColor: Boolean = false,
    val pureBlackAmoled: Boolean = true,
    val defaultQuality: String = "1080p",
    val defaultPlaybackSpeed: Float = 1.0f,
    val pipedInstanceUrl: String = "https://pipedapi.kavin.rocks",
    val contentRegion: String = "US",
    val sponsorBlockEnabled: Boolean = true,
    val sponsorBlockCategories: Set<String> = setOf(
        SponsorCategory.SPONSOR.apiKey,
        SponsorCategory.SELFPROMO.apiKey,
        SponsorCategory.INTERACTION.apiKey,
        SponsorCategory.INTRO.apiKey,
        SponsorCategory.OUTRO.apiKey
    ),
    val returnYouTubeDislikeEnabled: Boolean = true,
    val backgroundPlaybackEnabled: Boolean = true,
    val autoPlayNext: Boolean = true,
    val pauseWatchHistory: Boolean = false,
    val pauseSearchHistory: Boolean = false
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PURE_BLACK_AMOLED = booleanPreferencesKey("pure_black_amoled")
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val DEFAULT_SPEED = floatPreferencesKey("default_playback_speed")
        val PIPED_INSTANCE_URL = stringPreferencesKey("piped_instance_url")
        val CONTENT_REGION = stringPreferencesKey("content_region")
        val SPONSOR_BLOCK_ENABLED = booleanPreferencesKey("sponsor_block_enabled")
        val SPONSOR_BLOCK_CATEGORIES = stringSetPreferencesKey("sponsor_block_categories")
        val RYD_ENABLED = booleanPreferencesKey("ryd_enabled")
        val BACKGROUND_PLAYBACK = booleanPreferencesKey("background_playback")
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val PAUSE_WATCH_HISTORY = booleanPreferencesKey("pause_watch_history")
        val PAUSE_SEARCH_HISTORY = booleanPreferencesKey("pause_search_history")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val modeName = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        val themeMode = ThemeMode.entries.firstOrNull { it.name == modeName } ?: ThemeMode.DARK
        UserPreferences(
            themeMode = themeMode,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            pureBlackAmoled = prefs[Keys.PURE_BLACK_AMOLED] ?: true,
            defaultQuality = prefs[Keys.DEFAULT_QUALITY] ?: "1080p",
            defaultPlaybackSpeed = prefs[Keys.DEFAULT_SPEED] ?: 1.0f,
            pipedInstanceUrl = prefs[Keys.PIPED_INSTANCE_URL] ?: "https://pipedapi.kavin.rocks",
            contentRegion = prefs[Keys.CONTENT_REGION] ?: "US",
            sponsorBlockEnabled = prefs[Keys.SPONSOR_BLOCK_ENABLED] ?: true,
            sponsorBlockCategories = prefs[Keys.SPONSOR_BLOCK_CATEGORIES] ?: setOf(
                SponsorCategory.SPONSOR.apiKey,
                SponsorCategory.SELFPROMO.apiKey,
                SponsorCategory.INTERACTION.apiKey,
                SponsorCategory.INTRO.apiKey,
                SponsorCategory.OUTRO.apiKey
            ),
            returnYouTubeDislikeEnabled = prefs[Keys.RYD_ENABLED] ?: true,
            backgroundPlaybackEnabled = prefs[Keys.BACKGROUND_PLAYBACK] ?: true,
            autoPlayNext = prefs[Keys.AUTO_PLAY_NEXT] ?: true,
            pauseWatchHistory = prefs[Keys.PAUSE_WATCH_HISTORY] ?: false,
            pauseSearchHistory = prefs[Keys.PAUSE_SEARCH_HISTORY] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setPureBlackAmoled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PURE_BLACK_AMOLED] = enabled }
    }

    suspend fun setDefaultQuality(quality: String) {
        context.dataStore.edit { it[Keys.DEFAULT_QUALITY] = quality }
    }

    suspend fun setDefaultPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.DEFAULT_SPEED] = speed }
    }

    suspend fun setPipedInstanceUrl(url: String) {
        context.dataStore.edit { it[Keys.PIPED_INSTANCE_URL] = url.trim().removeSuffix("/") }
    }

    suspend fun setContentRegion(region: String) {
        context.dataStore.edit { it[Keys.CONTENT_REGION] = region }
    }

    suspend fun setSponsorBlockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SPONSOR_BLOCK_ENABLED] = enabled }
    }

    suspend fun toggleSponsorCategory(categoryKey: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.SPONSOR_BLOCK_CATEGORIES]?.toMutableSet() ?: mutableSetOf(
                SponsorCategory.SPONSOR.apiKey,
                SponsorCategory.SELFPROMO.apiKey
            )
            if (current.contains(categoryKey)) {
                current.remove(categoryKey)
            } else {
                current.add(categoryKey)
            }
            prefs[Keys.SPONSOR_BLOCK_CATEGORIES] = current
        }
    }

    suspend fun setReturnYouTubeDislikeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RYD_ENABLED] = enabled }
    }

    suspend fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BACKGROUND_PLAYBACK] = enabled }
    }

    suspend fun setAutoPlayNext(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_PLAY_NEXT] = enabled }
    }

    suspend fun setPauseWatchHistory(paused: Boolean) {
        context.dataStore.edit { it[Keys.PAUSE_WATCH_HISTORY] = paused }
    }

    suspend fun setPauseSearchHistory(paused: Boolean) {
        context.dataStore.edit { it[Keys.PAUSE_SEARCH_HISTORY] = paused }
    }
}