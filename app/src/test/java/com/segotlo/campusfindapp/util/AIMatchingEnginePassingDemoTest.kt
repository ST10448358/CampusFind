
package com.segotlo.campusfindapp.util

import com.segotlo.campusfindapp.data.*
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class AIMatchingEnginePassingDemoTest {

    private fun createReport(type: ReportType): Report {
        return Report(
            userId = "user123",
            userDisplayName = "Test User",
            userEmail = "test@example.com",
            type = type,
            title = "Samsung Phone",
            description = "Samsung phone reported near the library",
            category = ItemCategory.ELECTRONICS,
            location = CampusLocation.MAIN_LIBRARY,
            specificLocationDetail = "Near entrance",
            dateOccurred = Date(),
            status = ReportStatus.OPEN
        )
    }

    @Test
    fun matchingLostAndFoundReportsReturnsHighConfidence() {
        val lost = createReport(ReportType.LOST)
        val found = createReport(ReportType.FOUND)

        val result = AIMatchingEngine.evaluateMatches(
            lost,
            listOf(found)
        )

        assertNotNull(result)
        assertEquals(ReportType.LOST, result!!.lostReport.type)
        assertEquals(ReportType.FOUND, result.foundReport.type)
        assertTrue(result.confidenceScore >= 45)
    }
}
