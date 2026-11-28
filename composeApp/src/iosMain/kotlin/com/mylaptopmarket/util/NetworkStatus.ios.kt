package com.mylaptopmarket.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

private var monitorStarted = false

@OptIn(ExperimentalForeignApi::class)
internal actual fun startNetworkMonitor() {
    if (monitorStarted) return
    monitorStarted = true
    val monitor = nw_path_monitor_create()
    nw_path_monitor_set_update_handler(monitor) { path ->
        val online = path != null && nw_path_get_status(path) == nw_path_status_satisfied
        NetworkStatus.report(online)
    }
    val queue = dispatch_queue_create("com.mylaptopmarket.network", null)
    nw_path_monitor_set_queue(monitor, queue)
    nw_path_monitor_start(monitor)
}
