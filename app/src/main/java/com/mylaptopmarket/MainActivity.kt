package com.mylaptopmarket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mylaptopmarket.service.globalSpeechService
import com.mylaptopmarket.util.initPlatformContext
import com.mylaptopmarket.util.initializePlatformContext

class MainActivity : ComponentActivity() {

    private var deepLinkUri by mutableStateOf<Uri?>(null)
    private val playStoreUpdate = PlayStoreUpdate(this)

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashView ->
                splashView.remove()
            }
        }
        super.onCreate(savedInstanceState)
        playStoreUpdate.start()

        Log.d("MainActivity", "onCreate started")
        deepLinkUri = intent?.data

        initPlatformContext(this)
        initializePlatformContext(this)
        PhoneAuthInstaller.install(this)
        com.mylaptopmarket.network.SessionManager.restore()
        requestNotificationPermission()

        try {
            globalSpeechService.initialize(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize speech service", e)
        }

        enableEdgeToEdge()
        setContent {
            App(
                deepLinkUri = deepLinkUri?.toString(),
                onDeepLinkConsumed = { deepLinkUri = null }
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkUri = intent.data
    }

    override fun onResume() {
        super.onResume()
        playStoreUpdate.check()
    }

    override fun onDestroy() {
        playStoreUpdate.stop()
        super.onDestroy()
        try {
            globalSpeechService.cleanup()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error cleaning up speech service", e)
        }
    }
}
