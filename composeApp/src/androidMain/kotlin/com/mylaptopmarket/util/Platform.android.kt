package com.mylaptopmarket.util

import android.content.Context
import android.content.Intent
import android.net.Uri

private lateinit var appContext: Context

fun initPlatformContext(context: Context) {
    appContext = context.applicationContext
}

internal fun applicationContextOrNull(): Context? =
    if (::appContext.isInitialized) appContext else null

actual fun makeCall(phoneNumber: String) {
    val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$cleanNumber")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext.startActivity(intent)
}

actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext.startActivity(intent)
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun currentHourOfDay(): Int =
    java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
