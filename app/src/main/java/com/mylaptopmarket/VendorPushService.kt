package com.mylaptopmarket

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mylaptopmarket.network.VendorPushEvents
import com.mylaptopmarket.util.ensureAlertChannel

class VendorPushService : FirebaseMessagingService() {
    override fun onCreate() {
        super.onCreate()
        ensureAlertChannel()
    }

    override fun onNewToken(token: String) {
        VendorPushEvents.onToken(token, "production", "android")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        val type = message.data["type"].orEmpty()
        val targetId = message.data["target_id"].orEmpty()
        val targetName = message.data["target_name"].orEmpty()
        show(title, body, type, targetId, targetName)
    }

    private fun show(title: String, body: String, type: String, targetId: String, targetName: String) {
        ensureChannel()
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("type", type)
            putExtra("target_id", targetId)
            putExtra("target_name", targetName)
        }
        val pending = PendingIntent.getActivity(
            this,
            title.hashCode(),
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val icon = resources.getIdentifier("ic_stat_notify", "drawable", packageName)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(if (icon != 0) icon else android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.notify(title.hashCode(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Messages and alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "mlm_alerts"
    }
}
