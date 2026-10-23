package com.segotlo.campusfindapp

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.FirebaseApp
import com.segotlo.campusfindapp.data.MockRepository

class CampusFindAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Force English default on startup to reset any previously stuck non-English locale
        try {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        } catch (_: Throwable) {}

        // Default to Dark Mode on fresh startup
        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } catch (_: Throwable) {}

        // Global uncaught exception handler to log the exact crash stack trace
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CampusFindAppCrash", "FATAL CRASH ON THREAD ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            Log.e("CampusFindAppCrash", "FirebaseApp init error", e)
        }

        try {
            MockRepository.init(this)
        } catch (e: Throwable) {
            Log.e("CampusFindAppCrash", "MockRepository init error", e)
        }
    }
}
