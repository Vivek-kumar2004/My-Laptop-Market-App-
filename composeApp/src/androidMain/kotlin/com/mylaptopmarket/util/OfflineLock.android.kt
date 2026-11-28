package com.mylaptopmarket.util

private val lock = Any()

internal actual fun <T> withOfflineLock(block: () -> T): T = synchronized(lock, block)
