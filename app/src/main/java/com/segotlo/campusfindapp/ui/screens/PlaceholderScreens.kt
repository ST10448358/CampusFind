package com.segotlo.campusfindapp.ui.screens

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.segotlo.campusfindapp.data.*
import com.segotlo.campusfindapp.ui.components.CampusBottomNavBar
import com.segotlo.campusfindapp.ui.components.CampusTextField
import com.segotlo.campusfindapp.ui.components.UserAvatar
import com.segotlo.campusfindapp.ui.components.CampusTopBar
import com.segotlo.campusfindapp.ui.components.ReportListItem
import com.segotlo.campusfindapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MyReportsScreen(onNavigate: (String) -> Unit) {
    val user by MockRepository.currentUser.collectAsState()
    val allReports by MockRepository.reports.collectAsState()
    
    val userReports = remember(user, allReports) {
        allReports.filter { it.userId == user?.id }
    }
    
    val lostReports = userReports.filter { it.type == ReportType.LOST && it.status != ReportStatus.RETURNED && it.status != ReportStatus.CLOSED }
    val foundReports = userReports.filter { it.type == ReportType.FOUND && it.status != ReportStatus.RETURNED && it.status != ReportStatus.CLOSED }
    val completedReports = userReports.filter { it.status == ReportStatus.RETURNED || it.status == ReportStatus.CLOSED }

    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    val reportsToShow = when(selectedTab) {
        0 -> lostReports
        1 -> foundReports
        else -> completedReports
    }

    Scaffold(
        topBar = { CampusTopBar() },
        bottomBar = { CampusBottomNavBar(currentRoute = "reports", onNavigate = onNavigate) },
        containerColor = DeepNavy
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Header Title and New Report Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Reports", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    
                    Box {
                        Button(
                            onClick = { showMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent.copy(alpha = 0.2f), contentColor = TealAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, TealAccent)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ + New Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .width(180.dp)
                                .background(CardNavy)
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentRed))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Report Lost Item", color = TextPrimary, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigate("report_lost")
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealAccent))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Report Found Item", color = TextPrimary, fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigate("report_found")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Capsule tab rows
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardNavy,
                    contentColor = TextPrimary,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color.White
                            )
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    val tabs = listOf(
                        "Lost (${lostReports.size})",
                        "Found (${foundReports.size})",
                        "Completed (${completedReports.size})"
                    )
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (reportsToShow.isEmpty()) {
                    // Central Dark Empty State Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = CardNavy.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(DeepNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                val heading = when(selectedTab) {
                                    0 -> "No lost reports yet"
                                    1 -> "No found reports yet"
                                    else -> "No completed reports yet"
                                }
                                Text(heading, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("If you have misplaced property on campus, report it here.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reportsToShow) { report ->
                            ReportListItem(report = report)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun NotificationsScreen(onNavigate: (String) -> Unit) {
    val notifications by MockRepository.notifications.collectAsState()

    Scaffold(
        topBar = { CampusTopBar() },
        bottomBar = { CampusBottomNavBar(currentRoute = "alerts", onNavigate = onNavigate) },
        containerColor = DeepNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Notifications", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            if (notifications.isEmpty()) {
                // White Box Empty State
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("You have no notifications right now.", color = Color(0xFF1F2937), fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Important updates about your lost and found reports will appear here.", color = Color(0xFF6B7280), fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notifications) { notification ->
                        NotificationItem(notification = notification, onClick = {
                            MockRepository.markNotificationAsRead(notification.id)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: AppNotification, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) CardNavy.copy(alpha = 0.6f) else CardNavy,
        border = BorderStroke(1.dp, if (notification.isRead) BorderColor.copy(alpha = 0.5f) else TealAccent.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (notification.isRead) DeepNavy else TealPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null, 
                    tint = if (notification.isRead) TextSecondary else TealAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.message, color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(notification.timestamp.toLocaleString(), color = BorderColor, fontSize = 10.sp)
            }
            if (!notification.isRead) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealAccent))
            }
        }
    }
}

@Composable
fun ProfileSettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val user by MockRepository.currentUser.collectAsState()
    val allReports by MockRepository.reports.collectAsState()
    
    val userReports = remember(user, allReports) {
        allReports.filter { it.userId == user?.id }
    }
    
    val lostCount = userReports.count { it.type == ReportType.LOST && it.status != ReportStatus.RETURNED && it.status != ReportStatus.CLOSED }
    val foundCount = userReports.count { it.type == ReportType.FOUND && it.status != ReportStatus.RETURNED && it.status != ReportStatus.CLOSED }
    val returnedCount = userReports.count { it.status == ReportStatus.RETURNED || it.status == ReportStatus.CLOSED }

    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfUse by remember { mutableStateOf(false) }
    var showHelpSupport by remember { mutableStateOf(false) }
    var showPhotoPickerDialog by remember { mutableStateOf(false) }

    // State for Edit Profile fields
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editStudentNumber by remember { mutableStateOf("") }

    // State for Change Password fields
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        user?.let { currentUser ->
            MockRepository.updateUser(currentUser.copy(pushNotificationsEnabled = isGranted))
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedAvatarUrl = saveBitmapToLocalFile(context, bitmap, user?.id)
            if (savedAvatarUrl != null) {
                user?.let { currentUser ->
                    MockRepository.updateUser(currentUser.copy(avatarUrl = savedAvatarUrl))
                }
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun launchCameraSafely() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val savedAvatarUrl = saveBitmapToLocalFile(context, bitmap, user?.id)
                    if (savedAvatarUrl != null) {
                        user?.let { currentUser ->
                            MockRepository.updateUser(currentUser.copy(avatarUrl = savedAvatarUrl))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Update local state when dialog opens
    LaunchedEffect(showEditProfile) {
        if (showEditProfile) {
            editFirstName = user?.firstName ?: ""
            editLastName = user?.lastName ?: ""
            editEmail = user?.email ?: ""
            editStudentNumber = user?.studentStaffNumber ?: ""
        }
    }

    LaunchedEffect(showChangePassword) {
        if (showChangePassword) {
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
            passwordErrorMessage = null
        }
    }

    if (showPhotoPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPickerDialog = false },
            title = { Text("Profile Picture Options", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Choose how you would like to update your profile photo:", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Take Photo
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoPickerDialog = false
                                launchCameraSafely()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CardNavy,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TealAccent)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Take Photo (Camera)", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Upload Photo / Gallery
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoPickerDialog = false
                                galleryLauncher.launch("image/*")
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CardNavy,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AccentSkyBlue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Upload Photo (Gallery)", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (!user?.avatarUrl.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Option 3: Remove Photo
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhotoPickerDialog = false
                                    user?.let { currentUser ->
                                        MockRepository.updateUser(currentUser.copy(avatarUrl = ""))
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = CardNavy,
                            border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Remove Photo", color = AccentRed, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoPickerDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardNavy
        )
    }

    if (showEditProfile) {
        Dialog(onDismissRequest = { showEditProfile = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Edit Profile", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showEditProfile = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CampusTextField(
                            value = editFirstName, onValueChange = { editFirstName = it }, label = "First name", modifier = Modifier.weight(1f)
                        )
                        CampusTextField(
                            value = editLastName, onValueChange = { editLastName = it }, label = "Last name", modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CampusTextField(
                        value = editEmail, onValueChange = { editEmail = it }, label = "Email address"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampusTextField(
                        value = editStudentNumber, onValueChange = { editStudentNumber = it }, label = "Student / staff number"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showEditProfile = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("Cancel", color = TextPrimary)
                        }
                        Button(
                            onClick = { 
                                user?.let {
                                    MockRepository.updateUser(
                                        it.copy(
                                            firstName = editFirstName,
                                            lastName = editLastName,
                                            email = editEmail,
                                            studentStaffNumber = editStudentNumber
                                        )
                                    )
                                }
                                showEditProfile = false 
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showChangePassword) {
        Dialog(onDismissRequest = { showChangePassword = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Change Password", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showChangePassword = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (passwordErrorMessage != null) {
                        Text(
                            text = passwordErrorMessage!!,
                            color = AccentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    CampusTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; passwordErrorMessage = null },
                        label = "Current Password",
                        placeholder = "Enter current password",
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampusTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordErrorMessage = null },
                        label = "New Password",
                        placeholder = "Min 8 characters",
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampusTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordErrorMessage = null },
                        label = "Confirm New Password",
                        placeholder = "Re-enter new password",
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showChangePassword = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("Cancel", color = TextPrimary)
                        }
                        Button(
                            onClick = {
                                if (currentPassword.isEmpty()) {
                                    passwordErrorMessage = "Please enter your current password"
                                } else if (newPassword.length < 8) {
                                    passwordErrorMessage = "New password must be at least 8 characters"
                                } else if (newPassword != confirmPassword) {
                                    passwordErrorMessage = "New passwords do not match"
                                } else {
                                    showChangePassword = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Update Password", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showPrivacyPolicy) {
        Dialog(onDismissRequest = { showPrivacyPolicy = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Privacy Policy", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "1. Data Protection: Personal contact numbers, residential room numbers, and financial details are never publicly visible on lost or found reports.\n\n2. Ownership Claim Privacy: Verification proofs (e.g. device unlock passwords, serial numbers, concealed scratches) are accessible only to university security desks for cross-referencing.\n\n3. Retention: Reports are archived 90 days after successful completion or resolution.",
                        color = TextSecondary, fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showPrivacyPolicy = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showTermsOfUse) {
        Dialog(onDismissRequest = { showTermsOfUse = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Terms of Use", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "1. University Property Code: All found items of value must be reported or handed in to the Campus Main Gate Security Desk within 48 hours.\n\n2. False Claims: Submitting false ownership claims or fictitious reports violates the university student code of conduct.\n\n3. Handover Safety: All physical exchanges should occur at designated well-lit campus security points.",
                        color = TextSecondary, fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showTermsOfUse = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showHelpSupport) {
        Dialog(onDismissRequest = { showHelpSupport = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Help & Campus Support", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().background(DeepNavy, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(16.dp)
                    ) {
                        Column {
                            Text("Campus Security Operations", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Operating 24/7 during academic terms", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Campus Security Desk Ext: 4400", color = TealAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showHelpSupport = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { CampusTopBar() },
        bottomBar = { CampusBottomNavBar(currentRoute = "profile", onNavigate = onNavigate) },
        containerColor = DeepNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Header & Credentials Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
                        UserAvatar(imageUrl = user?.avatarUrl, size = 80.dp)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(TealPrimary)
                                .clickable { showPhotoPickerDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Change profile picture", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("${user?.firstName ?: "Student"} ${user?.lastName ?: "User"}", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Student #${user?.studentStaffNumber ?: "N/A"}", color = TealAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // PROFILE DETAILS & CREDENTIALS
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("PROFILE DETAILS & CREDENTIALS", color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Email Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Email Address", color = TextSecondary, fontSize = 11.sp)
                                Text(user?.email ?: "student@campus.ac.za", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Student Number Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Student / Staff ID", color = TextSecondary, fontSize = 11.sp)
                                Text("#${user?.studentStaffNumber ?: "220031847"}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Institution Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Campus / Institution", color = TextSecondary, fontSize = 11.sp)
                                Text(user?.institution?.ifEmpty { "University of Pretoria" } ?: "University of Pretoria", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }



            // ACCOUNT SECTION
            ProfileSectionHeader("ACCOUNT")
            ProfileRowItem(title = "Edit Profile", onClick = { showEditProfile = true })
            ProfileRowItem(title = "Change Password", onClick = { showChangePassword = true })

            Spacer(modifier = Modifier.height(24.dp))

            // NOTIFICATIONS SECTION
            ProfileSectionHeader("NOTIFICATIONS")
            ProfileSwitchRow(
                title = "Push Notifications",
                subTitle = if (user?.pushNotificationsEnabled == true) "Enabled for campus alerts" else "Disabled",
                checked = user?.pushNotificationsEnabled ?: true,
                onCheckedChange = { isChecked ->
                    if (isChecked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        if (!com.segotlo.campusfindapp.util.NotificationHelper.hasNotificationPermission(context)) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            return@ProfileSwitchRow
                        }
                    }
                    user?.let { currentUser ->
                        MockRepository.updateUser(currentUser.copy(pushNotificationsEnabled = isChecked))
                    }
                }
            )
            ProfileSwitchRow(
                title = "Possible Match Alerts",
                subTitle = if (user?.matchAlertsEnabled == true) "Notify on found item matches" else "Alerts disabled",
                checked = user?.matchAlertsEnabled ?: true,
                onCheckedChange = { isChecked ->
                    user?.let { currentUser ->
                        MockRepository.updateUser(currentUser.copy(matchAlertsEnabled = isChecked))
                    }
                }
            )
            ProfileSwitchRow(
                title = "Report Updates",
                subTitle = if (user?.reportUpdatesEnabled == true) "Notify when report status changes" else "Updates disabled",
                checked = user?.reportUpdatesEnabled ?: true,
                onCheckedChange = { isChecked ->
                    user?.let { currentUser ->
                        MockRepository.updateUser(currentUser.copy(reportUpdatesEnabled = isChecked))
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // LANGUAGE SECTION
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                ProfileSectionHeader("LANGUAGE")
                Text("SA LANGUAGES", color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ProfileRadioRow(title = "English (Default)", selected = true, badge = null)
            ProfileRadioRow(title = "Setswana", selected = false, badge = "Batswana")
            ProfileRadioRow(title = "isiZulu", selected = false, badge = "AmaZulu")

            Spacer(modifier = Modifier.height(24.dp))

            // APP PREFERENCES SECTION
            ProfileSectionHeader("APP PREFERENCES")
            ProfileSwitchRow(title = "Dark Mode", subTitle = "Dark theme active", checked = isDarkMode, onCheckedChange = onToggleDarkMode)
            
            // Network Connectivity box row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Network Connectivity", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Connected. Automatic synchronization with Campus REST API enabled.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentGreen.copy(alpha = 0.15f))
                        .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Online", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PRIVACY & SECURITY SECTION
            ProfileSectionHeader("PRIVACY & SECURITY")
            ProfileRowItem(title = "Privacy Policy", onClick = { showPrivacyPolicy = true })
            ProfileRowItem(title = "Terms of Use", onClick = { showTermsOfUse = true })
            ProfileRowItem(title = "Help & Campus Support", onClick = { showHelpSupport = true })



            // Log Out Button Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() },
                shape = RoundedCornerShape(12.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", color = AccentRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("CampusFind v1.0.0", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileRowItem(title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
    Divider(color = BorderColor.copy(alpha = 0.5f))
}

@Composable
fun ProfileSwitchRow(title: String, subTitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (subTitle != null) {
                Text(subTitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealAccent)
        )
    }
    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
}

@Composable
fun ProfileRadioRow(title: String, selected: Boolean, badge: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TealAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        RadioButton(
            selected = selected,
            onClick = {},
            colors = RadioButtonDefaults.colors(selectedColor = AccentSkyBlue, unselectedColor = TextSecondary)
        )
    }
    Divider(color = BorderColor.copy(alpha = 0.5f))
}

private fun saveBitmapToLocalFile(context: android.content.Context, bitmap: android.graphics.Bitmap, userId: String?): String? {
    return try {
        val file = java.io.File(context.filesDir, "avatar_${userId ?: "user"}_${System.currentTimeMillis()}.png")
        val outputStream = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        file.toURI().toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
