package com.mylaptopmarket.util

import androidx.compose.runtime.Composable
import com.mylaptopmarket.service.SpeechRecognitionService

expect fun ensureSpeechServiceInitialized(service: SpeechRecognitionService)

@Composable
expect fun RequestMicrophonePermission()
