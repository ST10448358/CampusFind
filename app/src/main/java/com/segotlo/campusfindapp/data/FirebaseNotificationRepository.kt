package com.segotlo.campusfindapp.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseNotificationRepository : NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val userNotificationFlows = mutableMapOf<String, MutableStateFlow<List<AppNotification>>>()

    override fun getNotificationsForUser(userId: String): StateFlow<List<AppNotification>> {
        val flow = userNotificationFlows.getOrPut(userId) { MutableStateFlow(emptyList()) }
        
        firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val notifications = snapshot?.documents?.map { doc ->
                    mapDocumentToNotification(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                flow.value = notifications
            }
        
        return flow.asStateFlow()
    }

    override suspend fun sendNotification(notification: AppNotification): Result<Unit> {
        return try {
            firestore.collection("notifications").document(notification.id).set(mapNotificationToMap(notification)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapNotificationToMap(notification: AppNotification): Map<String, Any?> {
        return mapOf(
            "userId" to notification.userId,
            "title" to notification.title,
            "message" to notification.message,
            "timestamp" to notification.timestamp,
            "reportId" to notification.reportId,
            "isRead" to notification.isRead
        )
    }

    private fun mapDocumentToNotification(id: String, data: Map<String, Any>): AppNotification {
        return AppNotification(
            id = id,
            userId = data["userId"] as? String ?: "",
            title = data["title"] as? String ?: "",
            message = data["message"] as? String ?: "",
            timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
            reportId = data["reportId"] as? String,
            isRead = data["isRead"] as? Boolean ?: false
        )
    }
}
