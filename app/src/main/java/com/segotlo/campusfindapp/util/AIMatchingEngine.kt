package com.segotlo.campusfindapp.util

import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Intelligent AI matching engine for evaluating potential lost & found report pairings.
 * 
 * Uses spatial proximity (Haversine formula), keyword token overlap, category filtering,
 * and conflict heuristics to compute confidence scores for automatic match suggestions.
 */
object AIMatchingEngine {

    /**
     * Container representing an evaluated match candidate between a lost report and a found report.
     *
     * @property lostReport The lost item report.
     * @property foundReport The corresponding found item report.
     * @property confidenceScore Overall match confidence rating (scale 0 - 100).
     * @property reasoning Human-readable breakdown of match factors (GPS, title, description overlap).
     */
    data class MatchSuggestion(
        val lostReport: Report,
        val foundReport: Report,
        val confidenceScore: Int,
        val reasoning: String
    )

    /**
     * Evaluates a newly submitted report against existing open reports of the opposite type
     * (e.g. Lost vs. Found) to find the highest-scoring candidate match.
     *
     * @param newReport The newly created or updated report.
     * @param existingReports List of all active reports in the repository.
     * @return Best matching [MatchSuggestion] if confidence >= 45, or null if no reliable match is found.
     */
    fun evaluateMatches(newReport: Report, existingReports: List<Report>): MatchSuggestion? {
        // Filter candidate pool to open reports of the opposite type (Lost <-> Found)
        val oppositeType = if (newReport.type == ReportType.LOST) ReportType.FOUND else ReportType.LOST
        val candidates = existingReports.filter { it.type == oppositeType && it.status == ReportStatus.OPEN }

        var bestCandidate: Report? = null
        var highestScore = 0
        var bestReasoning = ""

        for (candidate in candidates) {
            val newTitleLower = newReport.title.lowercase()
            val candidateTitleLower = candidate.title.lowercase()

            // Disallow obvious mismatched object pairs (e.g., Charger vs Phone or Headphones vs Charger)
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

            // 1. GPS Map Proximity (Haversine distance in meters between report coordinates)
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

            // 2. Title & Description Keyword Overlap (Required for high confidence matching)
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

            // Evaluate if this candidate meets minimum confidence threshold (45+) and exceeds prior scores
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

    /**
     * Calculates the great-circle distance between two GPS geographic points using the Haversine formula.
     *
     * @param lat1 Latitude of first location.
     * @param lon1 Longitude of first location.
     * @param lat2 Latitude of second location.
     * @param lon2 Longitude of second location.
     * @return Distance in meters.
     */
    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // Mean Earth radius in meters
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
