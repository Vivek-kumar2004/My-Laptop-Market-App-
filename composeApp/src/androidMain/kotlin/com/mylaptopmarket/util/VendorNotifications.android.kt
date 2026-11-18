package com.mylaptopmarket.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mylaptopmarket.network.VendorSession
import com.mylaptopmarket.repository.ApiAdminRepository
import com.mylaptopmarket.repository.ApiMessageRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

private const val CHANNEL_ID = "mlm_alerts"
private const val PREFS = "mlm_notification_state"
private const val KEY_BASELINE = "has_baseline"
private const val KEY_UNREAD_PREFIX = "unread_"
private const val KEY_ANNOUNCEMENT = "announcement"
private const val WORK_NAME = "mlm_vendor_notifications"
private const val ANNOUNCEMENT_ID = 42

private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
private val pollMutex = Mutex()
private var pollJob: Job? = null
private var watchingVendorId: Int? = null

private val json = Json { ignoreUnknownKeys = true }

actual fun ensureVendorNotifications(vendorId: Int?) {
    val context = applicationContextOrNull() ?: return
    if (vendorId == null) {
        pollJob?.cancel()
        pollJob = null
        watchingVendorId = null
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        return
    }
    ensureChannel(context)
    scheduleBackgroundChecks(context)
    if (pollJob?.isActive == true && watchingVendorId == vendorId) return
    pollJob?.cancel()
    watchingVendorId = vendorId
    pollJob = scope.launch {
        while (isActive) {
            runCatching { pollVendorNotifications() }
            delay(20_000)
        }
    }
}

internal suspend fun pollVendorNotifications() {
    val context = applicationContextOrNull() ?: return
    pollMutex.withLock {
        val vendorId = currentVendorId() ?: return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val conversations = ApiMessageRepository().getConversations(vendorId).getOrNull().orEmpty()
        val hasBaseline = prefs.getBoolean(KEY_BASELINE, false)
        val editor = prefs.edit()
        conversations.forEach { conversation ->
            val key = KEY_UNREAD_PREFIX + conversation.contact_id
            val previous = prefs.getInt(key, 0)
            val isNew = if (hasBaseline) {
                conversation.unread_count > previous
            } else {
                conversation.unread_count > 0
            }
            if (isNew && conversation.unread_count > 0) {
                showNotification(
                    context = context,
                    id = 1000 + conversation.contact_id,
                    title = conversation.contact_name.ifBlank { "New message" },
                    body = conversation.last_message.ifBlank { "You have a new message" }
                )
            }
            editor.putInt(key, conversation.unread_count)
        }
        val announcement = ApiAdminRepository().currentAnnouncement(forVendorApp = true).getOrNull().orEmpty().trim()
        val alreadySeen = seenAnnouncement()
        val alreadyNotified = prefs.getString(KEY_ANNOUNCEMENT, "").orEmpty()
        if (announcement.isNotEmpty() && announcement != alreadySeen && announcement != alreadyNotified) {
            showNotification(
                context = context,
                id = ANNOUNCEMENT_ID,
                title = "My Laptop Market",
                body = announcement
            )
            editor.putString(KEY_ANNOUNCEMENT, announcement)
        }
        editor.putBoolean(KEY_BASELINE, true).apply()
    }
}

private fun currentVendorId(): Int? {
    val raw = readSessionJson() ?: return null
    return runCatching { json.decodeFromString<VendorSession>(raw).id }.getOrNull()
}

private fun scheduleBackgroundChecks(context: Context) {
    val request = PeriodicWorkRequestBuilder<VendorNotificationWorker>(15, TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

private fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(NotificationManager::class.java) ?: return
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Messages and alerts",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "New messages and marketplace notices"
    }
    manager.createNotificationChannel(channel)
}

private fun showNotification(context: Context, id: Int, title: String, body: String) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    } ?: return
    val pending = PendingIntent.getActivity(
        context,
        id,
        launch,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val appIcon = context.resources.getIdentifier("ic_stat_notify", "drawable", context.packageName)
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(if (appIcon != 0) appIcon else android.R.drawable.stat_notify_chat)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setAutoCancel(true)
        .setContentIntent(pending)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
    NotificationManagerCompat.from(context).notify(id, notification)
}
