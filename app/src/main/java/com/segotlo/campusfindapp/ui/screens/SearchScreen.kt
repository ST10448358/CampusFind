package com.segotlo.campusfindapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.segotlo.campusfindapp.data.ItemCategory
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.ReportType
import com.segotlo.campusfindapp.ui.components.*
import com.segotlo.campusfindapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigate: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Lost, 2: Found
    var selectedInstitutionFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf<ItemCategory?>(null) }
    var showFilterModal by remember { mutableStateOf(false) }
    var isMapView by remember { mutableStateOf(false) }
    var selectedReportForDetail by remember { mutableStateOf<com.segotlo.campusfindapp.data.Report?>(null) }

    val allReports by MockRepository.reports.collectAsState()

    // Filter reports according to tab selection, search query, and filters
    val filteredReports = remember(allReports, searchQuery, selectedTabIndex, selectedInstitutionFilter, selectedCategoryFilter) {
        allReports.filter { report ->
            // Tab filter
            val matchesTab = when (selectedTabIndex) {
                1 -> report.type == ReportType.LOST
                2 -> report.type == ReportType.FOUND
                else -> true
            }

            // Text search query
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    report.title.lowercase().contains(query) ||
                    report.description.lowercase().contains(query) ||
                    report.institution.lowercase().contains(query) ||
                    report.referenceCode.lowercase().contains(query) ||
                    report.specificLocationDetail.lowercase().contains(query) ||
                    report.category.name.lowercase().contains(query)

            // Institution filter
            val matchesInstitution = selectedInstitutionFilter == "All" ||
                    report.institution.contains(selectedInstitutionFilter, ignoreCase = true)

            // Category filter
            val matchesCategory = selectedCategoryFilter == null || report.category == selectedCategoryFilter

            matchesTab && matchesQuery && matchesInstitution && matchesCategory
        }
    }

    Scaffold(
        topBar = { CampusTopBar() },
        bottomBar = { CampusBottomNavBar(currentRoute = "search", onNavigate = onNavigate) },
        containerColor = DeepNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Screen Heading
            Text(
                text = "Search Reports",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar + Filter Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Search for an item...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary
                                )
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardNavy,
                        unfocusedContainerColor = CardNavy,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { showFilterModal = true },
                    shape = RoundedCornerShape(16.dp),
                    color = CardNavy,
                    border = BorderStroke(1.dp, if (selectedInstitutionFilter != "All" || selectedCategoryFilter != null) TealAccent else BorderColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = if (selectedInstitutionFilter != "All" || selectedCategoryFilter != null) TealAccent else TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs: All Items, Lost, Found
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CardNavy,
                contentColor = TealAccent,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = TealAccent
                        )
                    }
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                listOf("All Items", "Lost", "Found").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) TealAccent else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results count and View Toggle (List vs Map)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredReports.size} results",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardNavy)
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                ) {
                    IconButton(
                        onClick = { isMapView = false },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (!isMapView) TealAccent.copy(alpha = 0.2f) else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "List View",
                            tint = if (!isMapView) TealAccent else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { isMapView = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isMapView) TealAccent.copy(alpha = 0.2f) else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map View",
                            tint = if (isMapView) TealAccent else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedReportForDetail != null) {
                com.segotlo.campusfindapp.ui.components.ReportDetailDialog(
                    report = selectedReportForDetail!!,
                    onDismiss = { selectedReportForDetail = null }
                )
            }

            if (isMapView) {
                com.segotlo.campusfindapp.ui.components.CampusMapView(
                    reports = filteredReports,
                    onReportClick = { selectedReportForDetail = it },
                    modifier = Modifier.weight(1f)
                )
            } else if (filteredReports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No reports match your search criteria",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try adjusting your search terms, university/college filters, or switching tabs.",
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredReports, key = { it.id }) { report ->
                        SearchReportCard(
                            report = report,
                            onViewDetails = null
                        )
                    }
                }
            }
        }
    }

    // Filter Modal Dialog
    if (showFilterModal) {
        Dialog(onDismissRequest = { showFilterModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Reports",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showFilterModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter by Institution / University / College
                    Text(
                        text = "University / College",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val institutions = listOf("All", "Pretoria", "Wits", "Johannesburg", "Cape Town", "TUT")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        institutions.take(3).forEach { inst ->
                            FilterChip(
                                selected = selectedInstitutionFilter == inst,
                                onClick = { selectedInstitutionFilter = inst },
                                label = { Text(inst, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealAccent,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DeepNavy,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        institutions.drop(3).forEach { inst ->
                            FilterChip(
                                selected = selectedInstitutionFilter == inst,
                                onClick = { selectedInstitutionFilter = inst },
                                label = { Text(inst, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealAccent,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DeepNavy,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter by Category
                    Text(
                        text = "Item Category",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf(
                        "All" to null,
                        "Bags" to ItemCategory.WALLETS_BAGS,
                        "Electronics" to ItemCategory.ELECTRONICS,
                        "Cards/IDs" to ItemCategory.STUDENT_CARDS_IDS,
                        "Keys" to ItemCategory.KEYS_ACCESS
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.chunked(3).forEach { rowCats ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowCats.forEach { (catName, catEnum) ->
                                    FilterChip(
                                        selected = selectedCategoryFilter == catEnum,
                                        onClick = { selectedCategoryFilter = catEnum },
                                        label = { Text(catName, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TealAccent,
                                            selectedLabelColor = Color.Black,
                                            containerColor = DeepNavy,
                                            labelColor = TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                selectedInstitutionFilter = "All"
                                selectedCategoryFilter = null
                                showFilterModal = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("Reset", color = TextPrimary)
                        }

                        Button(
                            onClick = { showFilterModal = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply Filters", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
