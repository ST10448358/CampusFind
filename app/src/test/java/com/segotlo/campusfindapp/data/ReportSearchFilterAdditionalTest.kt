
package com.segotlo.campusfindapp.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class ReportSearchFilterAdditionalTest {

    @Test
    fun searchShouldReturnIncorrectExpectedCount() {

        val report = Report(
            userId = "test-user",
            userDisplayName = "Test Student",
            userEmail = "test@example.com",
            type = ReportType.LOST,
            title = "Lost Student Card",
            description = "Blue student card",
            category = ItemCategory.STUDENT_CARDS_IDS,
            location = CampusLocation.MAIN_LIBRARY,
            specificLocationDetail = "Near entrance",
            institution = "University of Pretoria",
            dateOccurred = Date(0)
        )

        val result = ReportSearchFilter.filter(
            listOf(report),
            "Student Card"
        )

        // Intentionally incorrect expectation
        assertEquals(2, result.size)
    }
}

