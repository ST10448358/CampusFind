package com.segotlo.campusfindapp.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseReportRepository : ReportRepository {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
    
    private val defaultInitialReports = listOf(
        Report(
            id = "rep_1",
            userId = "student_user_id",
            userDisplayName = "Thabo Mokoena",
            userEmail = "student@up.ac.za",
            type = ReportType.LOST,
            title = "MacBook Pro 14-inch",
            description = "Silver MacBook Pro left in the Main Library Study Hall near table 4.",
            category = ItemCategory.ELECTRONICS,
            location = CampusLocation.MAIN_LIBRARY,
            specificLocationDetail = "Floor 2, Study Hall",
            institution = "University of Pretoria",
            dateOccurred = Date(),
            status = ReportStatus.OPEN
        ),
        Report(
            id = "rep_2",
            userId = "admin_user_id",
            userDisplayName = "Campus Admin",
            userEmail = "admin@campusfind.ac.za",
            type = ReportType.FOUND,
            title = "Student ID Card - Computer Science",
            description = "Found ID card handed in at Campus Security Office.",
            category = ItemCategory.STUDENT_CARDS_IDS,
            location = CampusLocation.CAMPUS_SECURITY_OFFICE,
            specificLocationDetail = "Front Desk",
            institution = "University of Pretoria",
            dateOccurred = Date(),
            status = ReportStatus.OPEN
        )
    )

    private val _allReports = MutableStateFlow<List<Report>>(defaultInitialReports)
    override val allReports: StateFlow<List<Report>> = _allReports.asStateFlow()
    
    private val userReportFlows = mutableMapOf<String, MutableStateFlow<List<Report>>>()

    init {
        listenToAllReports()
    }

    private fun listenToAllReports() {
        try {
            firestore?.collection("reports")
                ?.orderBy("createdAt", Query.Direction.DESCENDING)
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val reports = snapshot?.documents?.map { doc ->
                        mapDocumentToReport(doc.id, doc.data ?: emptyMap())
                    } ?: emptyList()
                    if (reports.isNotEmpty()) {
                        _allReports.value = reports
                    }
                }
        } catch (_: Throwable) {}
    }

    override suspend fun addReport(report: Report): Result<Unit> {
        return try {
            if (firestore != null) {
                firestore.collection("reports").document(report.id).set(mapReportToMap(report)).await()
            }
            _allReports.value = listOf(report) + _allReports.value
            Result.success(Unit)
        } catch (e: Exception) {
            _allReports.value = listOf(report) + _allReports.value
            Result.success(Unit)
        }
    }

    override suspend fun updateReportStatus(reportId: String, status: ReportStatus, customLabel: String?): Result<Unit> {
        return try {
            if (firestore != null) {
                val updates = mutableMapOf<String, Any>("status" to status.name)
                if (customLabel != null) updates["customStatusLabel"] = customLabel
                firestore.collection("reports").document(reportId).update(updates).await()
            }
            val updated = _allReports.value.map { r ->
                if (r.id == reportId) r.copy(status = status, customStatusLabel = customLabel ?: r.customStatusLabel) else r
            }
            _allReports.value = updated
            Result.success(Unit)
        } catch (e: Exception) {
            val updated = _allReports.value.map { r ->
                if (r.id == reportId) r.copy(status = status, customStatusLabel = customLabel ?: r.customStatusLabel) else r
            }
            _allReports.value = updated
            Result.success(Unit)
        }
    }

    override suspend fun linkReports(reportId1: String, reportId2: String): Result<Unit> {
        return try {
            if (firestore != null) {
                firestore.runBatch { batch ->
                    val ref1 = firestore.collection("reports").document(reportId1)
                    val ref2 = firestore.collection("reports").document(reportId2)
                    
                    batch.update(ref1, mapOf("status" to ReportStatus.MATCH_SUGGESTED.name, "matchedWithId" to reportId2))
                    batch.update(ref2, mapOf("status" to ReportStatus.MATCH_SUGGESTED.name, "matchedWithId" to reportId1))
                }.await()
            }
            val updated = _allReports.value.map { r ->
                when (r.id) {
                    reportId1 -> r.copy(status = ReportStatus.MATCH_SUGGESTED, matchedWithId = reportId2)
                    reportId2 -> r.copy(status = ReportStatus.MATCH_SUGGESTED, matchedWithId = reportId1)
                    else -> r
                }
            }
            _allReports.value = updated
            Result.success(Unit)
        } catch (e: Exception) {
            val updated = _allReports.value.map { r ->
                when (r.id) {
                    reportId1 -> r.copy(status = ReportStatus.MATCH_SUGGESTED, matchedWithId = reportId2)
                    reportId2 -> r.copy(status = ReportStatus.MATCH_SUGGESTED, matchedWithId = reportId1)
                    else -> r
                }
            }
            _allReports.value = updated
            Result.success(Unit)
        }
    }

    override suspend fun unlinkReports(reportId1: String, reportId2: String): Result<Unit> {
        return try {
            if (firestore != null) {
                firestore.runBatch { batch ->
                    val ref1 = firestore.collection("reports").document(reportId1)
                    val ref2 = firestore.collection("reports").document(reportId2)
                    
                    batch.update(ref1, mapOf("status" to ReportStatus.OPEN.name, "matchedWithId" to null, "customStatusLabel" to null))
                    batch.update(ref2, mapOf("status" to ReportStatus.OPEN.name, "matchedWithId" to null, "customStatusLabel" to null))
                }.await()
            }
            val updated = _allReports.value.map { r ->
                when (r.id) {
                    reportId1 -> r.copy(status = ReportStatus.OPEN, matchedWithId = null, customStatusLabel = null)
                    reportId2 -> r.copy(status = ReportStatus.OPEN, matchedWithId = null, customStatusLabel = null)
                    else -> r
                }
            }
            _allReports.value = updated
            Result.success(Unit)
        } catch (e: Exception) {
            val updated = _allReports.value.map { r ->
                when (r.id) {
                    reportId1 -> r.copy(status = ReportStatus.OPEN, matchedWithId = null, customStatusLabel = null)
                    reportId2 -> r.copy(status = ReportStatus.OPEN, matchedWithId = null, customStatusLabel = null)
                    else -> r
                }
            }
            _allReports.value = updated
            Result.success(Unit)
        }
    }

    override fun getReportsByUser(userId: String): StateFlow<List<Report>> {
        val flow = userReportFlows.getOrPut(userId) { 
            MutableStateFlow(_allReports.value.filter { it.userId == userId })
        }
        
        try {
            firestore?.collection("reports")
                ?.whereEqualTo("userId", userId)
                ?.orderBy("createdAt", Query.Direction.DESCENDING)
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val reports = snapshot?.documents?.map { doc ->
                        mapDocumentToReport(doc.id, doc.data ?: emptyMap())
                    } ?: emptyList()
                    if (reports.isNotEmpty()) {
                        flow.value = reports
                    }
                }
        } catch (_: Throwable) {}

        // Keep updated whenever allReports changes
        flow.value = _allReports.value.filter { it.userId == userId }
        
        return flow.asStateFlow()
    }

    private fun mapReportToMap(report: Report): Map<String, Any?> {
        return mapOf(
            "userId" to report.userId,
            "userDisplayName" to report.userDisplayName,
            "userEmail" to report.userEmail,
            "type" to report.type.name,
            "title" to report.title,
            "description" to report.description,
            "category" to report.category.name,
            "location" to report.location.name,
            "specificLocationDetail" to report.specificLocationDetail,
            "institution" to report.institution,
            "dateOccurred" to report.dateOccurred,
            "imageUrl" to report.imageUrl,
            "status" to report.status.name,
            "customStatusLabel" to report.customStatusLabel,
            "secretIdentifierQuestion" to report.secretIdentifierQuestion,
            "referenceCode" to report.referenceCode,
            "matchedWithId" to report.matchedWithId,
            "createdAt" to report.createdAt
        )
    }

    private fun mapDocumentToReport(id: String, data: Map<String, Any>): Report {
        val typeStr = (data["type"] as? String ?: "LOST").uppercase()
        val parsedType = try { ReportType.valueOf(typeStr) } catch (e: Exception) { ReportType.LOST }

        val catStr = (data["category"] as? String ?: "OTHER").uppercase()
        val parsedCat = try { ItemCategory.valueOf(catStr) } catch (e: Exception) { ItemCategory.OTHER }

        val locStr = (data["location"] as? String ?: "OTHER").uppercase()
        val parsedLoc = try { CampusLocation.valueOf(locStr) } catch (e: Exception) { CampusLocation.OTHER }

        val statusStr = (data["status"] as? String ?: "OPEN").uppercase()
        val parsedStatus = try { ReportStatus.valueOf(statusStr) } catch (e: Exception) { ReportStatus.OPEN }

        return Report(
            id = id,
            userId = data["userId"] as? String ?: "",
            userDisplayName = data["userDisplayName"] as? String ?: "",
            userEmail = data["userEmail"] as? String ?: "",
            type = parsedType,
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            category = parsedCat,
            location = parsedLoc,
            specificLocationDetail = data["specificLocationDetail"] as? String ?: "",
            institution = data["institution"] as? String ?: "",
            dateOccurred = (data["dateOccurred"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            imageUrl = data["imageUrl"] as? String,
            status = parsedStatus,
            customStatusLabel = data["customStatusLabel"] as? String,
            secretIdentifierQuestion = data["secretIdentifierQuestion"] as? String,
            referenceCode = data["referenceCode"] as? String ?: "",
            matchedWithId = data["matchedWithId"] as? String,
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
        )
    }
}
