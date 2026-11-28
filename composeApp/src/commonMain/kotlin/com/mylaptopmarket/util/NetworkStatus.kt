package com.mylaptopmarket.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkStatus {
    private val _online = MutableStateFlow(true)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    private val _backOnline = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val backOnline: SharedFlow<Unit> = _backOnline.asSharedFlow()

    private var last: Boolean? = null

    fun report(online: Boolean) {
        val rose = withOfflineLock {
            val previous = last
            last = online
            previous == false && online
        }
        _online.value = online
        if (rose) _backOnline.tryEmit(Unit)
    }

    fun start() {
        startNetworkMonitor()
    }
}

internal expect fun startNetworkMonitor()

object OfflineSync {
    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision.asStateFlow()

    fun noteRefresh() {
        _revision.value = _revision.value + 1
    }
}
