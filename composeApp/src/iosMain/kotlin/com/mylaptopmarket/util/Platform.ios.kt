package com.mylaptopmarket.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication

actual fun makeCall(phoneNumber: String) {
    val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }
    val url = NSURL(string = "tel:$cleanNumber") ?: return
    val app = UIApplication.sharedApplication
    if (app.canOpenURL(url)) {
        app.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

actual fun openUrl(url: String) {
    val nsUrl = NSURL(string = url) ?: return
    val app = UIApplication.sharedApplication
    if (app.canOpenURL(nsUrl)) {
        app.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun currentHourOfDay(): Int =
    NSCalendar.currentCalendar.component(NSCalendarUnitHour, fromDate = NSDate()).toInt()
