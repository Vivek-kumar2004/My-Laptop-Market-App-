package com.mylaptopmarket.util

internal expect fun readSessionJson(): String?

internal expect fun writeSessionJson(json: String?)

internal expect fun readAdminSessionJson(): String?

internal expect fun writeAdminSessionJson(json: String?)

internal expect fun readWishlistJson(): String?

internal expect fun writeWishlistJson(json: String?)

internal expect fun readSeenAnnouncement(): String?

internal expect fun writeSeenAnnouncement(message: String?)

fun seenAnnouncement(): String = readSeenAnnouncement().orEmpty()

fun markAnnouncementSeen(message: String) = writeSeenAnnouncement(message)
