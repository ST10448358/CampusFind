package com.segotlo.campusfindapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.UserRole
import com.segotlo.campusfindapp.ui.components.CampusTextField
import com.segotlo.campusfindapp.ui.components.GoogleWebSignInDialog
import com.segotlo.campusfindapp.ui.components.GradientButton
import com.segotlo.campusfindapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: (String, UserRole) -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showGoogleAccountsSheet by remember { mutableStateOf(false) }
    var showCustomGoogleWebForm by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun handleLogin() {
        if (email.isBlank() || password.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("Please enter your email and password")
            }
        } else {
            scope.launch {
                val result = MockRepository.login(email, password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        onLoginSuccess(email, user.role)
                    }
                } else {
                    snackbarHostState.showSnackbar("Login failed: ${result.exceptionOrNull()?.message}")
                }
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
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                }

                Spacer(modifier = Modifier.height(24.dp))

                com.segotlo.campusfindapp.ui.components.CampusFindLogoWithText(
                    logoSize = 40.dp,
                    textSize = 20.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text("Welcome back", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Sign in to your account", color = TextSecondary, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(32.dp))

                CampusTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    placeholder = "student@campus.ac.za",
                    isRequired = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                var passwordVisible by remember { mutableStateOf(false) }

                Column {
                    Row {
                        Text("Password", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(" *", color = AccentRed, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = TextSecondary) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = TextSecondary)
                            }
                        },
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            "Forgot password?",
                            color = TealAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                resetEmailInput = email
                                showForgotPasswordDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    text = "Log In",
                    onClick = { handleLogin() }
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

                // Admin Portal Access
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardNavy,
                    border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentPurple))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Administrator Portal Access", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Default password: AdminPassword123!", color = TextSecondary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onLoginSuccess("admin@campus.ac.za", UserRole.ADMIN) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Use Admin", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onRegister() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Create account",
                        color = TealAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showForgotPasswordDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = CardNavy,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Reset Password", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enter your registered campus email address and we'll send you a password reset link.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampusTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = "Email Address",
                        placeholder = "student@campus.ac.za"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showForgotPasswordDialog = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("Cancel", color = TextPrimary)
                        }
                        Button(
                            onClick = {
                                if (resetEmailInput.isNotBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Password reset link sent to $resetEmailInput")
                                    }
                                    showForgotPasswordDialog = false
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please enter your email address")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send Link", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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
                                scope.launch {
                                    MockRepository.register(
                                        firstName = fName,
                                        lastName = lName,
                                        email = googleEmail,
                                        password = "GooglePass123!",
                                        studentNumber = "220" + (100000..999999).random(),
                                        institution = inst
                                    )
                                    onLoginSuccess(googleEmail, UserRole.STUDENT)
                                }
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
                scope.launch {
                    MockRepository.register(
                        firstName = fName,
                        lastName = lName,
                        email = enteredEmail,
                        password = "GooglePass123!",
                        studentNumber = "220" + (100000..999999).random(),
                        institution = "University of Pretoria (UP)"
                    )
                    onLoginSuccess(enteredEmail, UserRole.STUDENT)
                }
            }
        )
    }
}
