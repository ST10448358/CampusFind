package com.segotlo.campusfindapp.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.User
import kotlinx.coroutines.tasks.await

object GoogleAuthManager {

    private const val WEB_CLIENT_ID = "566079707733-hcp80kf8lvf4vbk97nk485tnec1ebquq.apps.googleusercontent.com"

    suspend fun signInWithGoogle(context: Context): Result<User> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            if (credential is GoogleIdTokenCredential) {
                val idToken = credential.idToken
                val email = credential.id
                val displayName = credential.displayName ?: "Google User"
                val nameParts = displayName.split(" ")
                val firstName = nameParts.firstOrNull() ?: "Google"
                val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else "User"

                try {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential).await()
                } catch (e: Throwable) {
                    return Result.failure(Exception("Firebase Auth failed: ${e.message}"))
                }

                MockRepository.loginWithGoogle(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    avatarUrl = credential.profilePictureUri?.toString()
                )
            } else {
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Google sign-in error"))
        }
    }
}
