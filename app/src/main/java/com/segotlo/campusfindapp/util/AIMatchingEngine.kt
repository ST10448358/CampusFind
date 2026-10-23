package com.segotlo.campusfindapp.util

import com.segotlo.campusfindapp.data.Report
import com.segotlo.campusfindapp.data.ReportStatus
import com.segotlo.campusfindapp.data.ReportType

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
            var score = 0
            val reasons = mutableListOf<String>()

            // 1. Category match (High weight)
            if (newReport.category == candidate.category) {
                score += 45
                reasons.add("Exact category match (${newReport.category.name.lowercase().replace('_', ' ')})")
            }

            // 2. Location match (Medium weight)
            if (newReport.location == candidate.location) {
                score += 30
                reasons.add("Same campus location (${newReport.location.name.lowercase().replace('_', ' ')})")
            }

            // 3. Keyword text similarity (Title & Description)
            val newWords = (newReport.title + " " + newReport.description).lowercase().split("\\s+".toRegex()).toSet()
            val candidateWords = (candidate.title + " " + candidate.description).lowercase().split("\\s+".toRegex()).toSet()
            val intersection = newWords.intersect(candidateWords)
            // filter out common stop words
            val meaningfulOverlap = intersection.filter { it.length > 3 }
            if (meaningfulOverlap.isNotEmpty()) {
                score += 25
                reasons.add("Keyword overlap (${meaningfulOverlap.take(2).joinToString(", ")})")
            }

            if (score > highestScore && score >= 50) {
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
}
