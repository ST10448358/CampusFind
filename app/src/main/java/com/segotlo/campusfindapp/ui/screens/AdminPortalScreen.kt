package com.segotlo.campusfindapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segotlo.campusfindapp.data.*
import com.segotlo.campusfindapp.ui.components.UserAvatar
import com.segotlo.campusfindapp.ui.theme.*

enum class AdminTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    REPORTS("Reports", Icons.AutoMirrored.Filled.Assignment),
    MATCHES("Matches", Icons.Default.Share),
    USERS("Users", Icons.Default.People),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun AdminPortalScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onStudentView: () -> Unit = {},
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    Scaffold(
        topBar = {
            AdminTopHeader(
                currentTab = currentTab,
                onTabSelect = { currentTab = it },
                onLogout = onLogout
            )
        },
        bottomBar = {
            AdminBottomNavBar(
                currentTab = currentTab,
                onTabSelect = { currentTab = it }
            )
        },
        containerColor = DeepNavy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DeepNavy)
        ) {
            when (currentTab) {
                AdminTab.DASHBOARD -> AdminDashboardTab(
                    onNavigateToMatches = { currentTab = AdminTab.MATCHES },
                    onNavigateToReports = { currentTab = AdminTab.REPORTS }
                )
                AdminTab.REPORTS -> AdminReportsTab()
                AdminTab.MATCHES -> AdminMatchesTab()
                AdminTab.USERS -> AdminUsersTab()
                AdminTab.PROFILE -> AdminProfileTab(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun AdminTopHeader(
    currentTab: AdminTab,
    onTabSelect: (AdminTab) -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = SurfaceNavy,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                com.segotlo.campusfindapp.ui.components.CampusFindLogo(
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    val titleText = when (currentTab) {
                        AdminTab.DASHBOARD -> "CampusFind"
                        AdminTab.REPORTS -> "Reports"
                        AdminTab.MATCHES -> "Matches"
                        AdminTab.USERS -> "Users"
                        AdminTab.PROFILE -> "Settings"
                    }
                    Text(titleText, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("ADMIN", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentTab != AdminTab.DASHBOARD) {
                    IconButton(
                        onClick = { onTabSelect(AdminTab.DASHBOARD) },
                        modifier = Modifier.size(32.dp).background(CardNavy, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                IconButton(onClick = onLogout, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AdminBottomNavBar(
    currentTab: AdminTab,
    onTabSelect: (AdminTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceNavy,
        tonalElevation = 8.dp,
        modifier = Modifier.border(BorderStroke(1.dp, BorderColor))
    ) {
        AdminTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) TealAccent else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TealAccent else TextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealAccent,
                    selectedTextColor = TealAccent,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun AdminDashboardTab(
    onNavigateToMatches: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val reports by MockRepository.reports.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Dashboard", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Overview of CampusFind activity", color = TextSecondary, fontSize = 14.sp)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricCard(title = "Pending", count = reports.count { it.status == ReportStatus.OPEN }, icon = Icons.Default.Schedule, iconColor = AccentAmber, modifier = Modifier.weight(1f), onClick = onNavigateToReports)
                    AdminMetricCard(title = "Lost", count = reports.count { it.type == ReportType.LOST }, icon = Icons.Default.Inventory, iconColor = AccentRed, modifier = Modifier.weight(1f), onClick = onNavigateToReports)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminMetricCard(title = "Found", count = reports.count { it.type == ReportType.FOUND }, icon = Icons.Default.CheckCircle, iconColor = AccentGreen, modifier = Modifier.weight(1f), onClick = onNavigateToReports)
                    AdminMetricCard(title = "Matches", count = reports.count { it.status == ReportStatus.MATCH_SUGGESTED }, icon = Icons.Default.Share, iconColor = AccentPurple, modifier = Modifier.weight(1f), onClick = onNavigateToMatches)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth(0.5f)) {
                    AdminMetricCard(title = "Returned", count = reports.count { it.status == ReportStatus.RETURNED }, icon = Icons.Default.CheckCircle, iconColor = AccentSkyBlue, modifier = Modifier.weight(1f), onClick = onNavigateToReports)
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMatches() },
                shape = RoundedCornerShape(16.dp),
                color = AccentPurple.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val suggestedCount = reports.count { it.status == ReportStatus.MATCH_SUGGESTED }
                        Text("$suggestedCount possible matches awaiting review", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Review and confirm or reject the identified matches.", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Review now ↗", color = TealAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Reports", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("View all", color = TealAccent, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onNavigateToReports() })
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                if (reports.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No reports registered in the system yet.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reports.take(5)) { report ->
                            Surface(
                                modifier = Modifier
                                    .width(200.dp)
                                    .fillMaxHeight()
                                    .clickable { onNavigateToReports() },
                                shape = RoundedCornerShape(12.dp),
                                color = DeepNavy,
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(report.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(report.type.name, color = if (report.type == ReportType.LOST) AccentRed else AccentGreen, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = CardNavy,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(count.toString(), color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(title, color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun AdminReportsTab() {
    val reports by MockRepository.reports.collectAsState()
    var selectedReportIdForMatch by remember { mutableStateOf<String?>(null) }
    var currentFilter by remember { mutableStateOf("All Reports") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredReports = remember(reports, currentFilter, searchQuery) {
        reports.filter { report ->
            val matchesFilter = when (currentFilter) {
                "Pending" -> report.status == ReportStatus.OPEN
                "Lost" -> report.type == ReportType.LOST
                "Found" -> report.type == ReportType.FOUND
                "Returned" -> report.status == ReportStatus.RETURNED || report.status == ReportStatus.CLAIMED
                else -> true
            }

            val query = searchQuery.trim()
            val matchesSearch = query.isEmpty() ||
                    report.title.contains(query, ignoreCase = true) ||
                    report.description.contains(query, ignoreCase = true) ||
                    report.referenceCode.contains(query, ignoreCase = true) ||
                    report.userDisplayName.contains(query, ignoreCase = true) ||
                    report.userEmail.contains(query, ignoreCase = true) ||
                    report.specificLocationDetail.contains(query, ignoreCase = true) ||
                    report.category.name.contains(query, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }
    
    val liveSelectedReport = remember(selectedReportIdForMatch, reports) {
        reports.find { it.id == selectedReportIdForMatch }
    }

    if (liveSelectedReport != null) {
        MatchReportDialog(
            sourceReport = liveSelectedReport,
            allReports = reports,
            onDismiss = { selectedReportIdForMatch = null },
            onMatchConfirmed = { otherReport ->
                MockRepository.matchReports(
                    lostReport = if (liveSelectedReport.type == ReportType.LOST) liveSelectedReport else otherReport,
                    foundReport = if (liveSelectedReport.type == ReportType.FOUND) liveSelectedReport else otherReport
                )
                selectedReportIdForMatch = null
            },
            onUnmatchRequested = { report, otherId ->
                MockRepository.unmatchReports(report, otherId)
                selectedReportIdForMatch = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardNavy,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Column {
                    Text("Report Management", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Review incoming student submissions.", color = TextSecondary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search reports by title, code, reporter, location...", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { AdminFilterPill(label = "All Reports", count = reports.size, isActive = currentFilter == "All Reports", onClick = { currentFilter = "All Reports" }) }
                    item { AdminFilterPill(label = "Pending", count = reports.count { it.status == ReportStatus.OPEN }, isActive = currentFilter == "Pending", onClick = { currentFilter = "Pending" }) }
                    item { AdminFilterPill(label = "Lost", count = reports.count { it.type == ReportType.LOST }, isActive = currentFilter == "Lost", onClick = { currentFilter = "Lost" }) }
                    item { AdminFilterPill(label = "Found", count = reports.count { it.type == ReportType.FOUND }, isActive = currentFilter == "Found", onClick = { currentFilter = "Found" }) }
                    item { AdminFilterPill(label = "Returned", count = reports.count { it.status == ReportStatus.RETURNED || it.status == ReportStatus.CLAIMED }, isActive = currentFilter == "Returned", onClick = { currentFilter = "Returned" }) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Surface(
                modifier = Modifier.width(800.dp),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Item & Identifier", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
                    Text("Type", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                    Text("Category", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                    Text("Location & Date", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
                    Text("Reporter", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                    Text("Status", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                    Text("Actions", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                }
            }

            Surface(
                modifier = Modifier.width(800.dp),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = CardNavy.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                if (filteredReports.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Text("No reports match this query.", color = TextSecondary, fontSize = 14.sp)
                    }
                } else {
                    Column {
                        filteredReports.forEach { report ->
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(report.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
                                Box(modifier = Modifier.width(80.dp)) {
                                    Text(report.type.name, color = if (report.type == ReportType.LOST) AccentRed else AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(report.category.name, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(120.dp))
                                Column(modifier = Modifier.width(150.dp)) {
                                    val displayLoc = if (report.specificLocationDetail.isNotBlank()) {
                                        report.specificLocationDetail
                                    } else {
                                        report.location.name.replace("_", " ")
                                    }
                                    Text(displayLoc, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Text(report.institution, color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    @Suppress("DEPRECATION")
                                    Text(report.dateOccurred.toLocaleString(), color = TextSecondary, fontSize = 10.sp)
                                }
                                Text(report.userDisplayName, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(120.dp))
                                Box(modifier = Modifier.width(100.dp)) {
                                    val statusColor = when(report.status) {
                                        ReportStatus.OPEN -> TealAccent
                                        ReportStatus.MATCH_SUGGESTED -> AccentPurple
                                        ReportStatus.CLAIMED, ReportStatus.RETURNED -> AccentGreen
                                        else -> TextSecondary
                                    }
                                    Text(report.status.name, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.width(80.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    if (report.status == ReportStatus.OPEN || report.status == ReportStatus.MATCH_SUGGESTED) {
                                        IconButton(
                                            onClick = { selectedReportIdForMatch = report.id },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (report.status == ReportStatus.OPEN) Icons.Default.Link else Icons.Default.Edit, 
                                                contentDescription = "Match", 
                                                tint = if (report.status == ReportStatus.OPEN) TealAccent else AccentPurple, 
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFilterPill(label: String, count: Int, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) TealPrimary else CardNavy)
            .border(1.dp, if (isActive) Color.Transparent else BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = if (isActive) TextPrimary else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isActive) Color.White.copy(alpha = 0.2f) else DeepNavy)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(count.toString(), color = if (isActive) TextPrimary else TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AdminMatchesTab() {
    val reports by MockRepository.reports.collectAsState()
    var currentFilter by remember { mutableStateOf("Pending Review") }
    
    val pairs = remember(reports, currentFilter) {
        val matchedIds = mutableSetOf<String>()
        val result = mutableListOf<Pair<Report, Report>>()
        
        // Determine which status to filter by based on the tab
        val statusToFilter = when(currentFilter) {
            "Confirmed" -> ReportStatus.CLAIMED
            else -> ReportStatus.MATCH_SUGGESTED
        }

        reports.filter { it.type == ReportType.LOST && it.status == statusToFilter }.forEach { lost ->
            val found = if (lost.matchedWithId != null) {
                reports.find { it.id == lost.matchedWithId }
            } else {
                reports.find { 
                    it.type == ReportType.FOUND && 
                    it.status == statusToFilter && 
                    it.category == lost.category &&
                    !matchedIds.contains(it.id)
                }
            }
            
            if (found != null) {
                result.add(lost to found)
                matchedIds.add(lost.id)
                matchedIds.add(found.id)
            }
        }
        result
    }

    var selectedPairForReview by remember { mutableStateOf<Pair<Report, Report>?>(null) }

    if (selectedPairForReview != null) {
        MatchReviewDialog(
            lostReport = selectedPairForReview!!.first,
            foundReport = selectedPairForReview!!.second,
            onDismiss = { selectedPairForReview = null },
            onConfirm = {
                MockRepository.confirmMatch(selectedPairForReview!!.first, selectedPairForReview!!.second)
                selectedPairForReview = null
            },
            onReject = {
                MockRepository.unmatchReports(selectedPairForReview!!.first, selectedPairForReview!!.second.id)
                selectedPairForReview = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardNavy,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Assisted Bridge", color = AccentPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Matches Evaluation", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Review potential lost-and-found matches side-by-side.", color = TextSecondary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { AdminFilterPillPurple(label = "Pending Review", count = reports.count { it.status == ReportStatus.MATCH_SUGGESTED && it.type == ReportType.LOST }, isActive = currentFilter == "Pending Review", onClick = { currentFilter = "Pending Review" }) }
                    item { AdminFilterPillPurple(label = "Confirmed", count = reports.count { it.status == ReportStatus.CLAIMED && it.type == ReportType.LOST }, isActive = currentFilter == "Confirmed", onClick = { currentFilter = "Confirmed" }) }
                    item { AdminFilterPillPurple(label = "Rejected", count = 0, isActive = currentFilter == "Rejected", onClick = { currentFilter = "Rejected" }) }
                }
            }
        }

        if (pairs.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardNavy.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, AccentGreen.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No Matches found", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Select items in the Reports tab to create matches.", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pairs.forEach { (lost, found) ->
                    AdminMatchPairCard(
                        lostReport = lost,
                        foundReport = found,
                        onReview = { selectedPairForReview = lost to found }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminMatchPairCard(lostReport: Report, foundReport: Report, onReview: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardNavy,
        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Potential Match: ${lostReport.title}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Category: ${lostReport.category.name}", color = TextSecondary, fontSize = 12.sp)
                }
                Button(
                    onClick = onReview,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MatchMiniInfo(title = "LOST BY", name = lostReport.userDisplayName, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(24.dp).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                MatchMiniInfo(title = "FOUND BY", name = foundReport.userDisplayName, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MatchMiniInfo(title: String, name: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun AdminFilterPillPurple(label: String, count: Int, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) AccentPurple else CardNavy)
            .border(1.dp, if (isActive) Color.Transparent else BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = if (isActive) TextPrimary else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isActive) Color.White.copy(alpha = 0.2f) else DeepNavy)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(count.toString(), color = if (isActive) TextPrimary else TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AdminUsersTab() {
    val users by MockRepository.getAllUsers().collectAsState()
    val allReports by MockRepository.reports.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val filteredUsers = remember(users, searchQuery, selectedRoleFilter, selectedStatusFilter) {
        users.filter { user ->
            val matchesSearch = searchQuery.isBlank() ||
                    user.firstName.contains(searchQuery, ignoreCase = true) ||
                    user.lastName.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true) ||
                    user.studentStaffNumber.contains(searchQuery, ignoreCase = true)

            val matchesRole = when (selectedRoleFilter) {
                "Students" -> user.role == UserRole.STUDENT
                "Admins" -> user.role == UserRole.ADMIN
                else -> true
            }

            val matchesStatus = when (selectedStatusFilter) {
                "Active" -> user.accountStatus == AccountStatus.ACTIVE
                "Inactive" -> user.accountStatus == AccountStatus.SUSPENDED
                else -> true
            }

            matchesSearch && matchesRole && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardNavy,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Column {
                    Text("User Directory", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Review student and staff accounts.", color = TextSecondary, fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search users by name, email, student/staff ID...", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LazyRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { Text("Role:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                        item {
                            AdminFilterPillCompact(
                                label = "All",
                                count = users.size,
                                isActive = selectedRoleFilter == "All",
                                onClick = { selectedRoleFilter = "All" }
                            )
                        }
                        item {
                            AdminFilterPillCompact(
                                label = "Students",
                                count = users.count { it.role == UserRole.STUDENT },
                                isActive = selectedRoleFilter == "Students",
                                onClick = { selectedRoleFilter = "Students" }
                            )
                        }
                        item {
                            AdminFilterPillCompact(
                                label = "Admins",
                                count = users.count { it.role == UserRole.ADMIN },
                                isActive = selectedRoleFilter == "Admins",
                                onClick = { selectedRoleFilter = "Admins" }
                            )
                        }
                    }
                    LazyRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { Text("Status:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                        item {
                            AdminFilterPillCompact(
                                label = "All",
                                count = null,
                                isActive = selectedStatusFilter == "All",
                                onClick = { selectedStatusFilter = "All" }
                            )
                        }
                        item {
                            AdminFilterPillCompact(
                                label = "Active",
                                count = users.count { it.accountStatus == AccountStatus.ACTIVE },
                                isActive = selectedStatusFilter == "Active",
                                onClick = { selectedStatusFilter = "Active" }
                            )
                        }
                        item {
                            AdminFilterPillCompact(
                                label = "Inactive",
                                count = users.count { it.accountStatus == AccountStatus.SUSPENDED },
                                isActive = selectedStatusFilter == "Inactive",
                                onClick = { selectedStatusFilter = "Inactive" }
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Surface(
                modifier = Modifier.width(800.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceNavy)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("User", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(180.dp))
                        Text("ID", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text("Department", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(180.dp))
                        Text("Role", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                        Text("Reports", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                        Text("Status", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                    }

                    filteredUsers.forEach { user ->
                        val userReportCount = allReports.count { it.userId == user.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.width(180.dp), verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(imageUrl = user.avatarUrl, size = 32.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("${user.firstName} ${user.lastName}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(user.email, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Text(user.studentStaffNumber, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                            Text(user.departmentOrFaculty, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(180.dp))
                            Box(modifier = Modifier.width(80.dp)) {
                                val isAdmin = user.role == UserRole.ADMIN
                                val roleColor = if (isAdmin) AccentPurple else TextSecondary
                                Text(if (isAdmin) "Admin" else "Student", color = roleColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(userReportCount.toString(), color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                            Box(modifier = Modifier.width(100.dp)) {
                                val isActive = user.accountStatus == AccountStatus.ACTIVE
                                Text(if (isActive) "Active" else "Suspended", color = if (isActive) AccentGreen else AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFilterPillCompact(
    label: String,
    count: Int?,
    isActive: Boolean,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) BorderColor else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (count != null) "$label ($count)" else label,
            color = if (isActive) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AdminProfileTab(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val user by MockRepository.currentUser.collectAsState()

    var showPassword by remember { mutableStateOf(false) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var claimAlertsEnabled by remember { mutableStateOf(true) }
    var securitySyncEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English (Default)") }

    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfUse by remember { mutableStateOf(false) }
    var showHelpSupport by remember { mutableStateOf(false) }

    if (showPrivacyPolicy) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPrivacyPolicy = false }) {
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
        androidx.compose.ui.window.Dialog(onDismissRequest = { showTermsOfUse = false }) {
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
        androidx.compose.ui.window.Dialog(onDismissRequest = { showHelpSupport = false }) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("Admin Profile & Settings", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Account credentials, notifications and system preferences", color = TextSecondary, fontSize = 14.sp)
        }

        // Profile Details & Admin Credentials Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!user?.avatarUrl.isNullOrEmpty()) {
                            UserAvatar(imageUrl = user?.avatarUrl, size = 64.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(AccentPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(36.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${user?.firstName ?: "Admin"} ${user?.lastName ?: "User"}",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(AccentPurple, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("SYSTEM ADMINISTRATOR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("PROFILE DETAILS & CREDENTIALS", color = TealAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin Email", color = TextSecondary, fontSize = 11.sp)
                            Text(user?.email ?: "admin@campus.ac.za", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin Password", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                if (showPassword) "AdminPassword123!" else "••••••••••••••",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = TealAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Staff ID Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin Staff ID", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                "#${user?.studentStaffNumber?.ifEmpty { "ADMIN-001" } ?: "ADMIN-001"}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // App Preferences & Theme Section
        item {
            Text("APP PREFERENCES & THEME", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Mode", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(if (isDarkMode) "Dark theme active" else "Light theme active", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealAccent)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("REST API Connectivity", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Connected. Synced with Campus REST API backend.", color = TextSecondary, fontSize = 12.sp)
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
                }
            }
        }

        // Notification Settings Section
        item {
            Text("NOTIFICATION SETTINGS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin Push Notifications", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Alerts for new system activity", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = { pushNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealAccent)
                        )
                    }
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("New Claim & Match Alerts", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Notify when claims are submitted", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = claimAlertsEnabled,
                            onCheckedChange = { claimAlertsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealAccent)
                        )
                    }
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Security Desk Instant Sync", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Auto-update verification statuses", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = securitySyncEnabled,
                            onCheckedChange = { securitySyncEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealAccent)
                        )
                    }
                }
            }
        }

        // Language Preferences Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LANGUAGE PREFERENCES", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("SA LANGUAGES", color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val languages = listOf("English (Default)" to null, "Setswana" to "Batswana", "isiZulu" to "AmaZulu")
                    languages.forEachIndexed { index, (lang, badge) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLanguage = lang }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = (selectedLanguage == lang),
                                    onClick = { selectedLanguage = lang },
                                    colors = RadioButtonDefaults.colors(selectedColor = TealAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lang, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                if (badge != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TealAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(badge, color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        if (index < languages.size - 1) {
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Privacy & Security Section
        item {
            Text("PRIVACY & SECURITY", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyPolicy = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Privacy Policy", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermsOfUse = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Terms of Use", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHelpSupport = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Campus Security & Support", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Actions Section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() },
                shape = RoundedCornerShape(16.dp),
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
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = AccentRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                "CampusFind Admin Portal v1.2.0\nAuthorized personnel only.",
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun AdminStatRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardNavy,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = TextPrimary, fontSize = 14.sp)
            Text(value, color = TealAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminActionRow(title: String, icon: ImageVector, color: Color = TextPrimary, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = color, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun MatchReportDialog(
    sourceReport: Report,
    allReports: List<Report>,
    onDismiss: () -> Unit,
    onMatchConfirmed: (Report) -> Unit,
    onUnmatchRequested: (Report, String) -> Unit
) {
    val isSuggested = sourceReport.status == ReportStatus.MATCH_SUGGESTED
    val matchedId = sourceReport.matchedWithId
    val oppositeType = if (sourceReport.type == ReportType.LOST) ReportType.FOUND else ReportType.LOST
    
    // BROADENED SEARCH: Show all reports of the opposite type
    val potentialMatches = allReports.filter { 
        it.type == oppositeType && 
        (it.status == ReportStatus.OPEN || it.id == matchedId)
    }.sortedByDescending { it.category == sourceReport.category } // Prioritize same category

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(if (isSuggested) "Edit Match for:" else "Find Match for:", color = TextPrimary, fontSize = 14.sp)
                Text(sourceReport.title, color = TealAccent, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                if (isSuggested) {
                    if (matchedId != null) {
                        val currentMatch = allReports.find { it.id == matchedId }
                        currentMatch?.let {
                            Text("CURRENT MATCH", color = AccentPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = AccentPurple.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(it.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        val currentLoc = if (it.specificLocationDetail.isNotBlank()) it.specificLocationDetail else it.location.name.replace("_", " ")
                                        Text("${it.userDisplayName} • $currentLoc (${it.institution})", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    TextButton(onClick = { onUnmatchRequested(sourceReport, it.id) }) {
                                        Text("Unlink", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = AccentAmber.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, AccentAmber)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("This item is marked as matched but the link is broken.", color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { MockRepository.updateReportStatus(sourceReport.id, ReportStatus.OPEN, null); onDismiss() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Reset to Open", color = Color.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                Text("ALL ${oppositeType.name} REPORTS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                val candidates = potentialMatches.filter { it.id != matchedId }
                
                if (candidates.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No other ${oppositeType.name.lowercase()} reports available.", color = TextSecondary, textAlign = TextAlign.Center, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(candidates) { report ->
                            val isRecommended = report.category == sourceReport.category
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onMatchConfirmed(report) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isRecommended) TealPrimary.copy(alpha = 0.05f) else CardNavy,
                                border = BorderStroke(1.dp, if (isRecommended) TealAccent.copy(alpha = 0.3f) else BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(report.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (isRecommended) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(modifier = Modifier.background(TealAccent, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                        Text("SAME CATEGORY", color = DeepNavy, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                            val candidateLoc = if (report.specificLocationDetail.isNotBlank()) report.specificLocationDetail else report.location.name.replace("_", " ")
                                            Text("${report.userDisplayName} • $candidateLoc (${report.institution})", color = TextSecondary, fontSize = 11.sp)
                                        }
                                        Icon(Icons.Default.AddLink, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                                    }
                                    if (report.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = report.description,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.background(DeepNavy.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(6.dp).fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = DeepNavy
    )
}

@Composable
fun MatchReviewDialog(
    lostReport: Report,
    foundReport: Report,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Match Pairing", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Confirm if these two reports describe the same item.", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReviewItemColumn(title = "LOST ITEM", report = lostReport, color = AccentRed, modifier = Modifier.weight(1f))
                    ReviewItemColumn(title = "FOUND ITEM", report = foundReport, color = AccentGreen, modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardNavy,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("MATCH QUALITY CHECK", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        MatchFactorRow(label = "Category Match", isMatch = lostReport.category == foundReport.category)
                        MatchFactorRow(label = "Institution Match", isMatch = lostReport.institution == foundReport.institution)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Approve Match", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("Reject Pairing", color = AccentRed)
            }
        },
        containerColor = DeepNavy
    )
}

@Composable
fun ReviewItemColumn(title: String, report: Report, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = DeepNavy,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(report.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(report.userDisplayName, color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("DESCRIPTION", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(report.description, color = TextPrimary, fontSize = 11.sp, maxLines = 3)
                Spacer(modifier = Modifier.height(8.dp))
                val reviewLoc = if (report.specificLocationDetail.isNotBlank()) report.specificLocationDetail else report.location.name.replace("_", " ")
                Text("LOCATION & INSTITUTION", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text("$reviewLoc (${report.institution})", color = TextPrimary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun MatchFactorRow(label: String, isMatch: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextPrimary, fontSize = 12.sp)
        Icon(
            imageVector = if (isMatch) Icons.Default.CheckCircle else Icons.Default.Cancel, 
            contentDescription = null, 
            tint = if (isMatch) AccentGreen else AccentRed,
            modifier = Modifier.size(16.dp)
        )
    }
}

