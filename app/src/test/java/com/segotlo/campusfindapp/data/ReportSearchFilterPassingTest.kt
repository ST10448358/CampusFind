
package com.segotlo.campusfindapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ReportSearchFilterPassingTest {

    private fun createReport(
        title: String,
        description: String,
        type: ReportType,
        category: ItemCategory,
        location: CampusLocation
    ): Report {
        return Report(
            userId = "test-user",
            userDisplayName = "Test Student",
            userEmail = "test@example.com",
            type = type,
            title = title,
            description = description,
            category = category,
            location = location,
            specificLocationDetail = "Near the entrance",
            institution = "University of Pretoria",
            dateOccurred = Date(0)
        )
    }

    private val reports = listOf(
        createReport(
            title = "Lost Student Card",
            description = "Blue student card near the library",
            type = ReportType.LOST,
            category = ItemCategory.STUDENT_CARDS_IDS,
            location = CampusLocation.MAIN_LIBRARY
        ),
        createReport(
            title = "Found Laptop",
            description = "Black laptop in the engineering building",
            type = ReportType.FOUND,
            category = ItemCategory.ELECTRONICS,
            location = CampusLocation.ENGINEERING_COMPLEX
        ),
        createReport(
            title = "Lost Keys",
            description = "Set of keys near the student union",
            type = ReportType.LOST,
            category = ItemCategory.KEYS_ACCESS,
            location = CampusLocation.STUDENT_UNION
        )
    )

    @Test
    fun validSearchReturnsMatchingReport() {
        val result = ReportSearchFilter.filter(reports, "Student Card")

        // Check that a valid search returns the correct report.
        assertEquals(1, result.size)
        assertEquals("Lost Student Card", result[0].title)
    }

    @Test
    fun searchIsCaseInsensitive() {
        val result = ReportSearchFilter.filter(reports, "lAPTOP")

        // Check that uppercase and lowercase letters both match.
        assertEquals(1, result.size)
        assertEquals("Found Laptop", result[0].title)
    }

    @Test
    fun invalidSearchReturnsNoResults() {
        val result = ReportSearchFilter.filter(reports, "Passport")

        // Check that a search with no matching report returns an empty list.
        assertTrue(result.isEmpty())
    }

    @Test
    fun emptySearchReturnsAllReports() {
        val result = ReportSearchFilter.filter(reports, "")

        // Check that an empty search does not exclude any reports.
        assertEquals(3, result.size)
    }

    @Test
    fun lostTypeFilterReturnsOnlyLostReports() {
        val result = ReportSearchFilter.filter(
            reports,
            query = "",
            type = ReportType.LOST
        )

        // Check that the filter excludes found reports.
        assertEquals(2, result.size)
        assertTrue(result.all { it.type == ReportType.LOST })
    }

    @Test
    fun categoryFilterReturnsElectronics() {
        val result = ReportSearchFilter.filter(
            reports,
            query = "",
            category = ItemCategory.ELECTRONICS
        )

        // Check that only reports in the selected category are returned.
        assertEquals(1, result.size)
        assertEquals("Found Laptop", result[0].title)
    }
}
