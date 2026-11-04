
package com.segotlo.campusfindapp.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class ReportSearchFilterFailureDemoTest {

    @Test
    fun searchShouldFindPassportButDemonstratesIncorrectExpectation() {
        val report = Report(
            userId = "test-user",
            userDisplayName = "Test Student",
            userEmail = "test@example.com",
            type = ReportType.LOST,
            title = "Lost Student Card",
            description = "Blue student card near the library",
            category = ItemCategory.STUDENT_CARDS_IDS,
            location = CampusLocation.MAIN_LIBRARY,
            specificLocationDetail = "Near the entrance",
            institution = "University of Pretoria",
            dateOccurred = Date(0)
        )

        val result = ReportSearchFilter.filter(listOf(report), "Passport")

        // Deliberately incorrect: no report matches "Passport".
        assertEquals(1, result.size)
    }
}
