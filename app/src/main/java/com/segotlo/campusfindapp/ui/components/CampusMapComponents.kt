package com.segotlo.campusfindapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.segotlo.campusfindapp.data.*
import com.segotlo.campusfindapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MapSearchBar(
    onLocationFound: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filteredCampuses = remember(query) {
        if (query.trim().length < 2) emptyList()
        else SouthAfricanInstitutions.search(query).take(5)
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = it.isNotBlank()
            },
            placeholder = { Text("Search campus e.g. CPUT, UCT, Wits, UP...", color = TextSecondary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TealAccent) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { query = ""; expanded = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardNavy)
                .border(1.dp, TealAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardNavy,
                unfocusedContainerColor = CardNavy,
                focusedBorderColor = TealAccent,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        DropdownMenu(
            expanded = expanded && filteredCampuses.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(CardNavy)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
        ) {
            filteredCampuses.forEach { campusName ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(campusName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    },
                    onClick = {
                        query = campusName
                        expanded = false
                        coroutineScope.launch(Dispatchers.IO) {
                            var coords: LatLng? = null
                            try {
                                val geocoder = android.location.Geocoder(context)
                                val addrs = geocoder.getFromLocationName("$campusName, South Africa", 1)
                                if (!addrs.isNullOrEmpty()) {
                                    coords = LatLng(addrs[0].latitude, addrs[0].longitude)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            if (coords == null) {
                                coords = SouthAfricanInstitutions.getCoordinatesForInstitution(campusName)
                            }

                            withContext(Dispatchers.Main) {
                                onLocationFound(coords)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CampusVectorMapLayout(
    reports: List<Report>,
    onReportClick: (Report) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141C28))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background Campus Grounds
            drawRoundRect(
                color = Color(0xFF141C28),
                size = size,
                cornerRadius = CornerRadius(16.dp.toPx())
            )

            // Green Lawns
            drawCircle(
                color = Color(0xFF1E3A34).copy(alpha = 0.7f),
                center = Offset(width * 0.48f, height * 0.42f),
                radius = width * 0.22f
            )
            drawCircle(
                color = Color(0xFF1E3A34).copy(alpha = 0.5f),
                center = Offset(width * 0.75f, height * 0.25f),
                radius = width * 0.18f
            )

            // Main Campus Roads
            val roadColor = Color(0xFF2A3A4E)
            val roadWidth = 14.dp.toPx()

            // Lynnwood Road (South)
            drawLine(color = roadColor, start = Offset(0f, height * 0.85f), end = Offset(width, height * 0.85f), strokeWidth = roadWidth)
            // University Road (West)
            drawLine(color = roadColor, start = Offset(width * 0.15f, 0f), end = Offset(width * 0.15f, height), strokeWidth = roadWidth)
            // Roper Street (East)
            drawLine(color = roadColor, start = Offset(width * 0.85f, 0f), end = Offset(width * 0.85f, height), strokeWidth = roadWidth)
            // Prospect Street (North)
            drawLine(color = roadColor, start = Offset(0f, height * 0.15f), end = Offset(width, height * 0.15f), strokeWidth = roadWidth)

            // Internal Campus Walkways
            val walkwayColor = Color(0xFF3B4F66)
            drawLine(color = walkwayColor, start = Offset(width * 0.15f, height * 0.5f), end = Offset(width * 0.85f, height * 0.5f), strokeWidth = 6.dp.toPx())
            drawLine(color = walkwayColor, start = Offset(width * 0.5f, height * 0.15f), end = Offset(width * 0.5f, height * 0.85f), strokeWidth = 6.dp.toPx())

            // University Buildings
            val bldgColor = Color(0xFF243447)

            // Merensky 2 Library
            drawRoundRect(color = bldgColor, topLeft = Offset(width * 0.38f, height * 0.35f), size = Size(width * 0.22f, height * 0.16f), cornerRadius = CornerRadius(8.dp.toPx()))
            // Student Centre
            drawRoundRect(color = bldgColor, topLeft = Offset(width * 0.58f, height * 0.58f), size = Size(width * 0.2f, height * 0.14f), cornerRadius = CornerRadius(8.dp.toPx()))
            // Science Complex
            drawRoundRect(color = bldgColor, topLeft = Offset(width * 0.58f, height * 0.22f), size = Size(width * 0.18f, height * 0.14f), cornerRadius = CornerRadius(8.dp.toPx()))
            // Engineering Building
            drawRoundRect(color = bldgColor, topLeft = Offset(width * 0.22f, height * 0.58f), size = Size(width * 0.18f, height * 0.18f), cornerRadius = CornerRadius(8.dp.toPx()))
            // Main Gate Security
            drawRoundRect(color = bldgColor, topLeft = Offset(width * 0.22f, height * 0.22f), size = Size(width * 0.14f, height * 0.12f), cornerRadius = CornerRadius(8.dp.toPx()))
        }

        // Campus Street Names Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            Text("PROSPECT STREET", color = TextSecondary.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
            Text("LYNNWOOD ROAD", color = TextSecondary.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
            Text("UNIVERSITY RD", color = TextSecondary.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp))
            Text("ROPER ST", color = TextSecondary.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp))
        }

        // Campus Buildings Text Labels
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = DeepNavy.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Text("Merensky Library", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }

            Surface(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 35.dp, top = 65.dp),
                color = DeepNavy.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Student Centre", color = TextPrimary, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }

            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 35.dp, top = 50.dp),
                color = DeepNavy.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Science Complex", color = TextPrimary, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }

            Surface(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 45.dp, top = 65.dp),
                color = DeepNavy.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Engineering Tower", color = TextPrimary, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }

            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(start = 45.dp, top = 50.dp),
                color = DeepNavy.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, TealAccent.copy(alpha = 0.6f))
            ) {
                Text("Security Gate", color = TealAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }
        }

        // Item Pins Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            reports.forEachIndexed { index, report ->
                val align = when (report.location) {
                    CampusLocation.MAIN_LIBRARY -> Alignment.Center
                    CampusLocation.STUDENT_UNION -> Alignment.BottomEnd
                    CampusLocation.SCIENCE_BUILDING -> Alignment.TopEnd
                    CampusLocation.ENGINEERING_COMPLEX -> Alignment.CenterStart
                    CampusLocation.CAMPUS_SECURITY_OFFICE -> Alignment.TopStart
                    CampusLocation.SPORTS_CENTRE -> Alignment.TopCenter
                    else -> Alignment.Center
                }

                val offsetPadding = ((index % 3) * 18).dp

                Box(
                    modifier = Modifier
                        .align(align)
                        .padding(start = offsetPadding, top = offsetPadding)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (report.type == ReportType.LOST) AccentRed else TealAccent)
                        .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                        .clickable { onReportClick(report) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            report.title.take(18),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CampusMapView(
    reports: List<Report>,
    onReportClick: (Report) -> Unit,
    modifier: Modifier = Modifier,
    initialCenter: LatLng = LatLng(-25.7545, 28.2315),
    allowEnlarge: Boolean = true
) {
    val context = LocalContext.current
    var isEnlarged by remember { mutableStateOf(false) }

    val mapsApiKey = remember {
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    val hasRealMapsKey = mapsApiKey.isNotBlank() && !mapsApiKey.contains("DEFAULT") && !mapsApiKey.contains("YOUR_")

    val inlineCameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, 16f)
    }

    if (isEnlarged) {
        val dialogCameraState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(inlineCameraState.position.target, inlineCameraState.position.zoom)
        }

        Dialog(
            onDismissRequest = { isEnlarged = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                color = DeepNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (hasRealMapsKey) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = dialogCameraState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = true,
                                compassEnabled = true,
                                mapToolbarEnabled = true,
                                myLocationButtonEnabled = true
                            ),
                            properties = MapProperties(isBuildingEnabled = true)
                        ) {
                            reports.forEach { report ->
                                val latLng = LatLng(report.effectiveLatitude, report.effectiveLongitude)
                                val isLost = report.type == ReportType.LOST
                                val markerHue = if (isLost) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_AZURE
                                val markerState = rememberMarkerState(key = "dialog_${report.id}", position = latLng)

                                Marker(
                                    state = markerState,
                                    title = report.title,
                                    snippet = "${report.type.name} • ${report.location.name.replace("_", " ")}",
                                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                                    onClick = {
                                        onReportClick(report)
                                        true
                                    }
                                )
                            }
                        }
                    } else {
                        CampusVectorMapLayout(
                            reports = reports,
                            onReportClick = onReportClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Fullscreen Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepNavy.copy(alpha = 0.95f))
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Campus Map • Full Screen (${reports.size} Items)",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = { isEnlarged = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
        color = CardNavy
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasRealMapsKey) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = inlineCameraState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        compassEnabled = true,
                        mapToolbarEnabled = true
                    ),
                    properties = MapProperties(
                        isBuildingEnabled = true
                    )
                ) {
                    reports.forEach { report ->
                        val latLng = LatLng(report.effectiveLatitude, report.effectiveLongitude)
                        val isLost = report.type == ReportType.LOST
                        val markerHue = if (isLost) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_AZURE
                        val markerState = rememberMarkerState(key = "inline_${report.id}", position = latLng)

                        Marker(
                            state = markerState,
                            title = report.title,
                            snippet = "${report.type.name} • ${report.location.name.replace("_", " ")}",
                            icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                            onClick = {
                                onReportClick(report)
                                true
                            }
                        )
                    }
                }
            } else {
                CampusVectorMapLayout(
                    reports = reports,
                    onReportClick = onReportClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Campus Map Header Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepNavy.copy(alpha = 0.9f))
                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (hasRealMapsKey) "Live Google Map • ${reports.size} Items" else "Hatfield Campus Map • ${reports.size} Items",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (allowEnlarge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepNavy.copy(alpha = 0.95f))
                        .border(1.dp, TealAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { isEnlarged = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Enlarge Map", tint = TealAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enlarge", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CampusLocationPicker(
    location: CampusLocation,
    onCoordinatesSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    allowEnlarge: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val defaultCoords = location.getCoordinates()
    var selectedLatLng by remember {
        mutableStateOf(LatLng(defaultCoords.first, defaultCoords.second))
    }
    var isEnlarged by remember { mutableStateOf(false) }

    val mapsApiKey = remember {
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    val hasRealMapsKey = mapsApiKey.isNotBlank() && !mapsApiKey.contains("DEFAULT") && !mapsApiKey.contains("YOUR_")

    val inlineCameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 17f)
    }

    LaunchedEffect(selectedLatLng) {
        inlineCameraState.animate(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 17f))
    }

    if (isEnlarged) {
        val dialogCameraState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(selectedLatLng, 17f)
        }

        Dialog(
            onDismissRequest = { isEnlarged = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                color = DeepNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (hasRealMapsKey) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = dialogCameraState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = true,
                                compassEnabled = true
                            ),
                            onMapClick = { latLng ->
                                selectedLatLng = latLng
                                onCoordinatesSelected(latLng.latitude, latLng.longitude)
                            }
                        ) {
                            val dialogMarkerState = rememberMarkerState(key = "picker_dialog", position = selectedLatLng)
                            LaunchedEffect(selectedLatLng) {
                                dialogMarkerState.position = selectedLatLng
                            }
                            LaunchedEffect(dialogMarkerState.position) {
                                if (dialogMarkerState.position != selectedLatLng) {
                                    selectedLatLng = dialogMarkerState.position
                                    onCoordinatesSelected(dialogMarkerState.position.latitude, dialogMarkerState.position.longitude)
                                }
                            }
                            Marker(
                                state = dialogMarkerState,
                                title = location.name.replace("_", " "),
                                snippet = "Draggable pin location",
                                draggable = true
                            )
                        }
                    } else {
                        CampusVectorMapLayout(
                            reports = emptyList(),
                            onReportClick = {},
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Fullscreen Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepNavy.copy(alpha = 0.95f))
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tap or Drag Pin to Set Location",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = { isEnlarged = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    // Map Search Bar Overlay
                    MapSearchBar(
                        onLocationFound = { newLatLng ->
                            selectedLatLng = newLatLng
                            onCoordinatesSelected(newLatLng.latitude, newLatLng.longitude)
                            coroutineScope.launch {
                                dialogCameraState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 16f))
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 76.dp, start = 16.dp, end = 16.dp)
                    )

                    // Move Pin to Center Button Overlay
                    if (hasRealMapsKey) {
                        Button(
                            onClick = {
                                val centerLatLng = dialogCameraState.position.target
                                selectedLatLng = centerLatLng
                                onCoordinatesSelected(centerLatLng.latitude, centerLatLng.longitude)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Move Pin to Center Screen", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, TealAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            color = CardNavy
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasRealMapsKey) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = inlineCameraState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            compassEnabled = false
                        ),
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                            onCoordinatesSelected(latLng.latitude, latLng.longitude)
                        }
                    ) {
                        val inlineMarkerState = rememberMarkerState(key = "picker_inline", position = selectedLatLng)
                        LaunchedEffect(selectedLatLng) {
                            inlineMarkerState.position = selectedLatLng
                        }
                        LaunchedEffect(inlineMarkerState.position) {
                            if (inlineMarkerState.position != selectedLatLng) {
                                selectedLatLng = inlineMarkerState.position
                                onCoordinatesSelected(inlineMarkerState.position.latitude, inlineMarkerState.position.longitude)
                            }
                        }
                        Marker(
                            state = inlineMarkerState,
                            title = location.name.replace("_", " "),
                            snippet = "Tap or drag pin on map",
                            draggable = true
                        )
                    }
                } else {
                    CampusVectorMapLayout(
                        reports = emptyList(),
                        onReportClick = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Inline Map Search Bar Overlay
                MapSearchBar(
                    onLocationFound = { newLatLng ->
                        selectedLatLng = newLatLng
                        onCoordinatesSelected(newLatLng.latitude, newLatLng.longitude)
                        coroutineScope.launch {
                            inlineCameraState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 16f))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 10.dp, start = 10.dp, end = 80.dp)
                )

                // Move Pin to Camera Center Button
                if (hasRealMapsKey) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepNavy.copy(alpha = 0.9f))
                            .border(1.dp, TealAccent, RoundedCornerShape(8.dp))
                            .clickable {
                                val centerLatLng = inlineCameraState.position.target
                                selectedLatLng = centerLatLng
                                onCoordinatesSelected(centerLatLng.latitude, centerLatLng.longitude)
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TealAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Move Pin Here", color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (allowEnlarge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepNavy.copy(alpha = 0.95f))
                            .border(1.dp, TealAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { isEnlarged = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Enlarge Map", tint = TealAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enlarge", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Selected Pin: Lat ${String.format("%.4f", selectedLatLng.latitude)}, Lng ${String.format("%.4f", selectedLatLng.longitude)}",
            color = TealAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
