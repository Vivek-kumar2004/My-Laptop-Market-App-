package com.mylaptopmarket.util

import platform.Foundation.NSUserDefaults

private const val KEY_SESSION = "vendor_session"
private const val KEY_ADMIN_SESSION = "admin_session"
private const val KEY_WISHLIST = "wishlist"
private const val KEY_SEEN_ANNOUNCEMENT = "seen_announcement"

internal actual fun readSessionJson(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_SESSION)

internal actual fun writeSessionJson(json: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (json.isNullOrBlank()) {
        defaults.removeObjectForKey(KEY_SESSION)
    } else {
        defaults.setObject(json, forKey = KEY_SESSION)
    }
}

internal actual fun readAdminSessionJson(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_ADMIN_SESSION)

internal actual fun writeAdminSessionJson(json: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (json.isNullOrBlank()) {
        defaults.removeObjectForKey(KEY_ADMIN_SESSION)
    } else {
        defaults.setObject(json, forKey = KEY_ADMIN_SESSION)
    }
}

internal actual fun readWishlistJson(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_WISHLIST)

internal actual fun writeWishlistJson(json: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (json.isNullOrBlank()) {
        defaults.removeObjectForKey(KEY_WISHLIST)
    } else {
        defaults.setObject(json, forKey = KEY_WISHLIST)
    }
}

internal actual fun readSeenAnnouncement(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(KEY_SEEN_ANNOUNCEMENT)

internal actual fun writeSeenAnnouncement(message: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    if (message.isNullOrBlank()) {
        defaults.removeObjectForKey(KEY_SEEN_ANNOUNCEMENT)
    } else {
        defaults.setObject(message, forKey = KEY_SEEN_ANNOUNCEMENT)
    }
}
