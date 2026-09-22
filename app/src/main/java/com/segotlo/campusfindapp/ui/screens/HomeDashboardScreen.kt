package com.segotlo.campusfindapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.ui.components.*
import com.segotlo.campusfindapp.ui.theme.*

@Composable
fun HomeDashboardScreen(
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onViewAllReports: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val user by MockRepository.currentUser.collectAsState()
    val allReports by MockRepository.reports.collectAsState()
    
    val userReports = remember(user, allReports) {
        allReports.filter { it.userId == user?.id }
    }

    Scaffold(
        topBar = { CampusTopBar() },
        bottomBar = { CampusBottomNavBar(currentRoute = "home", onNavigate = onNavigate) },
        containerColor = DeepNavy
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeHeader(
                    name = user?.let { "${it.firstName} ${it.lastName}" } ?: "Guest User",
                    id = user?.studentStaffNumber ?: "000000000",
                    avatarUrl = user?.avatarUrl
                )
                Spacer(modifier = Modifier.height(24.dp))
                SearchBar(placeholder = "Search lost & found reports...")
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(
                        title = "Report Lost",
                        subtitle = "I lost something",
                        icon = Icons.Default.HelpOutline,
                        gradient = Brush.verticalGradient(listOf(Color(0xFF4F46E5), Color(0xFF3730A3))),
                        onClick = onReportLost,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Report Found",
                        subtitle = "I found something",
                        icon = Icons.Default.AddCircleOutline,
                        gradient = Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF065F46))),
                        onClick = onReportFound,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recent Activity", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onViewAllReports) {
                        Text("See all", color = TealAccent, fontSize = 14.sp)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (userReports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(CardNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent reports in your area.", color = TextSecondary)
                    }
                }
            } else {
                items(userReports.take(3)) { report ->
                    ReportListItem(report = report)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DeepNavy,
                    border = BorderStroke(1.dp, TealAccent.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Need security verification? Visit Campus Main Gate Desk.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("Ext: 4400", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HomeHeader(name: String, id: String, avatarUrl: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("HELLO,", color = TealAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(name, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("ID: $id", color = TextSecondary, fontSize = 14.sp)
        }
        UserAvatar(imageUrl = avatarUrl, size = 56.dp)
    }
}

@Composable
fun SearchBar(placeholder: String) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextSecondary, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardNavy,
            unfocusedContainerColor = CardNavy,
            focusedBorderColor = TealAccent.copy(alpha = 0.5f),
            unfocusedBorderColor = BorderColor
        ),
        singleLine = true
    )
}

