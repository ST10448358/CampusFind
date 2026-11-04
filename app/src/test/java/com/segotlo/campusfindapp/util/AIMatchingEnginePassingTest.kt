
package com.segotlo.campusfindapp.util

import com.segotlo.campusfindapp.data.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class AIMatchingEnginePassingTest {

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
        // Create matching lost and found reports.
        val lost = createReport(ReportType.LOST)
        val found = createReport(ReportType.FOUND)

        // Evaluate whether the matching engine identifies a match.
        val result = AIMatchingEngine.evaluateMatches(lost, listOf(found))

        // Confirm that a match is returned with the expected confidence.
        assertNotNull(result)
        assertEquals(ReportType.LOST, result!!.lostReport.type)
        assertEquals(ReportType.FOUND, result.foundReport.type)
        assertEquals(90, result.confidenceScore)
        assertTrue(result.confidenceScore >= 45)
    }
}
