package com.mylaptopmarket.util

import android.content.Context

private const val PREFS_NAME = "mlm_vendor_session"
private const val KEY_SESSION = "vendor_session"
private const val KEY_ADMIN_SESSION = "admin_session"
private const val KEY_WISHLIST = "wishlist"
private const val KEY_SEEN_ANNOUNCEMENT = "seen_announcement"
private const val KEY_VENDOR_TUTORIAL = "vendor_flow_tour_seen"

internal actual fun readSessionJson(): String? {
    val context = applicationContextOrNull() ?: return null
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_SESSION, null)
}

internal actual fun writeSessionJson(json: String?) {
    val context = applicationContextOrNull() ?: return
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply {
            if (json.isNullOrBlank()) remove(KEY_SESSION) else putString(KEY_SESSION, json)
        }
        .apply()
}

internal actual fun readAdminSessionJson(): String? {
    val context = applicationContextOrNull() ?: return null
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_ADMIN_SESSION, null)
}

internal actual fun writeAdminSessionJson(json: String?) {
    val context = applicationContextOrNull() ?: return
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply {
            if (json.isNullOrBlank()) remove(KEY_ADMIN_SESSION) else putString(KEY_ADMIN_SESSION, json)
        }
        .apply()
}

internal actual fun readWishlistJson(): String? {
    val context = applicationContextOrNull() ?: return null
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_WISHLIST, null)
}

internal actual fun writeWishlistJson(json: String?) {
    val context = applicationContextOrNull() ?: return
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply {
            if (json.isNullOrBlank()) remove(KEY_WISHLIST) else putString(KEY_WISHLIST, json)
        }
        .apply()
}

internal actual fun readSeenAnnouncement(): String? {
    val context = applicationContextOrNull() ?: return null
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_SEEN_ANNOUNCEMENT, null)
}

internal actual fun readVendorTutorialSeen(): Boolean {
    val context = applicationContextOrNull() ?: return false
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_VENDOR_TUTORIAL, false)
}

internal actual fun writeVendorTutorialSeen(seen: Boolean) {
    val context = applicationContextOrNull() ?: return
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_VENDOR_TUTORIAL, seen)
        .apply()
}

internal actual fun writeSeenAnnouncement(message: String?) {
    val context = applicationContextOrNull() ?: return
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply {
            if (message.isNullOrBlank()) remove(KEY_SEEN_ANNOUNCEMENT) else putString(KEY_SEEN_ANNOUNCEMENT, message)
        }
        .apply()
}
