package com.mylaptopmarket.util

import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest

internal actual fun appCacheDir(): String =
    applicationContextOrNull()?.cacheDir?.absolutePath.orEmpty()

internal actual fun prefetchImages(urls: List<String>) {
    val context = applicationContextOrNull() ?: return
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
