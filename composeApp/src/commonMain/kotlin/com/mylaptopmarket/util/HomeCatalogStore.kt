package com.mylaptopmarket.util

import com.mylaptopmarket.model.MarketListing
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.StockSheet
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

@Serializable
data class HomeCatalog(
    val products: List<Product> = emptyList(),
    val listings: List<MarketListing> = emptyList(),
    val stockSheets: Map<String, StockSheet> = emptyMap()
)

internal expect fun appCacheDir(): String

internal expect fun prefetchImages(urls: List<String>)

object HomeCatalogStore {
    private const val FILE_NAME = "home_catalog.json"
    internal var directoryOverride: String? = null
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
        withOfflineLock {
            val sheets = read()?.stockSheets.orEmpty()
            writeCatalog(HomeCatalog(products, listings, sheets))
        }
    }

    fun saveStockSheet(productId: String, sheet: StockSheet) {
        withOfflineLock {
            val current = read() ?: HomeCatalog()
            writeCatalog(current.copy(stockSheets = current.stockSheets + (productId to sheet)))
        }
    }

    fun stockSheet(productId: String): StockSheet? = read()?.stockSheets?.get(productId)

    fun product(id: String): Product? = read()?.products?.find { it.id == id }

    fun productsForVendor(vendorId: String): List<Product> =
        read()?.products?.filter { it.providerId == vendorId }.orEmpty()

    private fun writeCatalog(catalog: HomeCatalog) {
        val path = catalogPath() ?: return
        val text = json.encodeToString(catalog)
        runCatching {
            FileSystem.SYSTEM.createDirectories(path.parent!!)
            FileSystem.SYSTEM.write(path) { writeUtf8(text) }
        }
    }

    private fun catalogPath() =
        (directoryOverride ?: appCacheDir()).takeIf { it.isNotBlank() }?.let { "$it/$FILE_NAME".toPath() }
}
