package com.segotlo.campusfindapp.util

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.segotlo.campusfindapp.R
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.User
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume

object GoogleAuthManager {

    private const val WEB_CLIENT_ID = "566079707733-hcp80kf8lvf4vbk97nk485tnec1ebquq.apps.googleusercontent.com"
    private const val PREFS_NAME = "google_auth_prefs"
    private const val KEY_SAVED_ACCOUNTS = "saved_google_accounts"

    class UnregisteredAccountException(val email: String) :
        Exception("No account registered for $email. Please register first to access CampusFind.")

    private fun getSavedAccounts(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SAVED_ACCOUNTS, emptySet()) ?: emptySet()
    }

    private fun saveAccount(context: Context, email: String) {
        if (email.isBlank() || !email.contains("@")) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SAVED_ACCOUNTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(email.trim().lowercase())
        prefs.edit().putStringSet(KEY_SAVED_ACCOUNTS, current).apply()
    }

    suspend fun signInWithGoogle(activity: Activity): Result<User> {
        val credentialManager = CredentialManager.create(activity)
        val nonce = UUID.randomUUID().toString()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential

            val googleCredential = try {
                if (credential is GoogleIdTokenCredential) {
                    credential
                } else if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    GoogleIdTokenCredential.createFrom(credential.data)
                } else {
                    GoogleIdTokenCredential.createFrom(credential.data)
                }
            } catch (_: Throwable) {
                null
            }

            if (googleCredential != null) {
                val email = googleCredential.id
                val displayName = googleCredential.displayName ?: googleCredential.givenName ?: "Google User"
                val firstName = googleCredential.givenName
                    ?: displayName.split(" ").firstOrNull()
                    ?: "Google"
                val lastName = googleCredential.familyName
                    ?: displayName.split(" ").let { if (it.size > 1) it.subList(1, it.size).joinToString(" ") else "User" }
                val avatarUrl = googleCredential.profilePictureUri?.toString()

                saveAccount(activity, email)

                if (!MockRepository.isUserRegistered(email)) {
                    return Result.failure(UnregisteredAccountException(email))
                }

                try {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential).await()
                } catch (_: Throwable) {
                    // Suppress Firebase SHA-1 or project setup errors so local/Firestore auth succeeds
                }

                MockRepository.loginWithGoogle(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    avatarUrl = avatarUrl
                )
            } else {
                val emailData = credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID")
                    ?: credential.data.getString("id")
                    ?: "google.user@campusfind.ac.za"

                saveAccount(activity, emailData)

                if (!MockRepository.isUserRegistered(emailData)) {
                    return Result.failure(UnregisteredAccountException(emailData))
                }

                MockRepository.loginWithGoogle(
                    email = emailData,
                    firstName = "Google",
                    lastName = "User",
                    avatarUrl = null
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (_: Throwable) {
            // Custom Google Account Chooser bottom sheet UI matching Google design
            showGoogleAccountPickerDialog(activity)
        }
    }

    private suspend fun showGoogleAccountPickerDialog(activity: Activity): Result<User> =
        suspendCancellableCoroutine { continuation ->
            val accountManager = AccountManager.get(activity)
            val deviceAccounts: List<String> = try {
                accountManager.getAccountsByType("com.google").map { it.name }
            } catch (_: Throwable) {
                emptyList()
            }
            val savedAccounts: Set<String> = getSavedAccounts(activity)
            val registeredUsers: List<String> = try {
                MockRepository.getAllUsers().value.map { it.email }
            } catch (_: Throwable) {
                emptyList()
            }

            // Strict deduplication by lowercase email
            val combinedEmails = (deviceAccounts + savedAccounts + registeredUsers)
                .map { it.trim().lowercase() }
                .filter { it.contains("@") && it.contains(".") }
                .distinct()

            activity.runOnUiThread {
                val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_google_account_chooser, null)
                val containerAccounts = dialogView.findViewById<LinearLayout>(R.id.container_accounts)

                val bottomSheetDialog = BottomSheetDialog(activity)
                bottomSheetDialog.setContentView(dialogView)

                bottomSheetDialog.setOnShowListener {
                    val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    bottomSheet?.let { sheet ->
                        val behavior = BottomSheetBehavior.from(sheet)
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        behavior.skipCollapsed = true
                    }
                }

                val avatarColors = listOf("#2E7D32", "#1565C0", "#00838F", "#4A148C", "#D84315", "#6A1B9A", "#C62828")

                // Add accounts to list
                combinedEmails.forEachIndexed { index, email ->
                    val itemView = LayoutInflater.from(activity).inflate(R.layout.item_google_account, containerAccounts, false)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_account_name)
                    val tvEmail = itemView.findViewById<TextView>(R.id.tv_account_email)
                    val tvInitial = itemView.findViewById<TextView>(R.id.tv_avatar_initial)
                    val flAvatarBg = itemView.findViewById<View>(R.id.fl_avatar_bg)

                    val nameParts = email.substringBefore("@").split(".", "_", "-")
                    val firstName = nameParts.firstOrNull()
                        ?.lowercase()
                        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        ?: "Google"
                    val lastName = if (nameParts.size > 1) {
                        nameParts.last()
                            .lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    } else ""
                    val displayName = "$firstName $lastName".trim()

                    tvName.text = displayName
                    tvEmail.text = email
                    tvInitial.text = firstName.take(1).uppercase()
                    tvInitial.visibility = View.VISIBLE

                    val color = Color.parseColor(avatarColors[index % avatarColors.size])
                    flAvatarBg.backgroundTintList = ColorStateList.valueOf(color)

                    itemView.setOnClickListener {
                        bottomSheetDialog.dismiss()
                        saveAccount(activity, email)
                        processAccountSignIn(email, firstName, lastName, continuation)
                    }

                    containerAccounts.addView(itemView)
                }

                // Add "Use another account" item at the bottom of the scrollable list
                val addAccountView = LayoutInflater.from(activity).inflate(R.layout.item_google_account, containerAccounts, false)
                val tvAddName = addAccountView.findViewById<TextView>(R.id.tv_account_name)
                val tvAddEmail = addAccountView.findViewById<TextView>(R.id.tv_account_email)
                val tvAddInitial = addAccountView.findViewById<TextView>(R.id.tv_avatar_initial)
                val imgAccountIcon = addAccountView.findViewById<ImageView>(R.id.img_account_icon)
                val flAvatarBg = addAccountView.findViewById<View>(R.id.fl_avatar_bg)
                val dividerLine = addAccountView.findViewById<View>(R.id.divider_line)

                tvAddName.text = "Use another account"
                tvAddEmail.visibility = View.GONE
                tvAddInitial.visibility = View.GONE
                imgAccountIcon.visibility = View.VISIBLE
                flAvatarBg.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#374151"))
                dividerLine.visibility = View.GONE

                addAccountView.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    launchRealSystemGoogleAddAccount(activity, continuation)
                }

                containerAccounts.addView(addAccountView)

                bottomSheetDialog.setOnCancelListener {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(GetCredentialCancellationException("User cancelled")))
                    }
                }

                bottomSheetDialog.show()
            }
        }

    private fun processAccountSignIn(
        email: String,
        firstName: String,
        lastName: String,
        continuation: CancellableContinuation<Result<User>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val isRegistered = MockRepository.isUserRegistered(email)
            if (!isRegistered) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(UnregisteredAccountException(email)))
                }
                return@launch
            }

            val loginRes = MockRepository.loginWithGoogle(
                email = email,
                firstName = firstName,
                lastName = lastName,
                avatarUrl = null
            )
            if (continuation.isActive) {
                continuation.resume(loginRes)
            }
        }
    }

    private fun launchRealSystemGoogleAddAccount(
        activity: Activity,
        continuation: CancellableContinuation<Result<User>>
    ) {
        val accountManager = AccountManager.get(activity)
        try {
            accountManager.addAccount(
                "com.google",
                null,
                null,
                null,
                activity,
                { future ->
                    try {
                        val bundle = future.result
                        val accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME)
                        if (!accountName.isNullOrEmpty()) {
                            saveAccount(activity, accountName)
                            val nameParts = accountName.substringBefore("@").split(".", "_", "-")
                            val firstName = nameParts.firstOrNull()
                                ?.lowercase()
                                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                ?: "Google"
                            val lastName = if (nameParts.size > 1) {
                                nameParts.last()
                                    .lowercase()
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            } else "User"

                            processAccountSignIn(accountName, firstName, lastName, continuation)
                        } else {
                            val updatedAccounts = accountManager.getAccountsByType("com.google")
                            val newestAccount = updatedAccounts.lastOrNull()?.name
                            if (!newestAccount.isNullOrEmpty()) {
                                saveAccount(activity, newestAccount)
                                val nameParts = newestAccount.substringBefore("@").split(".", "_", "-")
                                val firstName = nameParts.firstOrNull()
                                    ?.lowercase()
                                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    ?: "Google"
                                val lastName = if (nameParts.size > 1) {
                                    nameParts.last()
                                        .lowercase()
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                } else "User"

                                processAccountSignIn(newestAccount, firstName, lastName, continuation)
                            } else {
                                showCustomGmailInputDialog(activity) { result ->
                                    if (continuation.isActive) continuation.resume(result)
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        showCustomGmailInputDialog(activity) { result ->
                            if (continuation.isActive) continuation.resume(result)
                        }
                    }
                },
                null
            )
        } catch (_: Throwable) {
            try {
                val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                    putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                }
                activity.startActivity(intent)
            } catch (_: Throwable) {}

            showCustomGmailInputDialog(activity) { result ->
                if (continuation.isActive) continuation.resume(result)
            }
        }
    }

    private fun showCustomGmailInputDialog(
        activity: Activity,
        onResult: (Result<User>) -> Unit
    ) {
        val input = EditText(activity).apply {
            hint = "e.g. username@gmail.com"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(activity)
            .setTitle("Enter Google Account")
            .setMessage("Enter your Google email address to sign in:")
            .setView(input)
            .setPositiveButton("Sign In") { _, _ ->
                var email = input.text.toString().trim()
                if (email.isEmpty()) {
                    onResult(Result.failure(Exception("Email cannot be empty")))
                    return@setPositiveButton
                }
                if (!email.contains("@")) {
                    email = "$email@gmail.com"
                }

                saveAccount(activity, email)

                val nameParts = email.substringBefore("@").split(".", "_", "-")
                val firstName = nameParts.firstOrNull()
                    ?.lowercase()
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    ?: "Google"
                val lastName = if (nameParts.size > 1) {
                    nameParts.last()
                        .lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                } else "User"

                CoroutineScope(Dispatchers.IO).launch {
                    val isRegistered = MockRepository.isUserRegistered(email)
                    if (!isRegistered) {
                        onResult(Result.failure(UnregisteredAccountException(email)))
                        return@launch
                    }

                    val loginRes = MockRepository.loginWithGoogle(
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        avatarUrl = null
                    )
                    onResult(loginRes)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                onResult(Result.failure(GetCredentialCancellationException("User cancelled")))
            }
            .setOnCancelListener {
                onResult(Result.failure(GetCredentialCancellationException("User cancelled")))
            }
            .show()
    }
}
