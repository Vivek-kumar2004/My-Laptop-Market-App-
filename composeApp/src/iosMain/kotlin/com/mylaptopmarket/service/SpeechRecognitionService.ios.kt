package com.mylaptopmarket.service

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechUtterance
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual class SpeechRecognitionService {

    private var onResultCallback: ((String) -> Unit)? = null
    private var onFinalResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var onSpeakDoneCallback: (() -> Unit)? = null

    private var synthesizer: AVSpeechSynthesizer? = null
    private var speakDelegate: IosSpeakDelegate? = null

    actual fun setLanguage(languageCode: String) {
        // Locale is handled by the native iOS speech helper.
    }

    actual fun startListening(languageCode: String) {
        onMain {
            bindBridge()
            val start = IosSpeechBridge.startListening
            if (start == null) {
                onErrorCallback?.invoke("Microphone is not ready. Close the app and open it again.")
                return@onMain
            }
            start(languageCode)
        }
    }

    actual fun stopListening() {
        onMain { IosSpeechBridge.stopListening?.invoke() }
    }

    actual fun speak(text: String, languageCode: String) {
        onMain {
            IosSpeechBridge.stopListening?.invoke()
            if (text.isBlank()) {
                onSpeakDoneCallback?.invoke()
                return@onMain
            }
            val synth = synthesizer ?: AVSpeechSynthesizer().also { created ->
                val delegate = IosSpeakDelegate { onSpeakDoneCallback?.invoke() }
                speakDelegate = delegate
                created.delegate = delegate
                synthesizer = created
            }
            val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
            utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(mapLocale(languageCode))
                ?: AVSpeechSynthesisVoice.voiceWithLanguage("en-US")
            synth.speakUtterance(utterance)
        }
    }

    actual fun stopSpeaking() {
        onMain {
            synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
    }

    actual fun setOnResultCallback(callback: (String) -> Unit) {
        onResultCallback = callback
        bindBridge()
    }

    actual fun setOnFinalResultCallback(callback: (String) -> Unit) {
        onFinalResultCallback = callback
        bindBridge()
    }

    actual fun setOnErrorCallback(callback: (String) -> Unit) {
        onErrorCallback = callback
        bindBridge()
    }

    actual fun setOnSpeakDoneCallback(callback: () -> Unit) {
        onSpeakDoneCallback = callback
    }

    actual fun cleanup() {
        onMain {
            IosSpeechBridge.stopListening?.invoke()
            synthesizer?.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
            synthesizer?.delegate = null
            synthesizer = null
            speakDelegate = null
        }
    }

    private fun bindBridge() {
        IosSpeechBridge.bind(
            onPartial = { onResultCallback?.invoke(it) },
            onFinal = { onFinalResultCallback?.invoke(it) },
            onError = { onErrorCallback?.invoke(it) }
        )
    }

    private fun mapLocale(languageCode: String): String = when (languageCode) {
        "hi-IN" -> "hi-IN"
        "en-IN" -> "en-IN"
        else -> "en-US"
    }

    private fun onMain(block: () -> Unit) {
        dispatch_async(dispatch_get_main_queue()) { block() }
    }
}

private class IosSpeakDelegate(
    private val onDone: () -> Unit
) : NSObject(), AVSpeechSynthesizerDelegateProtocol {
    override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        didFinishSpeechUtterance: AVSpeechUtterance
    ) {
        onDone()
    }
}
