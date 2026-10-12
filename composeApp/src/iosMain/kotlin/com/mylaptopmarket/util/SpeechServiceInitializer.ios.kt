package com.mylaptopmarket.util

import androidx.compose.runtime.Composable
import com.mylaptopmarket.service.SpeechRecognitionService

actual fun ensureSpeechServiceInitialized(service: SpeechRecognitionService) {
    // No-op on iOS
}

@Composable
actual fun RequestMicrophonePermission() {
}
