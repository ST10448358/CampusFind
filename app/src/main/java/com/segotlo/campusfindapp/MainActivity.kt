package com.segotlo.campusfindapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.ui.screens.*
import com.segotlo.campusfindapp.ui.theme.CampusFindTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MockRepository.init(applicationContext)
        com.segotlo.campusfindapp.util.NotificationHelper.createNotificationChannels(applicationContext)
        setContent {
            val isDarkThemeRepo by MockRepository.isDarkTheme.collectAsState()
            var isDarkMode by remember { mutableStateOf(isDarkThemeRepo) }

            LaunchedEffect(isDarkThemeRepo) {
                isDarkMode = isDarkThemeRepo
            }

            CampusFindTheme(darkTheme = isDarkMode) {
                CampusFindApp(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { newMode ->
                        isDarkMode = newMode
                        MockRepository.setDarkTheme(newMode)
                    }
                )
            }
        }
    }
}

@Composable
fun CampusFindApp(isDarkMode: Boolean, onToggleDarkMode: (Boolean) -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "opening") {
        composable("opening") {
            OpeningScreen(
                onGetStarted = { navController.navigate("register") },
                onLogin = { navController.navigate("login") }
            )
        }
        composable("login") {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = { email, role ->
                    if (role == com.segotlo.campusfindapp.data.UserRole.ADMIN) {
                        navController.navigate("admin") {
                            popUpTo("opening") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("opening") { inclusive = true }
                        }
                    }
                },
                onRegister = {
                    navController.navigate("register") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            val scope = rememberCoroutineScope()
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onLoginNav = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onRegisterSuccess = { firstName, lastName, email, password, studentNumber, institution, avatarUrl ->
                    scope.launch {
                        val result = MockRepository.register(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = password,
                            studentNumber = studentNumber,
                            institution = institution,
                            avatarUrl = avatarUrl
                        )
                        if (result.isSuccess) {
                            navController.navigate("home") {
                                popUpTo("opening") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        composable("home") {
            HomeDashboardScreen(
                onReportLost = { navController.navigate("report_lost") },
                onReportFound = { navController.navigate("report_found") },
                onViewAllReports = { navController.navigate("search") },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("report_lost") {
            ReportLostScreen(
                onClose = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }
        composable("report_found") {
            ReportFoundScreen(
                onClose = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }
        composable("search") {
            SearchScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("reports") {
            MyReportsScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("alerts") {
            NotificationsScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("profile") {
            ProfileSettingsScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    MockRepository.logout()
                    navController.navigate("login") {
                        popUpTo("opening") { inclusive = true }
                    }
                }
            )
        }
        composable("admin") {
            AdminPortalScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onStudentView = {
                    navController.navigate("home")
                },
                onLogout = {
                    MockRepository.logout()
                    navController.navigate("login") {
                        popUpTo("opening") { inclusive = true }
                    }
                }
            )
        }
    }
}
