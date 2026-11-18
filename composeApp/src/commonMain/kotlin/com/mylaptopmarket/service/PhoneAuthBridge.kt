package com.mylaptopmarket.service

object PhoneAuthBridge {
    var start: ((String) -> Unit)? = null
    var confirm: ((String, String) -> Unit)? = null

    private var onCodeSent: ((String) -> Unit)? = null
    private var onVerified: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    fun bind(
        onCodeSent: (String) -> Unit,
        onVerified: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onCodeSent = onCodeSent
        this.onVerified = onVerified
        this.onError = onError
    }

    fun emitCodeSent(verificationId: String) {
        onCodeSent?.invoke(verificationId)
    }

    fun emitVerified(idToken: String) {
        onVerified?.invoke(idToken)
    }

    fun emitError(message: String) {
        onError?.invoke(message)
    }
}
