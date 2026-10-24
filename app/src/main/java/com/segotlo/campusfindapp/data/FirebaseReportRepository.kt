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
    
    private val _allReports = MutableStateFlow<List<Report>>(emptyList())
    override val allReports: StateFlow<List<Report>> = _allReports.asStateFlow()
    
    private val userReportFlows = mutableMapOf<String, MutableStateFlow<List<Report>>>()

    init {
        listenToAllReports()
    }

    private fun listenToAllReports() {
        try {
            firestore?.collection("reports")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val reports = snapshot?.documents?.map { doc ->
                        mapDocumentToReport(doc.id, doc.data ?: emptyMap())
                    } ?: emptyList()
                    val sorted = reports.sortedByDescending { it.createdAt }
                    _allReports.value = sorted
                    userReportFlows.forEach { (uid, flow) ->
                        flow.value = sorted.filter { it.userId == uid }
                    }
                }
        } catch (_: Throwable) {}
    }

    override suspend fun addReport(report: Report): Result<Unit> {
        return try {
            val db = firestore ?: throw Exception("Firebase Firestore unavailable")
            val match = com.segotlo.campusfindapp.util.AIMatchingEngine.evaluateMatches(report, _allReports.value)
            val finalReport = if (match != null) {
                report.copy(status = ReportStatus.MATCH_SUGGESTED, matchedWithId = match.foundReport.id)
            } else {
                report
            }

            db.collection("reports").document(finalReport.id).set(mapReportToMap(finalReport)).await()
            if (match != null) {
                db.collection("reports").document(match.foundReport.id).update(mapOf("status" to ReportStatus.MATCH_SUGGESTED.name, "matchedWithId" to finalReport.id)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReportStatus(reportId: String, status: ReportStatus, customLabel: String?): Result<Unit> {
        return try {
            val db = firestore ?: throw Exception("Firebase Firestore unavailable")
            val updates = mutableMapOf<String, Any>("status" to status.name)
            if (customLabel != null) updates["customStatusLabel"] = customLabel
            db.collection("reports").document(reportId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkReports(reportId1: String, reportId2: String): Result<Unit> {
        return try {
            val db = firestore ?: throw Exception("Firebase Firestore unavailable")
            db.runBatch { batch ->
                val ref1 = db.collection("reports").document(reportId1)
                val ref2 = db.collection("reports").document(reportId2)
                batch.update(ref1, mapOf("status" to ReportStatus.MATCH_SUGGESTED.name, "matchedWithId" to reportId2))
                batch.update(ref2, mapOf("status" to ReportStatus.MATCH_SUGGESTED.name, "matchedWithId" to reportId1))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkReports(reportId1: String, reportId2: String): Result<Unit> {
        return try {
            val db = firestore ?: throw Exception("Firebase Firestore unavailable")
            db.runBatch { batch ->
                val ref1 = db.collection("reports").document(reportId1)
                val ref2 = db.collection("reports").document(reportId2)
                batch.update(ref1, mapOf("status" to ReportStatus.OPEN.name, "matchedWithId" to null))
                batch.update(ref2, mapOf("status" to ReportStatus.OPEN.name, "matchedWithId" to null))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReportsByUser(userId: String): StateFlow<List<Report>> {
        return userReportFlows.getOrPut(userId) {
            MutableStateFlow(_allReports.value.filter { it.userId == userId })
        }
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
            "latitude" to report.latitude,
            "longitude" to report.longitude,
            "institution" to report.institution,
            "dateOccurred" to report.dateOccurred,
            "imageUrl" to report.imageUrl,
            "status" to report.status.name,
            "customStatusLabel" to report.customStatusLabel,
            "secretIdentifierQuestion" to report.secretIdentifierQuestion,
            "referenceCode" to report.referenceCode,
            "matchedWithId" to report.matchedWithId,
            "handoverPin" to report.handoverPin,
            "createdAt" to report.createdAt
        )
    }

    private fun mapDocumentToReport(id: String, data: Map<String, Any>): Report {
        val typeStr = (data["type"] as? String ?: "LOST").uppercase()
        val parsedType = try { ReportType.valueOf(typeStr) } catch (e: Exception) { ReportType.LOST }

        val catStr = (data["category"] as? String ?: "ELECTRONICS").uppercase()
        val parsedCat = try { ItemCategory.valueOf(catStr) } catch (e: Exception) { ItemCategory.ELECTRONICS }

        val locStr = (data["location"] as? String ?: "MAIN_LIBRARY").uppercase()
        val parsedLoc = try { CampusLocation.valueOf(locStr) } catch (e: Exception) { CampusLocation.MAIN_LIBRARY }

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
            latitude = data["latitude"] as? Double,
            longitude = data["longitude"] as? Double,
            institution = data["institution"] as? String ?: "",
            dateOccurred = (data["dateOccurred"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            imageUrl = data["imageUrl"] as? String,
            status = parsedStatus,
            customStatusLabel = data["customStatusLabel"] as? String,
            secretIdentifierQuestion = data["secretIdentifierQuestion"] as? String,
            referenceCode = data["referenceCode"] as? String ?: "CF-1234",
            matchedWithId = data["matchedWithId"] as? String,
            handoverPin = data["handoverPin"] as? String ?: "1234",
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
        )
    }
}
