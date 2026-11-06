package app.retube.core.util

import java.util.Locale

object Formatters {

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0L) return "LIVE"
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    fun formatCompactCount(count: Long): String {
        if (count < 0) return "0"
        return when {
            count >= 1_000_000_000L -> String.format(Locale.US, "%.1fB", count / 1_000_000_000.0)
            count >= 1_000_000L -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun extractYoutubeVideoId(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val vParamRegex = Regex("[?&]v=([a-zA-Z0-9_-]{6,15})")
        vParamRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }

        val shortUrlRegex = Regex("(?:youtu\\.be/|/shorts/|/embed/|/v/|/streams/)([a-zA-Z0-9_-]{6,15})")
        shortUrlRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }

        return trimmed.substringAfterLast("/").substringBefore("?").substringBefore("&")
    }
}