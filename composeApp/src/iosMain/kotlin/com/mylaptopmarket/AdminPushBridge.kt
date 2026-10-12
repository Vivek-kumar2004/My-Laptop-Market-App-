package com.mylaptopmarket

import com.mylaptopmarket.network.AdminPushEvents

object AdminPushBridge {
    fun onToken(token: String, environment: String) {
        AdminPushEvents.onToken(token, environment)
    }

    fun onOpen(target: String) {
        AdminPushEvents.onOpen(target)
    }
}
