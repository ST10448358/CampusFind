package com.segotlo.campusfindapp.util

import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object AIMatchingEngine {

    data class MatchSuggestion(
        val lostReport: Report,
        val foundReport: Report,
        val confidenceScore: Int,
        val reasoning: String
    )

    fun evaluateMatches(newReport: Report, existingReports: List<Report>): MatchSuggestion? {
        val oppositeType = if (newReport.type == ReportType.LOST) ReportType.FOUND else ReportType.LOST
        val candidates = existingReports.filter { it.type == oppositeType && it.status == ReportStatus.OPEN }

        var bestCandidate: Report? = null
        var highestScore = 0
        var bestReasoning = ""

        for (candidate in candidates) {
            val newTitleLower = newReport.title.lowercase()
            val candidateTitleLower = candidate.title.lowercase()

            // Disallow obvious mismatched object pairs
            val isConflict = (newTitleLower.contains("phone") && candidateTitleLower.contains("charger")) ||
                             (newTitleLower.contains("charger") && candidateTitleLower.contains("phone")) ||
                             (newTitleLower.contains("headphones") && candidateTitleLower.contains("charger")) ||
                             (newTitleLower.contains("charger") && candidateTitleLower.contains("headphones")) ||
                             (newTitleLower.contains("laptop") && candidateTitleLower.contains("phone")) ||
                             (newTitleLower.contains("phone") && candidateTitleLower.contains("laptop")) ||
                             (newTitleLower.contains("id") && candidateTitleLower.contains("charger")) ||
                             (newTitleLower.contains("charger") && candidateTitleLower.contains("id"))

            if (isConflict) {
                continue
            }

            var score = 0
            val reasons = mutableListOf<String>()

            // 1. GPS Map Proximity (Haversine distance in meters)
            val distanceMeters = calculateDistanceMeters(
                newReport.effectiveLatitude, newReport.effectiveLongitude,
                candidate.effectiveLatitude, candidate.effectiveLongitude
            )
            
            if (distanceMeters <= 500.0) {
                score += 45
                reasons.add("Close GPS proximity (${distanceMeters.toInt()}m apart)")
            } else if (distanceMeters <= 2000.0) {
                score += 30
                reasons.add("Same campus area")
            } else {
                score += 15
                reasons.add("University match")
            }

            // 2. Title & Description Keyword Overlap (Required for high confidence)
            val newTitleWords = newReport.title.lowercase().split("\\s+".toRegex()).toSet()
            val candidateTitleWords = candidate.title.lowercase().split("\\s+".toRegex()).toSet()
            val titleOverlap = newTitleWords.intersect(candidateTitleWords).filter { it.length > 2 }

            val newWords = (newReport.title + " " + newReport.description).lowercase().split("\\s+".toRegex()).toSet()
            val candidateWords = (candidate.title + " " + candidate.description).lowercase().split("\\s+".toRegex()).toSet()
            val descOverlap = newWords.intersect(candidateWords).filter { it.length > 2 }

            if (titleOverlap.isNotEmpty()) {
                score += 45
                reasons.add("Matching title (${titleOverlap.joinToString(", ")})")
            } else if (descOverlap.isNotEmpty()) {
                score += 25
                reasons.add("Keyword overlap")
            }

            if (score > highestScore && score >= 45) {
                highestScore = score.coerceAtMost(98)
                bestCandidate = candidate
                bestReasoning = reasons.joinToString(" • ")
            }
        }

        return if (bestCandidate != null) {
            MatchSuggestion(
                lostReport = if (newReport.type == ReportType.LOST) newReport else bestCandidate,
                foundReport = if (newReport.type == ReportType.FOUND) newReport else bestCandidate,
                confidenceScore = highestScore,
                reasoning = bestReasoning
            )
        } else {
            null
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun Double.format(digits: Int) = java.lang.String.format(java.util.Locale.getDefault(), "%.${digits}f", this)
}
