package com.mylaptopmarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mylaptopmarket.util.initPlatformContext
import com.mylaptopmarket.util.initializePlatformContext

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPlatformContext(this)
        initializePlatformContext(this)
        enableEdgeToEdge()
        setContent {
            AdminApp()
        }
    }
}
