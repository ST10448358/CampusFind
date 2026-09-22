package com.segotlo.campusfindapp.ui.screens

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import com.segotlo.campusfindapp.data.*
import com.segotlo.campusfindapp.ui.components.CampusTextField
import com.segotlo.campusfindapp.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlinx.coroutines.launch

fun mapCategoryNameToEnum(categoryName: String): ItemCategory {
    return when (categoryName) {
        "Electronics" -> ItemCategory.ELECTRONICS
        "Student Cards / IDs" -> ItemCategory.STUDENT_CARDS_IDS
        "Keys / Access Fobs" -> ItemCategory.KEYS_ACCESS
        "Wallets / Bags" -> ItemCategory.WALLETS_BAGS
        "Clothing / Accessories" -> ItemCategory.CLOTHING_ACCESSORIES
        "Academic Books / Notes" -> ItemCategory.ACADEMIC_BOOKS_NOTES
        else -> ItemCategory.OTHER
    }
}

fun saveBitmapToInternalStorage(context: android.content.Context, imageBitmap: ImageBitmap): String? {
    return try {
        val bitmap = imageBitmap.asAndroidBitmap()
        val imagesDir = File(context.filesDir, "report_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val file = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
        stream.flush()
        stream.close()
        file.toURI().toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ReportLostScreen(
    onClose: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val user by MockRepository.currentUser.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedInstitution by remember { mutableStateOf(user?.institution?.ifBlank { "University of Pretoria (UP)" } ?: "University of Pretoria (UP)") }
    var description by remember { mutableStateOf("") }
    var dateLost by remember { mutableStateOf("09/20/2026") }
    var approxTime by remember { mutableStateOf("12:30 PM") }
    var locationInput by remember { mutableStateOf("") }
    var customLat by remember { mutableStateOf<Double?>(null) }
    var customLng by remember { mutableStateOf<Double?>(null) }
    
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var institutionDropdownExpanded by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredInstitutions = remember(selectedInstitution) {
        SouthAfricanInstitutions.search(selectedInstitution)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    selectedImageBitmap = bitmap.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedImageBitmap = bitmap.asImageBitmap()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Camera is unavailable on this device") }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Camera permission is required") }
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
                scope.launch { snackbarHostState.showSnackbar("Camera is unavailable on this device") }
            }
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val categoriesList = listOf(
        "Electronics", "Student Cards / IDs", "Keys / Access Fobs",
        "Wallets / Bags", "Clothing / Accessories", "Academic Books / Notes", "Other Campus Items"
    )

    if (showPhotoSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onGallerySelect = {
                showPhotoSourceDialog = false
                galleryLauncher.launch("image/*")
            },
            onCameraSelect = {
                showPhotoSourceDialog = false
                launchCameraSafely()
            }
        )
    }

    fun handleReportSubmit() {
        if (itemName.isBlank() || selectedCategory.isEmpty() || locationInput.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Please fill in all required fields marked with *")
            }
        } else {
            val savedImageFileUrl = if (selectedImageBitmap != null) {
                saveBitmapToInternalStorage(context, selectedImageBitmap!!) ?: selectedImageUri?.toString()
            } else {
                selectedImageUri?.toString()
            }

            user?.let { u ->
                MockRepository.addReport(
                    Report(
                        userId = u.id,
                        userDisplayName = "${u.firstName} ${u.lastName}",
                        userEmail = u.email,
                        type = ReportType.LOST,
                        title = itemName,
                        description = description,
                        category = mapCategoryNameToEnum(selectedCategory),
                        location = CampusLocation.MAIN_LIBRARY,
                        specificLocationDetail = locationInput,
                        latitude = customLat,
                        longitude = customLng,
                        institution = selectedInstitution.ifBlank { "University of Pretoria (UP)" },
                        dateOccurred = Date(),
                        status = ReportStatus.OPEN,
                        customStatusLabel = "Searching",
                        imageUrl = savedImageFileUrl
                    )
                )
            }
            onSubmitSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepNavy
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepNavy)
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Report Lost Item", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("I lost something", color = TextSecondary, fontSize = 13.sp)
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Item Name
                    CampusTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = "Item name",
                        placeholder = "e.g. Black Samsung Galaxy A54",
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Selection
                    Text(
                        text = buildString { append("Category"); append(" *") },
                        color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { categoryDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (categoryDropdownExpanded) TealAccent else BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = CardNavy, contentColor = TextPrimary)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(if (selectedCategory.isEmpty()) "Select a category..." else selectedCategory, color = if (selectedCategory.isEmpty()) TextSecondary else TextPrimary, fontSize = 14.sp)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                            }
                        }
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f).background(CardNavy)
                        ) {
                            categoriesList.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = TextPrimary) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Box
                    Text("Description", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Describe the item and any identifying features...", color = TextSecondary, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardNavy,
                            unfocusedContainerColor = CardNavy,
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date and Time Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date lost *", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dateLost,
                                onValueChange = { dateLost = it },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardNavy, unfocusedContainerColor = CardNavy, focusedBorderColor = TealAccent, unfocusedBorderColor = BorderColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Approximate time", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = approxTime,
                                onValueChange = { approxTime = it },
                                trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardNavy, unfocusedContainerColor = CardNavy, focusedBorderColor = TealAccent, unfocusedBorderColor = BorderColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Institution / University Searchable Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row {
                            Text(
                                text = "Institution / University",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " *",
                                color = AccentRed,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = selectedInstitution,
                            onValueChange = {
                                selectedInstitution = it
                                institutionDropdownExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search South African institution...", color = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { institutionDropdownExpanded = !institutionDropdownExpanded }) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Institution",
                                        tint = TealAccent
                                    )
                                }
                            },
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

                        DropdownMenu(
                            expanded = institutionDropdownExpanded && filteredInstitutions.isNotEmpty(),
                            onDismissRequest = { institutionDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(CardNavy)
                                .heightIn(max = 240.dp)
                        ) {
                            filteredInstitutions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = TextPrimary, fontSize = 14.sp) },
                                    onClick = {
                                        selectedInstitution = item
                                        institutionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Direct Input Field
                    CampusTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = "Location where item was lost",
                        placeholder = "e.g. Main Library 1st Floor, Science Building Lab 2...",
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pin exact location on campus map", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    com.segotlo.campusfindapp.ui.components.CampusLocationPicker(
                        location = CampusLocation.MAIN_LIBRARY,
                        onCoordinatesSelected = { lat, lng ->
                            customLat = lat
                            customLng = lng
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Photo attachment
                    Text("Photo (optional)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPhotoSourceDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, if (selectedImageBitmap != null || selectedImageUri != null) TealAccent else BorderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (selectedImageBitmap == null && selectedImageUri == null) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(CardNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap to capture / upload photo", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Supports JPG, PNG, or Camera (Max 5MB)", color = TextSecondary, fontSize = 12.sp)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageBitmap != null) {
                                        Image(
                                            bitmap = selectedImageBitmap!!,
                                            contentDescription = "Attached Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (selectedImageUri != null) {
                                        SubcomposeAsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Attached Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedImageBitmap = null
                                            selectedImageUri = null
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Remove Photo", tint = AccentRed)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Or pick a sample campus item photo:", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SamplePhotoChip("+ Laptop", onClick = {
                                    itemName = "Dell Latitude Laptop"
                                    selectedCategory = "Electronics"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=500&q=80")
                                })
                                SamplePhotoChip("+ Keys", onClick = {
                                    itemName = "Dorm Room Keys Set"
                                    selectedCategory = "Keys / Access Fobs"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&w=500&q=80")
                                })
                                SamplePhotoChip("+ Wallet", onClick = {
                                    itemName = "Brown Leather Wallet"
                                    selectedCategory = "Wallets / Bags"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80")
                                })
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SamplePhotoChip("+ Student Card", onClick = {
                                    itemName = "Student ID Card"
                                    selectedCategory = "Student Cards / IDs"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=500&q=80")
                                })
                                SamplePhotoChip("+ Backpack", onClick = {
                                    itemName = "Blue Nike Backpack"
                                    selectedCategory = "Wallets / Bags"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80")
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = { handleReportSubmit() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed, contentColor = Color.White)
                    ) {
                        Text("Submit Lost Report", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportFoundScreen(
    onClose: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val user by MockRepository.currentUser.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedInstitution by remember { mutableStateOf(user?.institution?.ifBlank { "University of Pretoria (UP)" } ?: "University of Pretoria (UP)") }
    var description by remember { mutableStateOf("") }
    var dateFound by remember { mutableStateOf("09/20/2026") }
    var approxTime by remember { mutableStateOf("12:30 PM") }
    var locationInput by remember { mutableStateOf("") }
    var customLat by remember { mutableStateOf<Double?>(null) }
    var customLng by remember { mutableStateOf<Double?>(null) }
    
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var institutionDropdownExpanded by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredInstitutions = remember(selectedInstitution) {
        SouthAfricanInstitutions.search(selectedInstitution)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    selectedImageBitmap = bitmap.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedImageBitmap = bitmap.asImageBitmap()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Camera is unavailable on this device") }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Camera permission is required") }
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
                scope.launch { snackbarHostState.showSnackbar("Camera is unavailable on this device") }
            }
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val categoriesList = listOf(
        "Electronics", "Student Cards / IDs", "Keys / Access Fobs",
        "Wallets / Bags", "Clothing / Accessories", "Academic Books / Notes", "Other Campus Items"
    )

    if (showPhotoSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onGallerySelect = {
                showPhotoSourceDialog = false
                galleryLauncher.launch("image/*")
            },
            onCameraSelect = {
                showPhotoSourceDialog = false
                launchCameraSafely()
            }
        )
    }

    fun handleReportSubmit() {
        if (itemName.isBlank() || selectedCategory.isEmpty() || locationInput.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Please fill in all required fields marked with *")
            }
        } else {
            val savedImageFileUrl = if (selectedImageBitmap != null) {
                saveBitmapToInternalStorage(context, selectedImageBitmap!!) ?: selectedImageUri?.toString()
            } else {
                selectedImageUri?.toString()
            }

            user?.let { u ->
                MockRepository.addReport(
                    Report(
                        userId = u.id,
                        userDisplayName = "${u.firstName} ${u.lastName}",
                        userEmail = u.email,
                        type = ReportType.FOUND,
                        title = itemName,
                        description = description,
                        category = mapCategoryNameToEnum(selectedCategory),
                        location = CampusLocation.MAIN_LIBRARY,
                        specificLocationDetail = locationInput,
                        latitude = customLat,
                        longitude = customLng,
                        institution = selectedInstitution.ifBlank { "University of Pretoria (UP)" },
                        dateOccurred = Date(),
                        status = ReportStatus.OPEN,
                        customStatusLabel = "Searching",
                        imageUrl = savedImageFileUrl
                    )
                )
            }
            onSubmitSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepNavy
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepNavy)
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Report Found Item", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("I found something", color = TextSecondary, fontSize = 13.sp)
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Green Privacy Alert Warning Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AccentGreen.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Privacy & Security Warning", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Do not include sensitive information such as PINs, passwords or banking information.", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Item Name
                    CampusTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = "Item name",
                        placeholder = "e.g. Blue Nike backpack",
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Selection
                    Text(
                        text = buildString { append("Category"); append(" *") },
                        color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { categoryDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (categoryDropdownExpanded) TealAccent else BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = CardNavy, contentColor = TextPrimary)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(if (selectedCategory.isEmpty()) "Select a category..." else selectedCategory, color = if (selectedCategory.isEmpty()) TextSecondary else TextPrimary, fontSize = 14.sp)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
                            }
                        }
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f).background(CardNavy)
                        ) {
                            categoriesList.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = TextPrimary) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Box
                    Text("Description", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Describe the item and any identifying features...", color = TextSecondary, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardNavy,
                            unfocusedContainerColor = CardNavy,
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date and Time Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date found *", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dateFound,
                                onValueChange = { dateFound = it },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardNavy, unfocusedContainerColor = CardNavy, focusedBorderColor = TealAccent, unfocusedBorderColor = BorderColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Approximate time", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = approxTime,
                                onValueChange = { approxTime = it },
                                trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardNavy, unfocusedContainerColor = CardNavy, focusedBorderColor = TealAccent, unfocusedBorderColor = BorderColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Institution / University Searchable Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row {
                            Text(
                                text = "Institution / University",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " *",
                                color = AccentRed,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = selectedInstitution,
                            onValueChange = {
                                selectedInstitution = it
                                institutionDropdownExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search South African institution...", color = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { institutionDropdownExpanded = !institutionDropdownExpanded }) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Institution",
                                        tint = TealAccent
                                    )
                                }
                            },
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

                        DropdownMenu(
                            expanded = institutionDropdownExpanded && filteredInstitutions.isNotEmpty(),
                            onDismissRequest = { institutionDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(CardNavy)
                                .heightIn(max = 240.dp)
                        ) {
                            filteredInstitutions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = TextPrimary, fontSize = 14.sp) },
                                    onClick = {
                                        selectedInstitution = item
                                        institutionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Direct Input Field
                    CampusTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = "Location where item was found",
                        placeholder = "e.g. Science Building Lab 2, Cafeteria...",
                        isRequired = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pin exact location on campus map", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    com.segotlo.campusfindapp.ui.components.CampusLocationPicker(
                        location = CampusLocation.MAIN_LIBRARY,
                        onCoordinatesSelected = { lat, lng ->
                            customLat = lat
                            customLng = lng
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Photo upload
                    Text("Photo (optional)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPhotoSourceDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, if (selectedImageBitmap != null || selectedImageUri != null) TealAccent else BorderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (selectedImageBitmap == null && selectedImageUri == null) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(CardNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tap to capture / upload photo", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Supports JPG, PNG, or Camera (Max 5MB)", color = TextSecondary, fontSize = 12.sp)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageBitmap != null) {
                                        Image(
                                            bitmap = selectedImageBitmap!!,
                                            contentDescription = "Attached Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (selectedImageUri != null) {
                                        SubcomposeAsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Attached Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedImageBitmap = null
                                            selectedImageUri = null
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Remove Photo", tint = AccentRed)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Or pick a sample campus item photo:", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SamplePhotoChip("+ Laptop", onClick = {
                                    itemName = "Dell Latitude Laptop"
                                    selectedCategory = "Electronics"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=500&q=80")
                                })
                                SamplePhotoChip("+ Keys", onClick = {
                                    itemName = "Dorm Room Keys Set"
                                    selectedCategory = "Keys / Access Fobs"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&w=500&q=80")
                                })
                                SamplePhotoChip("+ Wallet", onClick = {
                                    itemName = "Brown Leather Wallet"
                                    selectedCategory = "Wallets / Bags"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80")
                                })
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SamplePhotoChip("+ Student Card", onClick = {
                                    itemName = "Student ID Card"
                                    selectedCategory = "Student Cards / IDs"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=500&q=80")
                                })
                                SamplePhotoChip("+ Backpack", onClick = {
                                    itemName = "Blue Nike Backpack"
                                    selectedCategory = "Wallets / Bags"
                                    selectedImageBitmap = null
                                    selectedImageUri = Uri.parse("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80")
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Found Report Button
                    Button(
                        onClick = { handleReportSubmit() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent, contentColor = Color.White)
                    ) {
                        Text("Submit Found Report", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onGallerySelect: () -> Unit,
    onCameraSelect: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardNavy,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Photo Source", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PhotoSourceCard(
                        icon = Icons.Default.PhotoLibrary,
                        label = "Gallery",
                        onClick = onGallerySelect,
                        modifier = Modifier.weight(1f)
                    )
                    PhotoSourceCard(
                        icon = Icons.Default.PhotoCamera,
                        label = "Camera",
                        onClick = onCameraSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun PhotoSourceCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = DeepNavy,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = TealAccent, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SamplePhotoChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardNavy)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = TealAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
