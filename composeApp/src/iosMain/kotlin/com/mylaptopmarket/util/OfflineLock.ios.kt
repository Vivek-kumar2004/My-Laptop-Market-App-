package com.mylaptopmarket.util

import platform.Foundation.NSLock

private val lock = NSLock()

internal actual fun <T> withOfflineLock(block: () -> T): T {
    lock.lock()
    return try {
        block()
    } finally {
        lock.unlock()
    }
}
