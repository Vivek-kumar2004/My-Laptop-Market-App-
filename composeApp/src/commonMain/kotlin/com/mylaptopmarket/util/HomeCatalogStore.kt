package com.mylaptopmarket.util

import com.mylaptopmarket.model.MarketListing
import com.mylaptopmarket.model.Product
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

@Serializable
data class HomeCatalog(
    val products: List<Product> = emptyList(),
    val listings: List<MarketListing> = emptyList()
)

internal expect fun appCacheDir(): String

internal expect fun prefetchImages(urls: List<String>)

object HomeCatalogStore {
    private const val FILE_NAME = "home_catalog.json"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun read(): HomeCatalog? {
        val path = catalogPath() ?: return null
        val text = runCatching { FileSystem.SYSTEM.read(path) { readUtf8() } }.getOrNull() ?: return null
        return runCatching { json.decodeFromString<HomeCatalog>(text) }.getOrNull()
    }

    fun write(products: List<Product>, listings: List<MarketListing>) {
        val path = catalogPath() ?: return
        val text = json.encodeToString(HomeCatalog(products, listings))
        runCatching {
            FileSystem.SYSTEM.createDirectories(path.parent!!)
            FileSystem.SYSTEM.write(path) { writeUtf8(text) }
        }
    }

    private fun catalogPath() = appCacheDir().takeIf { it.isNotBlank() }?.let { "$it/$FILE_NAME".toPath() }
}
