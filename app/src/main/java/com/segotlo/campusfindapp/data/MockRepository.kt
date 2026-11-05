package com.segotlo.campusfindapp.data

import android.os.Build
import com.segotlo.campusfindapp.util.NotificationHelper
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
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
object MockRepository {
    private val userRepository: UserRepository by lazy { FirebaseUserRepository() }
    private val reportRepository: ReportRepository by lazy { FirebaseReportRepository() }
    private val notificationRepository: NotificationRepository by lazy { FirebaseNotificationRepository() }
    private val scope = CoroutineScope(Dispatchers.IO)
    private var appContext: android.content.Context? = null
    
    val currentUser: StateFlow<User?> get() = userRepository.currentUser
    val reports: StateFlow<List<Report>> get() = reportRepository.allReports

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
        try {
            com.google.firebase.FirebaseApp.initializeApp(context.applicationContext)
        } catch (_: Exception) {
            // Already initialized
        }
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
            notificationRepository.sendNotification(appNotification)

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

    private fun notifyNewDeviceLogin(user: User) {
        val deviceName = try {
            val manufacturer = Build.MANUFACTURER.lowercase().replaceFirstChar { it.uppercase() }
            val model = Build.MODEL
            if (model.lowercase().startsWith(manufacturer.lowercase())) model else "$manufacturer $model"
        } catch (_: Throwable) {
            "Android Device"
        }
        val timeStr = SimpleDateFormat("HH:mm:ss, dd MMM yyyy", Locale.getDefault()).format(Date())

        sendNotificationWithPreferences(
            userId = user.id,
            title = "🔐 Security Alert: New Login",
            message = "Your account (${user.email}) was logged into on $deviceName at $timeStr.",
            reportId = null,
            channelId = NotificationHelper.CHANNEL_GENERAL
        )
    }

    suspend fun login(email: String, password: String): Result<User> {
        val res = userRepository.login(email, password)
        if (res.isSuccess) {
            res.getOrNull()?.let { notifyNewDeviceLogin(it) }
        }
        return res
    }

    suspend fun loginWithGoogle(
        email: String,
        firstName: String,
        lastName: String,
        avatarUrl: String? = null
    ): Result<User> {
        val res = userRepository.loginWithGoogle(email, firstName, lastName, avatarUrl)
        if (res.isSuccess) {
            res.getOrNull()?.let { notifyNewDeviceLogin(it) }
        }
        return res
    }

    suspend fun checkAuthentication(): Result<User> {
        return userRepository.checkAuthentication()
    }

    suspend fun isUserRegistered(email: String): Boolean {
        return userRepository.isUserRegistered(email)
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
        try {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                if (isDark) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            )
        } catch (_: Throwable) {}
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

    suspend fun linkReports(reportId1: String, reportId2: String): Result<Unit> {
        return reportRepository.linkReports(reportId1, reportId2)
    }

    fun matchReports(lostReport: Report, foundReport: Report) {
        scope.launch {
            reportRepository.linkReports(lostReport.id, foundReport.id)
            
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