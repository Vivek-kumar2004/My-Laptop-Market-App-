package com.mylaptopmarket.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

private var monitorStarted = false

internal actual fun startNetworkMonitor() {
    if (monitorStarted) return
    val context = applicationContextOrNull() ?: return
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    monitorStarted = true
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            NetworkStatus.report(true)
        }

        override fun onLost(network: Network) {
            NetworkStatus.report(false)
        }
    }
    val online = manager.activeNetwork?.let { network ->
        manager.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } == true
    NetworkStatus.report(online)
    manager.registerDefaultNetworkCallback(callback)
}
