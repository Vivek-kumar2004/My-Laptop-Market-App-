package com.mylaptopmarket.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VendorPushToken(
    val token: String,
    val environment: String
)

data class VendorPushOpen(
    val type: String,
    val targetId: String,
    val targetName: String
)

object VendorPushEvents {
    private val _token = MutableStateFlow<VendorPushToken?>(null)
    val token: StateFlow<VendorPushToken?> = _token.asStateFlow()

    private val _open = MutableStateFlow<VendorPushOpen?>(null)
    val open: StateFlow<VendorPushOpen?> = _open.asStateFlow()

    fun onToken(token: String, environment: String) {
        if (token.isBlank()) return
        _token.value = VendorPushToken(token = token, environment = environment)
    }

    fun onOpen(type: String, targetId: String, targetName: String) {
        if (type.isBlank()) return
        _open.value = VendorPushOpen(type = type, targetId = targetId, targetName = targetName)
    }

    fun consumeOpen() {
        _open.value = null
    }
}
