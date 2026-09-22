package com.segotlo.campusfindapp.data

import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    fun getNotificationsForUser(userId: String): StateFlow<List<AppNotification>>
    suspend fun sendNotification(notification: AppNotification): Result<Unit>
    suspend fun markAsRead(notificationId: String): Result<Unit>
}
