package com.segotlo.campusfindapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ReportSearchFilterTest {


    private fun makeReport(
        title: String = "Lost Student Card",
        description: String = "Blue student card near the library",
        institution: String = "University of Pretoria",
        type: ReportType = ReportType.LOST,
        category: ItemCategory = ItemCategory.STUDENT_CARDS_IDS,
        location: CampusLocation = CampusLocation.MAIN_LIBRARY
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
            institution = institution,
            dateOccurred = Date(0)
        )
    }

    private val reports = listOf(
        makeReport(),
        makeReport(
            title = "Found Laptop",
            description = "Black laptop in the engineering building",
            type = ReportType.FOUND,
            category = ItemCategory.ELECTRONICS,
            location = CampusLocation.ENGINEERING_COMPLEX
        ),
        makeReport(
            title = "Lost Keys",
            description = "Set of keys near the student union",
            category = ItemCategory.KEYS_ACCESS,
            location = CampusLocation.STUDENT_UNION
        )
    )

    @Test
    fun searchFindsMatchingTitle() {
        val result = ReportSearchFilter.filter(reports, "Student Card")

        assertEquals(1, result.size)
        assertEquals("Lost Student Card", result[0].title)
    }

    @Test
    fun searchIgnoresLetterCase() {
        val result = ReportSearchFilter.filter(reports, "lAPTOP")

        assertEquals(1, result.size)
        assertEquals("Found Laptop", result[0].title)
    }

    @Test
    fun searchFindsTextInDescription() {
        val result = ReportSearchFilter.filter(reports, "engineering building")

        assertEquals(1, result.size)
        assertEquals("Found Laptop", result[0].title)
    }

    @Test
    fun searchFindsInstitution() {
        val result = ReportSearchFilter.filter(reports, "University of Pretoria")

        assertEquals(3, result.size)
    }

    @Test
    fun emptyQueryReturnsAllReports() {
        val result = ReportSearchFilter.filter(reports, "")

        assertEquals(3, result.size)
    }

    @Test
    fun searchWithNoMatchReturnsEmptyList() {
        val result = ReportSearchFilter.filter(reports, "Passport")

        assertTrue(result.isEmpty())
    }

    @Test
    fun lostFilterExcludesFoundReports() {
        val result = ReportSearchFilter.filter(
            reports,
            query = "",
            type = ReportType.LOST
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.type == ReportType.LOST })
    }

    @Test
    fun categoryFilterReturnsOnlyMatchingCategory() {
        val result = ReportSearchFilter.filter(
            reports,
            query = "",
            category = ItemCategory.ELECTRONICS
        )

        assertEquals(1, result.size)
        assertEquals("Found Laptop", result[0].title)
    }

    @Test
    fun locationFilterReturnsOnlyMatchingLocation() {
        val result = ReportSearchFilter.filter(
            reports,
            query = "",
            location = CampusLocation.STUDENT_UNION
        )

        assertEquals(1, result.size)
        assertEquals("Lost Keys", result[0].title)
    }

    @Test
    fun combinedFiltersCanBeAppliedTogether() {
        val result = ReportSearchFilter.filter(
            reports,
            query = "laptop",
            type = ReportType.FOUND,
            category = ItemCategory.ELECTRONICS,
            location = CampusLocation.ENGINEERING_COMPLEX
        )

        assertEquals(1, result.size)
        assertEquals("Found Laptop", result[0].title)
    }
}
