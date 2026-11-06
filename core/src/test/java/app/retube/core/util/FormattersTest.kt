package app.retube.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun formatDuration_formatsMinutesAndHoursCorrectly() {
        assertEquals("05:09", Formatters.formatDuration(309L))
        assertEquals("1:02:03", Formatters.formatDuration(3723L))
        assertEquals("LIVE", Formatters.formatDuration(0L))
    }

    @Test
    fun formatCompactCount_formatsThousandsMillionsBillions() {
        assertEquals("950", Formatters.formatCompactCount(950L))
        assertEquals("14.5K", Formatters.formatCompactCount(14_500L))
        assertEquals("2.4M", Formatters.formatCompactCount(2_400_000L))
    }

    @Test
    fun extractYoutubeVideoId_handlesVariousUrlFormats() {
        assertEquals("aqz-KE-bpKQ", Formatters.extractYoutubeVideoId("https://www.youtube.com/watch?v=aqz-KE-bpKQ&t=12s"))
        assertEquals("aqz-KE-bpKQ", Formatters.extractYoutubeVideoId("https://youtu.be/aqz-KE-bpKQ?si=123"))
        assertEquals("aqz-KE-bpKQ", Formatters.extractYoutubeVideoId("https://piped.video/streams/aqz-KE-bpKQ"))
        assertEquals("aqz-KE-bpKQ", Formatters.extractYoutubeVideoId("aqz-KE-bpKQ"))
    }
}