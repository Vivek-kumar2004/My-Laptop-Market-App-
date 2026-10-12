package com.mylaptopmarket.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val category: String? = null,
    val price: String? = null,
    val description: String,
    val stockStatus: StockStatus,
    val stockCount: Int,
    val providerId: String,
    val providerName: String,
    val providerNumber: String,
    val media: List<Media>,
    val videoUrl: String? = null,
    val isActive: Boolean = true,
    val providerLogoUrl: String? = null,
    val hasStockSheet: Boolean = false,
    val shopTrusted: Boolean = false,
    val vendorState: String = "",
    val vendorCity: String = ""
)

@Serializable
data class StockSheet(
    val originalName: String = "",
    val viewerName: String = "",
    val viewedAt: String = "",
    val headers: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val truncated: Boolean = false
)

@Serializable
enum class StockStatus {
    AVAILABLE, OUT_OF_STOCK
}

@Serializable
data class Media(
    val url: String,
    val type: MediaType
)

@Serializable
enum class MediaType {
    IMAGE, VIDEO
}