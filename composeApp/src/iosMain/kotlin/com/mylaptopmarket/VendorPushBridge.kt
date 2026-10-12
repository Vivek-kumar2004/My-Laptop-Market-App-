package com.mylaptopmarket

import com.mylaptopmarket.network.VendorPushEvents

object VendorPushBridge {
    fun onToken(token: String, environment: String) {
        VendorPushEvents.onToken(token, environment)
    }

    fun onOpen(type: String, targetId: String, targetName: String) {
        VendorPushEvents.onOpen(type, targetId, targetName)
    }
}
