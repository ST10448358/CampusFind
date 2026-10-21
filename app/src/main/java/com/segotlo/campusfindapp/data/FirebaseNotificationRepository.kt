package com.segotlo.campusfindapp.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseNotificationRepository : NotificationRepository {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
    private val userNotificationFlows = mutableMapOf<String, MutableStateFlow<List<AppNotification>>>()
    private val globalNotifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = "notif_1",
                userId = "student_user_id",
                title = "Welcome to CampusFind!",
                message = "Report lost items or help return found items on campus.",
                timestamp = Date(),
                isRead = false
            )
        )
    )

    override fun getNotificationsForUser(userId: String): StateFlow<List<AppNotification>> {
        val flow = userNotificationFlows.getOrPut(userId) { 
            MutableStateFlow(globalNotifications.value.filter { it.userId == userId })
        }
        
        try {
            firestore?.collection("notifications")
                ?.whereEqualTo("userId", userId)
                ?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val notifications = snapshot?.documents?.map { doc ->
                        mapDocumentToNotification(doc.id, doc.data ?: emptyMap())
                    } ?: emptyList()
                    if (notifications.isNotEmpty()) {
                        flow.value = notifications
                    }
                }
        } catch (_: Throwable) {}

        flow.value = globalNotifications.value.filter { it.userId == userId }
        return flow.asStateFlow()
    }

    override suspend fun sendNotification(notification: AppNotification): Result<Unit> {
        return try {
            if (firestore != null) {
                firestore.collection("notifications").document(notification.id).set(mapNotificationToMap(notification)).await()
            }
            globalNotifications.value = listOf(notification) + globalNotifications.value
            userNotificationFlows[notification.userId]?.value = listOf(notification) + (userNotificationFlows[notification.userId]?.value ?: emptyList())
            Result.success(Unit)
        } catch (_: Exception) {
            globalNotifications.value = listOf(notification) + globalNotifications.value
            userNotificationFlows[notification.userId]?.value = listOf(notification) + (userNotificationFlows[notification.userId]?.value ?: emptyList())
            Result.success(Unit)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            if (firestore != null) {
                firestore.collection("notifications").document(notificationId).update("isRead", true).await()
            }
            globalNotifications.value = globalNotifications.value.map { if (it.id == notificationId) it.copy(isRead = true) else it }
            userNotificationFlows.values.forEach { flow ->
                flow.value = flow.value.map { if (it.id == notificationId) it.copy(isRead = true) else it }
            }
            Result.success(Unit)
        } catch (_: Exception) {
            globalNotifications.value = globalNotifications.value.map { if (it.id == notificationId) it.copy(isRead = true) else it }
            userNotificationFlows.values.forEach { flow ->
                flow.value = flow.value.map { if (it.id == notificationId) it.copy(isRead = true) else it }
            }
            Result.success(Unit)
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
