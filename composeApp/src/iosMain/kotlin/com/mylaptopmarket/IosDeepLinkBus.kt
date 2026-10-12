package com.mylaptopmarket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object IosDeepLinkBus {
    private val _uri = MutableStateFlow<String?>(null)
    val uri: StateFlow<String?> = _uri.asStateFlow()

    fun publish(uri: String?) {
        _uri.value = uri
    }

    fun consume() {
        _uri.value = null
    }
}
