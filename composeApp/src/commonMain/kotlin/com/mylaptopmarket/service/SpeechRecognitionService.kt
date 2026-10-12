package com.mylaptopmarket.service

expect class SpeechRecognitionService() {
    fun setLanguage(languageCode: String)
    fun startListening(languageCode: String)
    fun stopListening()
    fun speak(text: String, languageCode: String)
    fun stopSpeaking()
    fun setOnResultCallback(callback: (String) -> Unit)
    fun setOnFinalResultCallback(callback: (String) -> Unit)
    fun setOnErrorCallback(callback: (String) -> Unit)
    fun setOnSpeakDoneCallback(callback: () -> Unit)
    fun cleanup()
}
