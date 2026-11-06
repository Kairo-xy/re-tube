package app.retube.providers

import app.retube.core.model.SponsorCategory
import app.retube.providers.sponsorblock.SponsorBlockClient
import org.junit.Assert.assertEquals
import org.junit.Test

class SponsorBlockParserTest {

    @Test
    fun parseSegmentsJson_extractsCategoryAndTimestamps() {
        val sampleJson = """
            [
              {
                "segment": [12.5, 42.0],
                "UUID": "abc-123",
                "category": "sponsor",
                "actionType": "skip"
              },
              {
                "segment": [180.0, 195.5],
                "UUID": "def-456",
                "category": "selfpromo",
                "actionType": "skip"
              }
            ]
        """.trimIndent()

        val client = SponsorBlockClient()
        val parsed = client.parseSegmentsJson(sampleJson)

        assertEquals(2, parsed.size)
        assertEquals(SponsorCategory.SPONSOR, parsed[0].category)
        assertEquals(12.5f, parsed[0].startTimeSeconds, 0.01f)
        assertEquals(42.0f, parsed[0].endTimeSeconds, 0.01f)
        assertEquals(SponsorCategory.SELFPROMO, parsed[1].category)
    }
}