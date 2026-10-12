package com.mylaptopmarket

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mylaptopmarket.ui.navigation.NavGraph
import com.mylaptopmarket.ui.navigation.Screen
import com.mylaptopmarket.ui.theme.MyLaptopMarketTheme
import com.mylaptopmarket.network.SessionManager

@Composable
fun App(
    deepLinkUri: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    MyLaptopMarketTheme {
        remember { SessionManager.restore() }
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            val navController = rememberNavController()

            LaunchedEffect(deepLinkUri) {
                val uri = deepLinkUri ?: return@LaunchedEffect
                if (uri.startsWith("mylaptopmarket://payment-success")) {
                    val vendorId = uri.substringAfter("vendorId=", "")
                        .substringBefore("&")
                        .toIntOrNull()
                    navController.navigate(Screen.SellerVerification.createRoute(vendorId)) {
                        launchSingleTop = true
                    }
                    onDeepLinkConsumed()
                }
            }

            NavGraph(navController = navController)
        }
    }
}
