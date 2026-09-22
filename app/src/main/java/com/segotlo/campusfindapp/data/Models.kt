package com.segotlo.campusfindapp.data

import java.util.Date
import java.util.UUID

enum class UserRole {
    STUDENT, STAFF, ADMIN
}

enum class AccountStatus {
    ACTIVE, SUSPENDED
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val firstName: String,
    val lastName: String,
    val studentStaffNumber: String,
    val role: UserRole,
    val departmentOrFaculty: String,
    val institution: String = "",
    val avatarUrl: String? = null,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val pushNotificationsEnabled: Boolean = true,
    val matchAlertsEnabled: Boolean = true,
    val reportUpdatesEnabled: Boolean = true,
    val createdAt: Date = Date()
)

enum class ReportType {
    LOST, FOUND
}

enum class ReportStatus {
    OPEN, MATCH_SUGGESTED, UNDER_VERIFICATION, CLAIMED, RETURNED, CLOSED
}

enum class ItemCategory {
    ELECTRONICS, STUDENT_CARDS_IDS, KEYS_ACCESS, WALLETS_BAGS, CLOTHING_ACCESSORIES, ACADEMIC_BOOKS_NOTES, OTHER
}

enum class CampusLocation {
    MAIN_LIBRARY, SCIENCE_BUILDING, STUDENT_UNION, ENGINEERING_COMPLEX, SPORTS_CENTRE, CAMPUS_SECURITY_OFFICE, DINING_HALL, RESIDENTIAL_DORMS, OTHER
}

fun CampusLocation.getCoordinates(): Pair<Double, Double> {
    return when (this) {
        CampusLocation.MAIN_LIBRARY -> Pair(-25.7551, 28.2302)
        CampusLocation.SCIENCE_BUILDING -> Pair(-25.7529, 28.2335)
        CampusLocation.STUDENT_UNION -> Pair(-25.7538, 28.2321)
        CampusLocation.ENGINEERING_COMPLEX -> Pair(-25.7548, 28.2285)
        CampusLocation.SPORTS_CENTRE -> Pair(-25.7512, 28.2370)
        CampusLocation.CAMPUS_SECURITY_OFFICE -> Pair(-25.7562, 28.2290)
        CampusLocation.DINING_HALL -> Pair(-25.7560, 28.2340)
        CampusLocation.RESIDENTIAL_DORMS -> Pair(-25.7575, 28.2355)
        CampusLocation.OTHER -> Pair(-25.7545, 28.2315)
    }
}

data class Report(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userDisplayName: String,
    val userEmail: String,
    val type: ReportType,
    val title: String,
    val description: String,
    val category: ItemCategory,
    val location: CampusLocation,
    val specificLocationDetail: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val institution: String = "University of Pretoria (UP)",
    val dateOccurred: Date,
    val imageUrl: String? = null,
    val status: ReportStatus = ReportStatus.OPEN,
    val customStatusLabel: String? = null,
    val secretIdentifierQuestion: String? = null,
    val referenceCode: String = "CF-${(1000..9999).random()}",
    val matchedWithId: String? = null,
    val createdAt: Date = Date()
) {
    val effectiveLatitude: Double get() = latitude ?: location.getCoordinates().first
    val effectiveLongitude: Double get() = longitude ?: location.getCoordinates().second
}

data class Claim(
    val id: String = UUID.randomUUID().toString(),
    val reportId: String,
    val reportTitle: String,
    val claimantUserId: String,
    val claimantName: String,
    val claimantEmail: String,
    val proofDescription: String,
    val answerToSecretQuestion: String,
    val status: ClaimStatus = ClaimStatus.PENDING_REVIEW,
    val submittedAt: Date = Date()
)

enum class ClaimStatus {
    PENDING_REVIEW, APPROVED, REJECTED
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Date = Date(),
    val reportId: String? = null,
    val isRead: Boolean = false
)
