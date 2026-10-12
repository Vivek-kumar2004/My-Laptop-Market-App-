package com.mylaptopmarket.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class VendorNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        initPlatformContext(applicationContext)
        initializePlatformContext(applicationContext)
        return runCatching {
            pollVendorNotifications()
            Result.success()
        }.getOrDefault(Result.success())
    }
}
