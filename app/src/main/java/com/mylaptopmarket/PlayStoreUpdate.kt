package com.mylaptopmarket

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

class PlayStoreUpdate(
    private val activity: ComponentActivity
) {
    private val launcher: ActivityResultLauncher<IntentSenderRequest> =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { }

    private var manager: AppUpdateManager? = null

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            manager?.completeUpdate()
        }
    }

    fun start() {
        if (manager != null) return
        val created = AppUpdateManagerFactory.create(activity)
        manager = created
        created.registerListener(listener)
    }

    fun stop() {
        manager?.unregisterListener(listener)
    }

    fun check() {
        val manager = manager ?: return
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                manager.completeUpdate()
                return@addOnSuccessListener
            }
            val resume = info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            if (!resume && !available) return@addOnSuccessListener
            val type = when {
                info.isImmediateUpdateAllowed && (resume || !info.isFlexibleUpdateAllowed) -> AppUpdateType.IMMEDIATE
                info.isFlexibleUpdateAllowed -> AppUpdateType.FLEXIBLE
                info.isImmediateUpdateAllowed -> AppUpdateType.IMMEDIATE
                else -> return@addOnSuccessListener
            }
            manager.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(type).build()
            )
        }
    }
}
