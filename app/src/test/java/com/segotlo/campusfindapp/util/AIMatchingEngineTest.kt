package com.segotlo.campusfindapp.util

import com.segotlo.campusfindapp.data.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date



    class AIMatchingEngineTest {

        private fun createReport(
            type: ReportType,
            title: String,
            description: String = "Item reported on campus",
            status: ReportStatus = ReportStatus.OPEN,
            location: CampusLocation = CampusLocation.MAIN_LIBRARY
        ): Report {
            return Report(
                userId = "user123",
                userDisplayName = "Test User",
                userEmail = "test@example.com",
                type = type,
                title = title,
                description = description,
                category = ItemCategory.ELECTRONICS,
                location = location,
                specificLocationDetail = "Near entrance",
                dateOccurred = Date(),
                status = status
            )
        }

        @Test
        fun similarLostAndFoundItemsProduceMatch() {
            val lost = createReport(
                ReportType.LOST,
                "Samsung Phone"
            )

            val found = createReport(
                ReportType.FOUND,
                "Samsung Phone"
            )

            val result = AIMatchingEngine.evaluateMatches(
                lost,
                listOf(found)
            )

            assertNotNull(result)
            assertEquals(ReportType.LOST, result!!.lostReport.type)
            assertEquals(ReportType.FOUND, result.foundReport.type)
        }

        @Test
        fun matchingDescriptionsProduceKeywordMatch() {
            val lost = createReport(
                ReportType.LOST,
                "Missing Item",
                "Black Samsung device with cracked screen"
            )

            val found = createReport(
                ReportType.FOUND,
                "Found Object",
                "Black Samsung device with cracked screen"
            )

            val result = AIMatchingEngine.evaluateMatches(
                lost,
                listOf(found)
            )

            assertNotNull(result)
            assertTrue(result!!.confidenceScore >= 45)
        }

        @Test
        fun phoneAndChargerConflictProducesNoMatch() {
            val lost = createReport(
                ReportType.LOST,
                "Samsung Phone"
            )

            val found = createReport(
                ReportType.FOUND,
                "Phone Charger"
            )

            assertNull(
                AIMatchingEngine.evaluateMatches(lost, listOf(found))
            )
        }

        @Test
        fun laptopAndPhoneConflictProducesNoMatch() {
            val lost = createReport(
                ReportType.LOST,
                "Laptop"
            )

            val found = createReport(
                ReportType.FOUND,
                "Phone"
            )

            assertNull(
                AIMatchingEngine.evaluateMatches(lost, listOf(found))
            )
        }

        @Test
        fun sameReportTypeDoesNotProduceMatch() {
            val first = createReport(
                ReportType.LOST,
                "Samsung Phone"
            )

            val second = createReport(
                ReportType.LOST,
                "Samsung Phone"
            )

            assertNull(
                AIMatchingEngine.evaluateMatches(first, listOf(second))
            )
        }

        @Test
        fun closedReportIsExcludedFromMatching() {
            val lost = createReport(
                ReportType.LOST,
                "Samsung Phone"
            )

            val found = createReport(
                ReportType.FOUND,
                "Samsung Phone",
                status = ReportStatus.CLOSED
            )

            assertNull(
                AIMatchingEngine.evaluateMatches(lost, listOf(found))
            )
        }

        @Test
        fun emptyExistingReportsProduceNoMatch() {
            val lost = createReport(
                ReportType.LOST,
                "Samsung Phone"
            )

            assertNull(
                AIMatchingEngine.evaluateMatches(lost, emptyList())
            )
        }

        @Test
        fun matchingIgnoresUppercaseAndLowercaseDifferences() {
            val lost = createReport(
                ReportType.LOST,
                "SAMSUNG PHONE"
            )

            val found = createReport(
                ReportType.FOUND,
                "samsung phone"
            )

            val result = AIMatchingEngine.evaluateMatches(
                lost,
                listOf(found)
            )

            assertNotNull(result)
            assertTrue(result!!.confidenceScore > 0)
        }

        @Test
        fun matchingReportsProduceExpectedConfidence() {
            val lost = createReport(
                ReportType.LOST,
                "Samsung Phone"
            )

            val found = createReport(
                ReportType.FOUND,
                "Samsung Phone"
            )

            val result = AIMatchingEngine.evaluateMatches(
                lost,
                listOf(found)
            )

            assertEquals(90, result!!.confidenceScore)
        }
    }

