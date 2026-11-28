package com.mylaptopmarket.util

internal expect fun <T> withOfflineLock(block: () -> T): T
