package com.segotlo.campusfindapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
object MockRepository {
    private val userRepository: UserRepository = FirebaseUserRepository()
    private val reportRepository: ReportRepository = FirebaseReportRepository()
    private val notificationRepository: NotificationRepository = FirebaseNotificationRepository()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var appContext: android.content.Context? = null
    
    val currentUser: StateFlow<User?> = userRepository.currentUser
    val reports: StateFlow<List<Report>> = reportRepository.allReports

    val notifications: StateFlow<List<AppNotification>> = currentUser.flatMapLatest { user ->
        if (user != null) {
            notificationRepository.getNotificationsForUser(user.id)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun init(context: android.content.Context) {
        appContext = context.applicationContext
        com.segotlo.campusfindapp.util.NotificationHelper.createNotificationChannels(context.applicationContext)
    }

    fun sendNotificationWithPreferences(
        userId: String,
        title: String,
        message: String,
        reportId: String? = null,
        channelId: String = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_GENERAL
    ) {
        scope.launch {
            val appNotification = AppNotification(
                userId = userId,
                title = title,
                message = message,
                reportId = reportId
            )
            // Always save to in-app notification repository
            notificationRepository.sendNotification(appNotification)

            // Check target user preferences for OS Push Notification
            val user = currentUser.value
            if (user != null && user.id == userId) {
                if (!user.pushNotificationsEnabled) return@launch

                val isAllowed = when (channelId) {
                    com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_MATCHES -> user.matchAlertsEnabled
                    com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_UPDATES -> user.reportUpdatesEnabled
                    else -> user.pushNotificationsEnabled
                }

                if (isAllowed) {
                    appContext?.let { ctx ->
                        com.segotlo.campusfindapp.util.NotificationHelper.sendPushNotification(
                            context = ctx,
                            title = title,
                            message = message,
                            channelId = channelId
                        )
                    }
                }
            }
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return userRepository.login(email, password)
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        studentNumber: String,
        institution: String = "University of Pretoria",
        avatarUrl: String? = null
    ): Result<User> {
        return userRepository.register(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            studentNumber = studentNumber,
            institution = institution,
            avatarUrl = avatarUrl,
            role = if (email.contains("admin")) UserRole.ADMIN else UserRole.STUDENT
        )
    }

    fun getAllUsers(): StateFlow<List<User>> {
        return userRepository.getAllUsers()
    }

    fun updateUser(user: User) {
        scope.launch {
            userRepository.updateUser(user)
        }
    }

    fun logout() {
        scope.launch {
            userRepository.logout()
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun addReport(report: Report) {
        scope.launch {
            reportRepository.addReport(report)
            sendNotificationWithPreferences(
                userId = report.userId,
                title = if (report.type == ReportType.LOST) "Lost Item Reported" else "Found Item Reported",
                message = "Your report for '${report.title}' has been submitted successfully.",
                reportId = report.id,
                channelId = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_GENERAL
            )
        }
    }

    fun updateReportStatus(reportId: String, status: ReportStatus, customLabel: String?) {
        scope.launch {
            reportRepository.updateReportStatus(reportId, status, customLabel)
            val report = reports.value.find { it.id == reportId }
            report?.let { r ->
                sendNotificationWithPreferences(
                    userId = r.userId,
                    title = "Report Status Updated",
                    message = "Your report '${r.title}' status is now: ${status.name.lowercase().replaceFirstChar { it.uppercase() }}.",
                    reportId = r.id,
                    channelId = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_UPDATES
                )
            }
        }
    }

    fun matchReports(lostReport: Report, foundReport: Report) {
        scope.launch {
            // Update statuses and link IDs
            reportRepository.linkReports(lostReport.id, foundReport.id)
            
            // Send notifications according to user preferences
            sendNotificationWithPreferences(
                userId = lostReport.userId,
                title = "Possible Match Found!",
                message = "We found a match for your lost '${lostReport.title}'. Check the Matches tab.",
                reportId = lostReport.id,
                channelId = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_MATCHES
            )
            sendNotificationWithPreferences(
                userId = foundReport.userId,
                title = "Item Match Suggested",
                message = "The '${foundReport.title}' you found might belong to a student. Security will verify.",
                reportId = foundReport.id,
                channelId = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_MATCHES
            )
        }
    }

    fun confirmMatch(lostReport: Report, foundReport: Report) {
        scope.launch {
            reportRepository.updateReportStatus(lostReport.id, ReportStatus.CLAIMED, "Match Confirmed - Claimed")
            reportRepository.updateReportStatus(foundReport.id, ReportStatus.CLAIMED, "Match Confirmed - Claimed")
            
            sendNotificationWithPreferences(
                userId = lostReport.userId,
                title = "Match Confirmed!",
                message = "Your claim for '${lostReport.title}' has been approved. Please visit the Security Office for collection.",
                reportId = lostReport.id,
                channelId = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_MATCHES
            )
        }
    }

    fun unmatchReports(report1: Report, report2Id: String) {
        scope.launch {
            reportRepository.unlinkReports(report1.id, report2Id)
            
            sendNotificationWithPreferences(
                userId = report1.userId,
                title = "Match Update",
                message = "The previously suggested match for '${report1.title}' has been removed by admin for review.",
                reportId = report1.id,
                channelId = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_UPDATES
            )
            val otherReport = reports.value.find { it.id == report2Id }
            otherReport?.let {
                sendNotificationWithPreferences(
                    userId = it.userId,
                    title = "Match Update",
                    message = "The previously suggested match for '${it.title}' has been removed by admin for review.",
                    reportId = it.id,
                    channelId = com.segotlo.campusfindapp.util.NotificationHelper.CHANNEL_UPDATES
                )
            }
        }
    }

    fun getReportsByUser(userId: String): StateFlow<List<Report>> {
        return reportRepository.getReportsByUser(userId)
    }

    fun markNotificationAsRead(notificationId: String) {
        scope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }
}
