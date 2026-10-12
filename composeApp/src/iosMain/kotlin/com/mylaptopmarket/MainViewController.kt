package com.mylaptopmarket

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val deepLink by IosDeepLinkBus.uri.collectAsState()
    App(
        deepLinkUri = deepLink,
        onDeepLinkConsumed = { IosDeepLinkBus.consume() }
    )
}

fun AdminMainViewController(): UIViewController = ComposeUIViewController {
    AdminApp()
}
