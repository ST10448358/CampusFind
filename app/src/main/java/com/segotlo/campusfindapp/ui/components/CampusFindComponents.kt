package com.segotlo.campusfindapp.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.segotlo.campusfindapp.data.*
import com.segotlo.campusfindapp.ui.theme.*

@Composable
fun rememberDecodedImageBitmap(imageUrl: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(imageUrl) {
        if (imageUrl.isNull_or_blank()) return@remember null
        try {
            val uri = Uri.parse(imageUrl)
            if (uri.scheme == "file") {
                val file = java.io.File(uri.path ?: "")
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    return@remember bitmap?.asImageBitmap()
                }
            }
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.trim().isEmpty()
}

@Composable
fun CampusFindLogo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = androidx.compose.ui.res.painterResource(id = com.segotlo.campusfindapp.R.drawable.ic_campus_find_logo),
        contentDescription = "CampusFind Logo",
        modifier = modifier.size(36.dp)
    )
}

@Composable
fun CampusFindLogoWithText(
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp,
    textSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    textColor: Color = TextPrimary,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    ) {
        CampusFindLogo(modifier = Modifier.size(logoSize))
        Spacer(modifier = Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Campus",
                color = textColor,
                fontSize = textSize,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Find",
                color = TealAccent,
                fontSize = textSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CampusTopBar() {
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CampusFindLogo(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Campus", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Find", color = TealAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    VerticalDivider(
                        modifier = Modifier.height(16.dp),
                        thickness = 1.dp,
                        color = BorderColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Find what you lost. Return what you found.", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = CardNavy, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentAmber)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Light", fontSize = 12.sp)
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = CardNavy, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp), tint = TealAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EN", fontSize = 12.sp)
                }
                OutlinedIconButton(
                    onClick = {},
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = CardNavy, contentColor = TextPrimary)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun CampusBottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = DeepNavy,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("home", "Home", Icons.Default.Home),
            Triple("search", "Search", Icons.Default.Search),
            Triple("reports", "My Reports", Icons.Default.Assignment),
            Triple("alerts", "Alerts", Icons.Default.Notifications),
            Triple("profile", "Profile", Icons.Default.Person)
        )
        
        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label, tint = if (isSelected) TealAccent else TextSecondary) },
                label = { Text(label, fontSize = 10.sp, color = if (isSelected) TealAccent else TextSecondary) },
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
fun SearchReportCard(
    report: Report,
    onViewDetails: (() -> Unit)? = null
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    val decodedBitmap = rememberDecodedImageBitmap(report.imageUrl)
    val formattedDate = remember(report.dateOccurred) {
        val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        sdf.format(report.dateOccurred)
    }

    fun handleDetailsClick() {
        if (onViewDetails != null) {
            onViewDetails()
        } else {
            showDetailDialog = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { handleDetailsClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Item Picture Thumbnail (80x80 dp)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    if (decodedBitmap != null) {
                        Image(
                            bitmap = decodedBitmap,
                            contentDescription = report.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!report.imageUrl.isNull_or_blank()) {
                        coil.compose.SubcomposeAsyncImage(
                            model = report.imageUrl,
                            contentDescription = report.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = {
                                Icon(
                                    imageVector = when (report.category) {
                                        ItemCategory.ELECTRONICS -> Icons.Default.Devices
                                        ItemCategory.STUDENT_CARDS_IDS -> Icons.Default.Badge
                                        ItemCategory.KEYS_ACCESS -> Icons.Default.Key
                                        ItemCategory.WALLETS_BAGS -> Icons.Default.Work
                                        else -> Icons.Default.Search
                                    },
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        )
                    } else {
                        Icon(
                            imageVector = when (report.category) {
                                ItemCategory.ELECTRONICS -> Icons.Default.Devices
                                ItemCategory.STUDENT_CARDS_IDS -> Icons.Default.Badge
                                ItemCategory.KEYS_ACCESS -> Icons.Default.Key
                                ItemCategory.WALLETS_BAGS -> Icons.Default.Work
                                else -> Icons.Default.Search
                            },
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Badges Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type Badge (Found / Lost)
                        val isFound = report.type == ReportType.FOUND
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFound) Color(0xFFE6F4EA) else Color(0xFFFCE8E6))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isFound) "Found" else "Lost",
                                color = if (isFound) Color(0xFF137333) else Color(0xFFC5221F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Sub-status Badge (Searching / Possible Match / Claimed)
                        val statusText = report.customStatusLabel ?: when (report.status) {
                            ReportStatus.OPEN -> "Searching"
                            ReportStatus.MATCH_SUGGESTED -> "Possible Match"
                            ReportStatus.CLAIMED -> "Claimed"
                            ReportStatus.RETURNED -> "Returned"
                            else -> "In Progress"
                        }

                        val (statusBg, statusFg) = when (statusText) {
                            "Searching" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                            "Possible Match" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
                            "Claimed" -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
                            else -> Color(0xFFE5E7EB) to Color(0xFF374151)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = statusFg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Title
                    Text(
                        text = report.title,
                        color = Color(0xFF111827),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Category
                    val categoryText = when (report.category) {
                        ItemCategory.WALLETS_BAGS -> "Bags"
                        ItemCategory.ELECTRONICS -> "Electronics"
                        ItemCategory.STUDENT_CARDS_IDS -> "Student ID / Cards"
                        ItemCategory.KEYS_ACCESS -> "Keys / Access"
                        ItemCategory.CLOTHING_ACCESSORIES -> "Clothing"
                        ItemCategory.ACADEMIC_BOOKS_NOTES -> "Books / Notes"
                        else -> "Other"
                    }
                    Text(
                        text = categoryText,
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Location & University & Date Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Location + University detail
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            
                            val displayLoc = if (report.specificLocationDetail.isNotBlank()) {
                                report.specificLocationDetail
                            } else {
                                report.location.name.replace("_", " ").lowercase().capitalize()
                            }
                            
                            val shortInst = when {
                                report.institution.contains("Pretoria") -> "UP"
                                report.institution.contains("Wits") -> "Wits"
                                report.institution.contains("Johannesburg") -> "UJ"
                                report.institution.contains("Cape Town") -> "UCT"
                                report.institution.contains("TUT") -> "TUT"
                                else -> report.institution.take(12)
                            }
                            Text(
                                text = "$displayLoc • $shortInst",
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Date
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formattedDate,
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer row: Reference Code on left, View Details link on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.referenceCode,
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "View Details",
                    color = Color(0xFF2563EB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { handleDetailsClick() }
                )
            }
        }
    }

    if (showDetailDialog) {
        ReportDetailDialog(report = report, onDismiss = { showDetailDialog = false })
    }
}

@Composable
fun ReportListItem(
    report: Report,
    onClick: (() -> Unit)? = null
) {
    SearchReportCard(report = report, onViewDetails = onClick)
}

@Composable
fun ReportDetailDialog(
    report: com.segotlo.campusfindapp.data.Report,
    onDismiss: () -> Unit
) {
    val decodedBitmap = rememberDecodedImageBitmap(report.imageUrl)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = CardNavy,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            status = report.type.name,
                            color = if (report.type == ReportType.LOST) AccentRed else AccentGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = report.referenceCode,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Banner (if available)
                if (decodedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = decodedBitmap,
                            contentDescription = report.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Item Title
                Text(
                    text = report.title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location Details
                val locationText = if (report.specificLocationDetail.isNotBlank()) {
                    report.specificLocationDetail
                } else {
                    report.location.name.replace("_", " ")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = TealAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Location: $locationText (${report.institution})",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                CampusMapView(
                    reports = listOf(report),
                    onReportClick = {},
                    modifier = Modifier.height(150.dp),
                    initialCenter = com.google.android.gms.maps.model.LatLng(report.effectiveLatitude, report.effectiveLongitude)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Category: ${report.category.name.replace("_", " ").lowercase().capitalize()}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (report.description.isNotBlank()) {
                    Text(
                        text = "Description",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = DeepNavy,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text(
                            text = report.description,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Reporter Info Card
                Text(
                    text = "Reported By",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DeepNavy,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TealAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = report.userDisplayName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = report.userEmail,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Details", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CampusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isRequired: Boolean = false,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (isRequired) {
                Text(
                    text = " *",
                    color = AccentRed,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary) },
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword) {
                {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardNavy,
                unfocusedContainerColor = CardNavy,
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TealPrimary,
    contentColor: Color = TextPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@Composable
fun StatusPill(status: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun UserAvatar(imageUrl: String?, size: Dp = 64.dp) {
    val decodedBitmap = rememberDecodedImageBitmap(imageUrl)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(CardNavy)
            .border(2.dp, TealPrimary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap,
                contentDescription = "User Avatar",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
