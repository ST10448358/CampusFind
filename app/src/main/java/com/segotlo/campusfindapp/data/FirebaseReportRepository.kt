package com.segotlo.campusfindapp.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseReportRepository : ReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _allReports = MutableStateFlow<List<Report>>(emptyList())
    override val allReports: StateFlow<List<Report>> = _allReports.asStateFlow()
    
    private val userReportFlows = mutableMapOf<String, MutableStateFlow<List<Report>>>()

    init {
        listenToAllReports()
    }

    private fun listenToAllReports() {
        firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val reports = snapshot?.documents?.map { doc ->
                    mapDocumentToReport(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                _allReports.value = reports
            }
    }

    override suspend fun addReport(report: Report): Result<Unit> {
        return try {
            firestore.collection("reports").document(report.id).set(mapReportToMap(report)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReportStatus(reportId: String, status: ReportStatus, customLabel: String?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status.name)
            if (customLabel != null) updates["customStatusLabel"] = customLabel
            firestore.collection("reports").document(reportId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkReports(reportId1: String, reportId2: String): Result<Unit> {
        return try {
            firestore.runBatch { batch ->
                val ref1 = firestore.collection("reports").document(reportId1)
                val ref2 = firestore.collection("reports").document(reportId2)
                
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
            firestore.runBatch { batch ->
                val ref1 = firestore.collection("reports").document(reportId1)
                val ref2 = firestore.collection("reports").document(reportId2)
                
                batch.update(ref1, mapOf("status" to ReportStatus.OPEN.name, "matchedWithId" to null, "customStatusLabel" to null))
                batch.update(ref2, mapOf("status" to ReportStatus.OPEN.name, "matchedWithId" to null, "customStatusLabel" to null))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReportsByUser(userId: String): StateFlow<List<Report>> {
        val flow = userReportFlows.getOrPut(userId) { MutableStateFlow(emptyList()) }
        
        firestore.collection("reports")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val reports = snapshot?.documents?.map { doc ->
                    mapDocumentToReport(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                flow.value = reports
            }
        
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
        return Report(
            id = id,
            userId = data["userId"] as? String ?: "",
            userDisplayName = data["userDisplayName"] as? String ?: "",
            userEmail = data["userEmail"] as? String ?: "",
            type = ReportType.valueOf(data["type"] as? String ?: "LOST"),
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            category = ItemCategory.valueOf(data["category"] as? String ?: "OTHER"),
            location = CampusLocation.valueOf(data["location"] as? String ?: "OTHER"),
            specificLocationDetail = data["specificLocationDetail"] as? String ?: "",
            institution = data["institution"] as? String ?: "",
            dateOccurred = (data["dateOccurred"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            imageUrl = data["imageUrl"] as? String,
            status = ReportStatus.valueOf(data["status"] as? String ?: "OPEN"),
            customStatusLabel = data["customStatusLabel"] as? String,
            secretIdentifierQuestion = data["secretIdentifierQuestion"] as? String,
            referenceCode = data["referenceCode"] as? String ?: "",
            matchedWithId = data["matchedWithId"] as? String,
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
        )
    }
}
