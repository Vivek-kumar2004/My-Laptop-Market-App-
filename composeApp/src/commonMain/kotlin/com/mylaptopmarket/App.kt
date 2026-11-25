package com.mylaptopmarket

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.mylaptopmarket.util.appCacheDir
import okio.Path.Companion.toPath
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ApiAuthRepository
import com.mylaptopmarket.repository.AuthNeedsProfileException
import com.mylaptopmarket.repository.AuthPendingException
import com.mylaptopmarket.ui.navigation.NavGraph
import com.mylaptopmarket.ui.navigation.Screen
import com.mylaptopmarket.ui.theme.MyLaptopMarketTheme
import com.mylaptopmarket.viewmodel.SignInNotice

@Composable
fun App(
    deepLinkUri: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    setSingletonImageLoaderFactory { context ->
        val cacheDir = appCacheDir()
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .apply {
                if (cacheDir.isNotBlank()) {
                    diskCache {
                        DiskCache.Builder()
                            .directory("$cacheDir/image_cache".toPath())
                            .maxSizeBytes(200L * 1024 * 1024)
                            .build()
                    }
                }
            }
            .build()
    }
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
                } else if (uri.startsWith("mylaptopmarket://auth")) {
                    val code = uri.substringAfter("code=", "").substringBefore("&")
                    if (code.isBlank()) {
                        SignInNotice.show("Google sign-in could not be finished.")
                    } else {
                        val result = ApiAuthRepository().loginWithFirebaseCode(code)
                        result.onSuccess { session ->
                            if (!session.profileComplete) {
                                SignInNotice.clear()
                                navController.navigate(
                                    Screen.CompleteProfile.createRoute(session.id, session.email)
                                ) {
                                    launchSingleTop = true
                                }
                            } else {
                                SessionManager.login(session, promptShopTrust = true)
                                SignInNotice.clear()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }.onFailure { error ->
                            when {
                                error is AuthNeedsProfileException && error.vendorId > 0 -> {
                                    SignInNotice.clear()
                                    navController.navigate(
                                        Screen.CompleteProfile.createRoute(error.vendorId, error.email)
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                                error is AuthPendingException && error.pending.vendorId > 0 -> {
                                    navController.navigate(
                                        Screen.SellerVerification.createRoute(error.pending.vendorId)
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                                else -> {
                                    SignInNotice.show(
                                        error.message ?: "Google sign-in could not be finished."
                                    )
                                }
                            }
                        }
                    }
                    onDeepLinkConsumed()
                }
            }

            NavGraph(navController = navController)
        }
    }
}
