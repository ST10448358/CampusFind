package com.segotlo.campusfindapp.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.segotlo.campusfindapp.data.MockRepository
import com.segotlo.campusfindapp.data.User

object GoogleAuthManager {

    private const val WEB_CLIENT_ID = "566079707733-dummyclientidforcampusfindapp.apps.googleusercontent.com"

    suspend fun signInWithGoogle(context: Context): Result<User> {
        return try {
            val availability = try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            } catch (_: SecurityException) {
                ConnectionResult.SERVICE_INVALID
            } catch (_: Throwable) {
                ConnectionResult.SERVICE_INVALID
            }

            if (availability != ConnectionResult.SUCCESS) {
                return MockRepository.loginWithGoogle(
                    email = "google.student@up.ac.za",
                    firstName = "Google",
                    lastName = "Student",
                    avatarUrl = null
                )
            }

            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = try {
                credentialManager.getCredential(context, request)
            } catch (e: SecurityException) {
                val fallbackEmail = "google.student@up.ac.za"
                return MockRepository.loginWithGoogle(
                    email = fallbackEmail,
                    firstName = "Google",
                    lastName = "Student",
                    avatarUrl = null
                )
            }
            val credential = response.credential

            if (credential is GoogleIdTokenCredential) {
                val email = credential.id
                val displayName = credential.displayName ?: "Google User"
                val nameParts = displayName.split(" ")
                val firstName = nameParts.firstOrNull() ?: "Google"
                val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else "User"

                MockRepository.loginWithGoogle(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    avatarUrl = credential.profilePictureUri?.toString()
                )
            } else {
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: SecurityException) {
            // Fallback for SecurityException from GoogleApiManager / broker
            val fallbackEmail = "google.student@up.ac.za"
            MockRepository.loginWithGoogle(
                email = fallbackEmail,
                firstName = "Google",
                lastName = "Student",
                avatarUrl = null
            )
        } catch (e: Throwable) {
            // Fallback for emulator without Play Services credentials or general errors
            val fallbackEmail = "google.student@up.ac.za"
            MockRepository.loginWithGoogle(
                email = fallbackEmail,
                firstName = "Google",
                lastName = "Student",
                avatarUrl = null
            )
        }
    }
}