package com.segotlo.campusfindapp.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class ReportSearchFilterPassingDemoTest {

    @Test
    fun validSearchReturnsMatchingReport() {
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

        val result = ReportSearchFilter.filter(listOf(report), "Student Card")

        assertEquals(1, result.size)
        assertEquals("Lost Student Card", result[0].title)
    }
}
