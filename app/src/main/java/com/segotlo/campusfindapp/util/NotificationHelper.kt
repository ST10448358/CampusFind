package com.segotlo.campusfindapp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.segotlo.campusfindapp.MainActivity

object NotificationHelper {
    const val CHANNEL_MATCHES = "campus_find_matches"
    const val CHANNEL_UPDATES = "campus_find_updates"
    const val CHANNEL_GENERAL = "campus_find_general"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val matchesChannel = NotificationChannel(
                CHANNEL_MATCHES,
                "Match Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a lost item matches a found item"
                enableVibration(true)
            }

            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                "Report Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates on your submitted lost & found reports"
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General campus lost and found alerts"
            }

            manager.createNotificationChannel(matchesChannel)
            manager.createNotificationChannel(updatesChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendPushNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_GENERAL,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
