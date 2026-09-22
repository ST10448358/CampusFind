package com.segotlo.campusfindapp.ui.screens

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.segotlo.campusfindapp.data.SouthAfricanInstitutions
import com.segotlo.campusfindapp.ui.components.CampusTextField
import com.segotlo.campusfindapp.ui.components.GoogleWebSignInDialog
import com.segotlo.campusfindapp.ui.components.GradientButton
import com.segotlo.campusfindapp.ui.theme.*
import com.segotlo.campusfindapp.util.PasswordValidator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onLoginNav: () -> Unit,
    onRegisterSuccess: (firstName: String, lastName: String, email: String, password: String, studentNumber: String, institution: String, avatarUrl: String?) -> Unit
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var isInstitutionDropdownExpanded by remember { mutableStateOf(false) }
    var studentNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    var avatarBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var showPhotoPickerDialog by remember { mutableStateOf(false) }
    var showGoogleAccountsSheet by remember { mutableStateOf(false) }
    var showCustomGoogleWebForm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredInstitutions = remember(institution) {
        SouthAfricanInstitutions.search(institution)
    }

    val passwordCriteria = remember(password) { PasswordValidator.validate(password) }
    val passwordStrength = remember(passwordCriteria) { PasswordValidator.getStrength(passwordCriteria) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            avatarBitmap = bitmap.asImageBitmap()
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
                scope.launch {
                    snackbarHostState.showSnackbar("Camera is unavailable on this device")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to take a photo")
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
                scope.launch {
                    snackbarHostState.showSnackbar("Camera is unavailable on this device")
                }
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
                    avatarBitmap = bitmap.asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveBitmapToLocalFile(bitmap: ImageBitmap): String? {
        return try {
            val file = java.io.File(context.filesDir, "avatar_${System.currentTimeMillis()}.png")
            val outputStream = java.io.FileOutputStream(file)
            val androidBitmap = bitmap.asAndroidBitmap()
            androidBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun handleRegister() {
        when {
            firstName.isBlank() || lastName.isBlank() || institution.isBlank() || studentNumber.isBlank() || email.isBlank() || password.isBlank() -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Please fill in all required fields including institution")
                }
            }
            !isValidEmail(email) -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Please enter a valid email address")
                }
            }
            password != confirmPassword -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Passwords do not match")
                }
            }
            !agreeToTerms -> {
                scope.launch {
                    snackbarHostState.showSnackbar("You must agree to the terms and conditions")
                }
            }
            else -> {
                val savedAvatarUrl = avatarBitmap?.let { saveBitmapToLocalFile(it) }
                onRegisterSuccess(firstName, lastName, email, password, studentNumber, institution, savedAvatarUrl)
            }
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
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logo
                com.segotlo.campusfindapp.ui.components.CampusFindLogoWithText(
                    logoSize = 36.dp,
                    textSize = 20.sp,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text("Create account", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Join your campus community", color = TextSecondary, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(32.dp))

                // Avatar Upload Section
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { showPhotoPickerDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardNavy)
                            .border(1.dp, BorderColor, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap!!,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(TealPrimary)
                            .border(2.dp, DeepNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = if (avatarBitmap != null) "Change Photo" else "Add Photo (Camera / Upload)",
                    color = TealAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                        .clickable { showPhotoPickerDialog = true }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Names Row
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CampusTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "First name",
                        placeholder = "Lerato",
                        isRequired = true,
                        modifier = Modifier.weight(1f)
                    )
                    CampusTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last name",
                        placeholder = "Mokoena",
                        isRequired = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Institution Dropdown Field (BEFORE student/staff number)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row {
                        Text(
                            text = "University / College",
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
                        value = institution,
                        onValueChange = {
                            institution = it
                            isInstitutionDropdownExpanded = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search South African institution...", color = TextSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { isInstitutionDropdownExpanded = !isInstitutionDropdownExpanded }) {
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
                        expanded = isInstitutionDropdownExpanded && filteredInstitutions.isNotEmpty(),
                        onDismissRequest = { isInstitutionDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .background(CardNavy)
                            .heightIn(max = 240.dp)
                    ) {
                        filteredInstitutions.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = TextPrimary, fontSize = 14.sp) },
                                onClick = {
                                    institution = item
                                    isInstitutionDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CampusTextField(
                    value = studentNumber,
                    onValueChange = { studentNumber = it },
                    label = "Student / staff number",
                    placeholder = "220031847",
                    isRequired = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                CampusTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    placeholder = "lerato@campus.ac.za",
                    isRequired = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                CampusTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Min 8 characters (uppercase, lowercase, number & special)",
                    isPassword = true,
                    isRequired = true
                )

                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordStrengthMeter(strength = passwordStrength)
                }

                Spacer(modifier = Modifier.height(16.dp))

                CampusTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm password",
                    placeholder = "Re-enter password",
                    isPassword = true,
                    isRequired = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = TealPrimary, uncheckedColor = TextSecondary)
                    )
                    Text("I agree to the CampusFind terms and privacy policy.", color = TextPrimary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    text = "Create account",
                    onClick = { handleRegister() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Divider(modifier = Modifier.weight(1f), color = BorderColor)
                    Text("  OR  ", color = TextSecondary, fontSize = 12.sp)
                    Divider(modifier = Modifier.weight(1f), color = BorderColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showGoogleAccountsSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google",
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continue with Google", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Already have an account? Log in",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .clickable { onLoginNav() },
                    textAlign = TextAlign.Center,
                    color = TealAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Photo Option Dialog
    if (showPhotoPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoPickerDialog = false },
            title = { Text("Profile Picture Option", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Choose how you would like to add your profile photo:", color = TextSecondary, fontSize = 14.sp)
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
                        color = CardNavy
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TealAccent)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Take a Picture (Camera)", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Gallery
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoPickerDialog = false
                                galleryLauncher.launch("image/*")
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CardNavy
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AccentSkyBlue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Choose from Gallery / Photos", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (avatarBitmap != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Option 3: Remove
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    avatarBitmap = null
                                    showPhotoPickerDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = CardNavy
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Remove Profile Photo", color = AccentRed, fontWeight = FontWeight.SemiBold)
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
            containerColor = DeepNavy
        )
    }

    // Google Accounts Single Sign-In Sheet
    if (showGoogleAccountsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoogleAccountsSheet = false },
            containerColor = CardNavy,
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = AccentSkyBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Sign in with Google", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Choose an account to continue to CampusFind", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val googleAccounts = listOf(
                    Triple("Katlego Molokwane", "katlego.molokwane@gmail.com", "University of Pretoria (UP)"),
                    Triple("Lerato Mokoena", "220031847@campus.ac.za", "Tshwane University of Technology (TUT)"),
                    Triple("Sibusiso Ndlovu", "sibusiso.n@wits.ac.za", "University of the Witwatersrand (Wits)")
                )

                googleAccounts.forEach { (name, googleEmail, inst) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                showGoogleAccountsSheet = false
                                val parts = name.split(" ")
                                val fName = parts.getOrNull(0) ?: "User"
                                val lName = parts.getOrNull(1) ?: "Account"
                                onRegisterSuccess(fName, lName, googleEmail, "GooglePass123!", "220" + (100000..999999).random(), inst, null)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = DeepNavy,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.first().toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(googleEmail, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        showGoogleAccountsSheet = false
                        showCustomGoogleWebForm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add another Google Account", color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Google Web Form Sign In Dialog
    if (showCustomGoogleWebForm) {
        GoogleWebSignInDialog(
            onDismiss = { showCustomGoogleWebForm = false },
            onSignInSuccess = { enteredEmail ->
                showCustomGoogleWebForm = false
                val parts = enteredEmail.substringBefore("@").replace(".", " ").split(" ")
                val fName = parts.getOrNull(0)?.capitalize() ?: "Google"
                val lName = parts.getOrNull(1)?.capitalize() ?: "User"
                onRegisterSuccess(fName, lName, enteredEmail, "GooglePass123!", "220" + (100000..999999).random(), "University of Pretoria (UP)", null)
            }
        )
    }
}

@Composable
fun PasswordStrengthMeter(strength: com.segotlo.campusfindapp.util.PasswordStrength) {
    val color = when (strength) {
        com.segotlo.campusfindapp.util.PasswordStrength.VERY_WEAK, com.segotlo.campusfindapp.util.PasswordStrength.WEAK -> AccentRed
        com.segotlo.campusfindapp.util.PasswordStrength.MEDIUM -> AccentAmber
        com.segotlo.campusfindapp.util.PasswordStrength.STRONG -> AccentSkyBlue
        com.segotlo.campusfindapp.util.PasswordStrength.VERY_STRONG -> AccentGreen
    }

    val segments = when (strength) {
        com.segotlo.campusfindapp.util.PasswordStrength.VERY_WEAK -> 1
        com.segotlo.campusfindapp.util.PasswordStrength.WEAK -> 2
        com.segotlo.campusfindapp.util.PasswordStrength.MEDIUM -> 3
        com.segotlo.campusfindapp.util.PasswordStrength.STRONG -> 4
        com.segotlo.campusfindapp.util.PasswordStrength.VERY_STRONG -> 5
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < segments) color else BorderColor)
                )
            }
        }
        Text(
            text = strength.name.replace("_", " ").lowercase().capitalize(),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
