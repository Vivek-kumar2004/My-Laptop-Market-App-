package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.repository.ProductRepository
import com.mylaptopmarket.service.ChatbotAIService
import com.mylaptopmarket.service.SpeechRecognitionService
import com.mylaptopmarket.ui.screens.ChatMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

private var messageIdCounter = 0
private fun generateId(): String {
    messageIdCounter++
    return "msg-${messageIdCounter}-${Random.nextInt(0, 999999)}"
}

enum class AiStatus { Checking, Online, Offline }

class VoiceChatbotViewModel(
    private val speechRecognitionService: SpeechRecognitionService,
    private val chatbotAIService: ChatbotAIService,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isConversationMode = MutableStateFlow(false)
    val isConversationMode: StateFlow<Boolean> = _isConversationMode.asStateFlow()

    private val _currentTranscript = MutableStateFlow("")
    val currentTranscript: StateFlow<String> = _currentTranscript.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _aiStatus = MutableStateFlow(AiStatus.Checking)
    val aiStatus: StateFlow<AiStatus> = _aiStatus.asStateFlow()

    private val _voiceReadoutEnabled = MutableStateFlow(true)
    val voiceReadoutEnabled: StateFlow<Boolean> = _voiceReadoutEnabled.asStateFlow()

    private val languageCode = "en-IN"
    private val promptLanguage = "hinglish"

    private var speakDoneSignal: CompletableDeferred<Unit>? = null

    init {
        try {
            speechRecognitionService.setOnResultCallback { transcript ->
                _currentTranscript.value = transcript
            }

            speechRecognitionService.setOnFinalResultCallback { finalTranscript ->
                _currentTranscript.value = ""
                _isListening.value = false
                if (finalTranscript.isNotEmpty()) {
                    handleUserMessage(finalTranscript)
                } else if (_isConversationMode.value) {
                    resumeListeningSoon()
                }
            }

            speechRecognitionService.setOnErrorCallback { error ->
                _isListening.value = false
                _currentTranscript.value = ""
                val fatal = error.contains("Settings", ignoreCase = true) ||
                    error.contains("denied", ignoreCase = true) ||
                    error.contains("not available", ignoreCase = true) ||
                    error.contains("Microphone is off", ignoreCase = true)
                val ignore = error.contains("cancel", ignoreCase = true) ||
                    error.contains("No speech", ignoreCase = true)
                when {
                    ignore && _isConversationMode.value -> resumeListeningSoon()
                    fatal -> {
                        _isConversationMode.value = false
                        _errorMessage.value = error
                    }
                    _isConversationMode.value -> resumeListeningSoon()
                    else -> _errorMessage.value = error
                }
            }

            speechRecognitionService.setOnSpeakDoneCallback {
                _isSpeaking.value = false
                speakDoneSignal?.complete(Unit)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to initialize voice recognition. Please restart the app."
        }

        checkAiStatus()
    }

    fun checkAiStatus() {
        viewModelScope.launch {
            _aiStatus.value = AiStatus.Checking
            _aiStatus.value = if (chatbotAIService.checkConnection()) {
                AiStatus.Online
            } else {
                AiStatus.Offline
            }
        }
    }

    fun toggleVoiceReadout() {
        val enabled = !_voiceReadoutEnabled.value
        _voiceReadoutEnabled.value = enabled
        if (!enabled) {
            _isSpeaking.value = false
            speechRecognitionService.stopSpeaking()
            speakDoneSignal?.complete(Unit)
        }
    }

    fun toggleConversationMode() {
        if (_isConversationMode.value) {
            stopConversation()
        } else {
            startConversation()
        }
    }

    private fun startConversation() {
        _errorMessage.value = null
        _isConversationMode.value = true
        speechRecognitionService.stopSpeaking()
        startListening()
    }

    private fun stopConversation() {
        _isConversationMode.value = false
        _isListening.value = false
        _isSpeaking.value = false
        _currentTranscript.value = ""
        speechRecognitionService.stopListening()
        speechRecognitionService.stopSpeaking()
        speakDoneSignal?.complete(Unit)
    }

    fun startListening() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                speechRecognitionService.stopSpeaking()
                delay(250)
                _isListening.value = true
                speechRecognitionService.startListening(languageCode)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start listening: ${e.message}"
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        speechRecognitionService.stopListening()
        _isListening.value = false
    }

    fun sendTypedMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isProcessing.value) return
        _errorMessage.value = null
        _currentTranscript.value = ""
        handleUserMessage(trimmed)
    }

    private fun resumeListeningSoon() {
        viewModelScope.launch {
            delay(500)
            if (_isConversationMode.value && !_isProcessing.value && !_isSpeaking.value && !_isListening.value) {
                startListening()
            }
        }
    }

    private fun handleUserMessage(text: String) {
        viewModelScope.launch {
            val userMessage = ChatMessage(
                id = generateId(),
                text = text,
                isUser = true,
                language = promptLanguage
            )
            _messages.value = _messages.value + userMessage

            _isProcessing.value = true
            _errorMessage.value = null

            try {
                val products = productRepository.getAllProducts()
                val reply = chatbotAIService.processQuery(
                    query = text,
                    language = promptLanguage,
                    productsContext = products
                )

                if (reply.fromAi) {
                    _aiStatus.value = AiStatus.Online
                }

                val aiMessage = ChatMessage(
                    id = generateId(),
                    text = reply.text,
                    isUser = false,
                    language = promptLanguage,
                    fromAi = reply.fromAi
                )
                _messages.value = _messages.value + aiMessage

                if (_voiceReadoutEnabled.value) {
                    speakAndWait(stripMarkdownForSpeech(reply.text))
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process query: ${e.message}"
                val errorMsg = ChatMessage(
                    id = generateId(),
                    text = "Sorry, I couldn't process your request. Please try again.",
                    isUser = false
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isProcessing.value = false
            }

            if (_isConversationMode.value) {
                resumeListeningSoon()
            }
        }
    }

    private suspend fun speakAndWait(text: String) {
        if (text.isBlank()) return
        val signal = CompletableDeferred<Unit>()
        speakDoneSignal = signal
        _isSpeaking.value = true
        try {
            speechRecognitionService.speak(text, languageCode)
            withTimeoutOrNull(90_000) { signal.await() }
        } catch (e: Exception) {
            println("TTS error: ${e.message}")
        } finally {
            _isSpeaking.value = false
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopConversation()
        speechRecognitionService.cleanup()
    }
}

fun stripMarkdownForSpeech(text: String): String {
    return text
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("\\*(.+?)\\*"), "$1")
        .replace(Regex("`(.+?)`"), "$1")
        .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "")
        .replace("*", "")
        .trim()
}
