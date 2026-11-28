package com.mylaptopmarket.util

import com.mylaptopmarket.model.Media
import com.mylaptopmarket.model.MediaType
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.StockSheet
import com.mylaptopmarket.model.StockStatus
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeCatalogStoreTest {
    private val dir = "/tmp/mlm-offline-${currentTimeMillis()}"

    @BeforeTest
    fun setup() {
        HomeCatalogStore.directoryOverride = dir
        FileSystem.SYSTEM.createDirectories(dir.toPath())
    }

    @AfterTest
    fun tearDown() {
        HomeCatalogStore.directoryOverride = null
        runCatching { FileSystem.SYSTEM.deleteRecursively(dir.toPath()) }
    }

    @Test
    fun keepsASavedStockSheetWhenTheCatalogRefreshes() {
        val product = sample("9")
        HomeCatalogStore.write(listOf(product), emptyList())
        HomeCatalogStore.saveStockSheet(
            "9",
            StockSheet(headers = listOf("Model"), rows = listOf(listOf("T480")))
        )

        HomeCatalogStore.write(listOf(product.copy(price = "20000")), emptyList())

        assertEquals("20000", HomeCatalogStore.read()?.products?.single()?.price)
        assertEquals("T480", HomeCatalogStore.stockSheet("9")?.rows?.single()?.single())
    }

    @Test
    fun findsOneDealersSavedStock() {
        HomeCatalogStore.write(
            listOf(sample("1", providerId = "4"), sample("2", providerId = "8")),
            emptyList()
        )

        assertEquals(listOf("1"), HomeCatalogStore.productsForVendor("4").map { it.id })
        assertEquals("1", HomeCatalogStore.product("1")?.id)
        assertNull(HomeCatalogStore.product("missing"))
    }

    private fun sample(id: String, providerId: String = "4") = Product(
        id = id,
        name = "ThinkPad $id",
        description = "saved",
        price = "10000",
        stockStatus = StockStatus.AVAILABLE,
        stockCount = 2,
        providerId = providerId,
        providerName = "Shop $providerId",
        providerNumber = "9999999999",
        media = listOf(Media(url = "https://example.com/$id.jpg", type = MediaType.IMAGE))
    )
}
