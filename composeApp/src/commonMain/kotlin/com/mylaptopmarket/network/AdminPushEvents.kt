package com.mylaptopmarket.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdminPushToken(
    val token: String,
    val environment: String
)

object AdminPushEvents {
    private val _token = MutableStateFlow<AdminPushToken?>(null)
    val token: StateFlow<AdminPushToken?> = _token.asStateFlow()

    private val _openTarget = MutableStateFlow<String?>(null)
    val openTarget: StateFlow<String?> = _openTarget.asStateFlow()

    fun onToken(token: String, environment: String) {
        if (token.isBlank()) return
        _token.value = AdminPushToken(token = token, environment = environment)
    }

    fun onOpen(target: String) {
        if (target.isBlank()) return
        _openTarget.value = target
    }

    fun consumeOpen() {
        _openTarget.value = null
    }
}
