package com.mylaptopmarket.service

object IosSpeechBridge {
    var startListening: ((String) -> Unit)? = null
    var stopListening: (() -> Unit)? = null

    private var onPartial: ((String) -> Unit)? = null
    private var onFinal: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    fun bind(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onPartial = onPartial
        this.onFinal = onFinal
        this.onError = onError
    }

    fun emitPartial(text: String) {
        onPartial?.invoke(text)
    }

    fun emitFinal(text: String) {
        onFinal?.invoke(text)
    }

    fun emitError(message: String) {
        onError?.invoke(message)
    }
}
