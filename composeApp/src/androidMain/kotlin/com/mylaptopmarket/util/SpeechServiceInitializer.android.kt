package com.mylaptopmarket.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mylaptopmarket.service.SpeechRecognitionService

private var appContext: Context? = null

fun initializePlatformContext(context: Context) {
    appContext = context.applicationContext
    println("Platform context stored: ${appContext != null}")
}

fun getAndroidContext(): Context? {
    return appContext
}

actual fun ensureSpeechServiceInitialized(service: SpeechRecognitionService) {
    try {
        val ctx = appContext
        if (ctx != null) {
            service.initialize(ctx)
            println("Speech service initialized via ensureSpeechServiceInitialized")
        } else {
            println("WARNING: appContext is null, cannot initialize service")
        }
    } catch (e: Exception) {
        println("Error ensuring service initialization: ${e.message}")
        e.printStackTrace()
    }
}

@Composable
actual fun RequestMicrophonePermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
