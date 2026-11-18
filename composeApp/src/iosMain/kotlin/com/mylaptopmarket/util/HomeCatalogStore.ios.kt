package com.mylaptopmarket.util

import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun appCacheDir(): String {
    val first = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).firstOrNull()
    return (first as? String).orEmpty()
}

internal actual fun prefetchImages(urls: List<String>) {
    val context = PlatformContext.INSTANCE
    val loader = runCatching { SingletonImageLoader.get(context) }.getOrNull() ?: return
    urls.distinct().take(180).forEach { url ->
        if (!url.startsWith("http")) return@forEach
        loader.enqueue(
            ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
        )
    }
}
