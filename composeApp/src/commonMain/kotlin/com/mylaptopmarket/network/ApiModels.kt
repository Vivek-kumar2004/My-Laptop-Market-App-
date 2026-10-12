package com.mylaptopmarket.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ApiResponse(
    @SerialName("status") val status: Boolean = false,
    @SerialName("message") val message: String = "",
    @SerialName("data") val data: List<ProductResponse> = emptyList()
)

@Serializable
data class ProductResponse(
    @SerialName("id") val id: Int? = null,
    @SerialName("vendor_id") val vendorId: Int? = null,
    @SerialName("vendor_name") val vendorName: String? = null,
    @SerialName("vendor_number") val vendorNumber: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("images") val images: String? = null,
    @SerialName("image_urls") val imageUrls: List<String>? = null,
    @SerialName("main_image") val mainImage: String? = null,
    @SerialName("stock") val stock: Int? = null,
    @SerialName("youtube_video") val youtubeVideo: String? = null,
    @SerialName("status") val status: Int? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("vendor_logo") val vendorLogo: String? = null,
    @SerialName("vendor_logo_url") val vendorLogoUrl: String? = null,
    @SerialName("has_stock_sheet") val hasStockSheet: Boolean? = null,
    @SerialName("vendor_shop_trusted") val vendorShopTrusted: Boolean? = null,
    @SerialName("vendor_state") val vendorState: String? = null,
    @SerialName("vendor_city") val vendorCity: String? = null
)

@Serializable
data class StockSheetResponse(
    @SerialName("status") val status: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("original_name") val originalName: String? = null,
    @SerialName("viewer_name") val viewerName: String? = null,
    @SerialName("viewed_at") val viewedAt: String? = null,
    @SerialName("headers") val headers: List<String> = emptyList(),
    @SerialName("rows") val rows: List<List<String>> = emptyList(),
    @SerialName("truncated") val truncated: Boolean = false
)

@Serializable
data class WebProductsResponse(
    @SerialName("status") val status: Boolean = false,
    @SerialName("vendors") val vendors: List<WebVendorMatch> = emptyList(),
    @SerialName("data") val data: List<ProductResponse> = emptyList()
)

@Serializable
data class WebVendorMatch(
    @SerialName("vendor_id") val vendorId: Int? = null,
    @SerialName("vendor_name") val vendorName: String? = null,
    @SerialName("vendor_logo_url") val vendorLogoUrl: String? = null,
    @SerialName("product_count") val productCount: Int? = null
)
