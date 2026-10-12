package com.mylaptopmarket.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

actual class SpeechRecognitionService {
    private var context: Context? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var currentLanguageCode: String = "en-US"
    
    private var onResultCallback: ((String) -> Unit)? = null
    private var onFinalResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var onSpeakDoneCallback: (() -> Unit)? = null
    
    private var isTTSInitialized = false
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) {
            println("SpeechRecognitionService already initialized")
            return
        }
        
        this.context = context.applicationContext
        
        // Initialize Speech Recognizer
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                setupRecognitionListener()
                println("SpeechRecognizer initialized successfully")
            } else {
                println("Speech recognition not available on this device")
            }
        } catch (e: Exception) {
            println("Error initializing SpeechRecognizer: ${e.message}")
            e.printStackTrace()
        }
        
        // Initialize Text to Speech
        try {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTTSInitialized = true
                    textToSpeech?.language = Locale("en", "IN")
                    setupTTSListener()
                    println("TTS initialized successfully")
                } else {
                    println("TTS initialization failed")
                }
            }
        } catch (e: Exception) {
            println("Error initializing TTS: ${e.message}")
            e.printStackTrace()
        }
        
        isInitialized = true
        println("SpeechRecognitionService initialization complete")
    }

    actual fun setLanguage(languageCode: String) {
        currentLanguageCode = languageCode
        
        // Set TTS language
        val locale = when (languageCode) {
            "hi-IN" -> Locale("hi", "IN")
            "en-IN" -> Locale("en", "IN")
            else -> Locale.US
        }
        textToSpeech?.language = locale
    }

    actual fun startListening(languageCode: String) {
        if (!isInitialized || context == null) {
            val errorMsg = "Service not initialized. Please restart the app."
            println("ERROR: $errorMsg")
            onErrorCallback?.invoke(errorMsg)
            return
        }
        
        val ctx = context!!
        setLanguage(languageCode)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            
            // Enable on-device recognition if available (better for offline)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            
            // For better Hinglish support
            if (languageCode == "en-IN") {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en-IN")
            }
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onErrorCallback?.invoke("Failed to start recognition: ${e.message}")
        }
    }

    actual fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            println("Error stopping speech recognizer: ${e.message}")
        }
    }

    actual fun speak(text: String, languageCode: String) {
        if (!isTTSInitialized) {
            println("TTS not initialized")
            onSpeakDoneCallback?.invoke()
            return
        }

        setLanguage(languageCode)
        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    actual fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            println("Error stopping TTS: ${e.message}")
        }
    }

    actual fun setOnResultCallback(callback: (String) -> Unit) {
        onResultCallback = callback
    }

    actual fun setOnFinalResultCallback(callback: (String) -> Unit) {
        onFinalResultCallback = callback
    }

    actual fun setOnErrorCallback(callback: (String) -> Unit) {
        onErrorCallback = callback
    }

    actual fun setOnSpeakDoneCallback(callback: () -> Unit) {
        onSpeakDoneCallback = callback
    }

    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                println("Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                println("Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Volume changed - can be used for UI feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }

            override fun onEndOfSpeech() {
                println("Speech ended")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error: $error"
                }
                
                // Don't show error for "No speech match" as it's common during listening
                if (error != SpeechRecognizer.ERROR_NO_MATCH && 
                    error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    onErrorCallback?.invoke(errorMessage)
                } else {
                    // Just complete with empty result
                    onFinalResultCallback?.invoke("")
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    onFinalResultCallback?.invoke(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    onResultCallback?.invoke(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Event occurred
            }
        })
    }

    private fun setupTTSListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                println("TTS started")
            }

            override fun onDone(utteranceId: String?) {
                println("TTS completed")
                onSpeakDoneCallback?.invoke()
            }

            override fun onError(utteranceId: String?) {
                println("TTS error")
                onSpeakDoneCallback?.invoke()
            }
        })
    }

    actual fun cleanup() {
        println("Cleaning up SpeechRecognitionService")
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer = null
        textToSpeech = null
        context = null
        isInitialized = false
        isTTSInitialized = false
    }
}

// Global instance - will be initialized on first use
val globalSpeechService by lazy { SpeechRecognitionService() }
