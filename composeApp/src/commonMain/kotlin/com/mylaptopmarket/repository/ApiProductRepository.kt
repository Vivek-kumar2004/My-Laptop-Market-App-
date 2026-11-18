package com.mylaptopmarket.repository

import com.mylaptopmarket.model.*
import com.mylaptopmarket.network.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.timeout
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@kotlinx.serialization.Serializable
private data class CreatedProduct(val id: Int? = null)

@kotlinx.serialization.Serializable
private data class AddProductResponse(
    val status: Boolean,
    val message: String? = null,
    val data: CreatedProduct? = null
)

@kotlinx.serialization.Serializable
private data class MarketListResponse(
    val status: Boolean = false,
    val data: List<com.mylaptopmarket.model.MarketListing> = emptyList()
)

@kotlinx.serialization.Serializable
private data class MarketOneResponse(
    val status: Boolean = false,
    val data: com.mylaptopmarket.model.MarketListing? = null
)

class ApiProductRepository : ProductRepository {

    private val baseImageUrl = "${KtorClient.getMediaBaseUrl()}assets/uploads/products/"
    private val baseVendorLogoUrl = "${KtorClient.getMediaBaseUrl()}assets/uploads/vendors/"
    private val json = Json { ignoreUnknownKeys = true }
    private val vendorLogoCache = mutableMapOf<String, String?>()

    override suspend fun getAllProducts(): List<Product> {
        return try {
            val url = "${KtorClient.getBaseUrl()}api/view_products"
            println("Fetching products from: $url")
            val httpResponse = KtorClient.httpClient.get(url)
            val text = httpResponse.bodyAsText()
            if (httpResponse.status.value !in 200..299) {
                throw IllegalStateException(
                    "Products load nahi ho sake (HTTP ${httpResponse.status.value}). Internet check karke Retry dabayein."
                )
            }
            val response = AppJson.decodeFromString(ApiResponse.serializer(), text)
            println("API Response - Status: ${response.status}, Data count: ${response.data.size}")
            if (!response.status) {
                throw IllegalStateException(response.message.ifBlank { "No products found." })
            }
            enrichMissingVendorLogos(response.data.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("ERROR fetching products: ${e.message}")
            e.printStackTrace()
            throw IllegalStateException(friendlyNetworkError(e), e)
        }
    }

    override fun getTopProducts(): Flow<List<Product>> = flow {
        emit(getAllProducts())
    }

    override fun getMarketListings(): Flow<List<com.mylaptopmarket.model.MarketListing>> = flow {
        emit(fetchMarketListings())
    }

    override suspend fun getMarketListing(id: String): com.mylaptopmarket.model.MarketListing? {
        return try {
            val response = KtorClient.httpClient.get("${KtorClient.getBaseUrl()}api/market-listings/$id")
            val payload = AppJson.decodeFromString(MarketOneResponse.serializer(), response.bodyAsText())
            if (!payload.status) null else payload.data
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchMarketListings(): List<com.mylaptopmarket.model.MarketListing> {
        return try {
            val response = KtorClient.httpClient.get("${KtorClient.getBaseUrl()}api/market-listings")
            val payload = AppJson.decodeFromString(MarketListResponse.serializer(), response.bodyAsText())
            if (!payload.status) emptyList() else payload.data
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun getFeaturedProducts(): Flow<List<Product>> = flow {
        val all = getAllProducts()
        emit(all.take(5))
    }

    override fun getTopProviders(): Flow<List<Provider>> = flow {
        val all = getAllProducts()
        val providers = all.groupBy { it.providerId }.map { (id, products) -> 
            val first = products.first()
            Provider(
                id = id,
                name = first.providerName,
                contactPhone = first.providerNumber,
                productCount = products.size,
                logoUrl = first.providerLogoUrl
            )
        }
        emit(providers)
    }

    override fun getProductById(id: String): Flow<Product?> = flow {
        val all = getAllProducts()
        emit(all.find { it.id == id })
    }

    override fun getProviderById(id: String): Flow<Provider?> = flow {
        val all = getAllProducts()
        val products = all.filter { it.providerId == id }
        val product = products.firstOrNull()
        if (product != null) {
            val logo = product.providerLogoUrl
                ?: resolveVendorLogo(product.providerId, product.providerName, null)
            emit(
                Provider(
                    id = product.providerId,
                    name = product.providerName,
                    contactPhone = product.providerNumber,
                    productCount = products.size,
                    logoUrl = logo
                )
            )
        } else {
            emit(null)
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> = flow {
        try {
            val response: ApiResponse = KtorClient.httpClient.get("${KtorClient.getBaseUrl()}api/searchProducts") {
                parameter("search", query)
            }.body()
            if (response.status) {
                emit(enrichMissingVendorLogos(response.data.map { it.toDomain() }))
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            val all = getAllProducts()
            emit(all.filter { it.name.contains(query, ignoreCase = true) })
        }
    }

    override fun getMyListings(vendorId: String): Flow<List<Product>> = flow {
        try {
            val response: ApiResponse = KtorClient.httpClient.get("${KtorClient.getBaseUrl()}api/vendor_products/$vendorId").body()
            if (response.status) {
                emit(enrichMissingVendorLogos(response.data.map { it.toDomain() }))
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun searchProviders(query: String): Flow<List<Provider>> = flow {
        val all = getAllProducts()
        val providers = all.filter { it.providerName.contains(query, ignoreCase = true) }
            .groupBy { it.providerId }.map { (id, products) ->
                val first = products.first()
                Provider(
                    id = id,
                    name = first.providerName,
                    contactPhone = first.providerNumber,
                    productCount = products.size,
                    logoUrl = first.providerLogoUrl
                )
            }
        emit(providers)
    }

    override suspend fun addProduct(
        vendorId: Int,
        productName: String,
        category: String,
        description: String,
        price: Double,
        stock: Int,
        youtubeVideo: String?,
        status: Int,
        images: List<ByteArray>,
        stockSheetName: String?,
        stockSheetBytes: ByteArray?
    ): Result<Boolean> {
        return try {
            val response: AddProductResponse = KtorClient.httpClient.post("${KtorClient.getBaseUrl()}api/products/add") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("vendor_id", vendorId.toString())
                            append("product_name", productName)
                            append("category", category)
                            append("description", description)
                            append("price", price.toString())
                            append("stock", stock.toString())
                            if (!youtubeVideo.isNullOrBlank()) {
                                append("youtube_video", youtubeVideo)
                            }
                            append("status", status.toString())
                            images.forEachIndexed { index, bytes ->
                                append(
                                    "images[]",
                                    bytes,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"image_$index.jpg\"")
                                    }
                                )
                            }
                        }
                    )
                )
            }.body()
            if (!response.status) {
                return Result.failure(Exception(response.message ?: "Failed"))
            }
            val productId = response.data?.id
            if (stockSheetBytes != null && stockSheetBytes.isNotEmpty()) {
                if (productId == null) {
                    return Result.failure(Exception("Product saved, but the Excel sheet could not be attached"))
                }
                return uploadStockSheet(vendorId, productId.toString(), stockSheetName ?: "stock.xlsx", stockSheetBytes)
            }
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(
        vendorId: Int,
        productId: String,
        productName: String,
        category: String,
        description: String,
        price: Double,
        stock: Int,
        youtubeVideo: String?,
        status: Int,
        images: List<ByteArray>,
        keepImageNames: List<String>?,
        stockSheetName: String?,
        stockSheetBytes: ByteArray?,
        removeStockSheet: Boolean
    ): Result<Boolean> {
        return try {
            val response: AddProductResponse = KtorClient.httpClient.post(
                "${KtorClient.getBaseUrl()}api/products/update/$productId"
            ) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("vendor_id", vendorId.toString())
                            append("product_name", productName)
                            append("category", category)
                            append("description", description)
                            append("price", price.toString())
                            append("stock", stock.toString())
                            if (!youtubeVideo.isNullOrBlank()) {
                                append("youtube_video", youtubeVideo)
                            }
                            append("status", status.toString())
                            if (keepImageNames != null) {
                                append("keep_images", json.encodeToString(keepImageNames))
                            }
                            images.forEachIndexed { index, bytes ->
                                append(
                                    "images[]",
                                    bytes,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(
                                            HttpHeaders.ContentDisposition,
                                            "filename=\"image_$index.jpg\""
                                        )
                                    }
                                )
                            }
                        }
                    )
                )
            }.body()
            if (!response.status) {
                return Result.failure(Exception(response.message ?: "Failed to update product"))
            }
            if (stockSheetBytes != null && stockSheetBytes.isNotEmpty()) {
                return uploadStockSheet(vendorId, productId, stockSheetName ?: "stock.xlsx", stockSheetBytes)
            }
            if (removeStockSheet) {
                return deleteStockSheet(vendorId, productId)
            }
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(vendorId: Int, productId: String): Result<Boolean> {
        return try {
            val response: AddProductResponse = KtorClient.httpClient.submitForm(
                url = "${KtorClient.getBaseUrl()}api/products/delete/$productId",
                formParameters = Parameters.build {
                    append("vendor_id", vendorId.toString())
                }
            ).body()
            if (response.status) Result.success(true)
            else Result.failure(Exception(response.message ?: "Failed to delete product"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getStockSheet(vendorId: Int, productId: String): Result<StockSheet> {
        return try {
            val response: StockSheetResponse = KtorClient.httpClient.get(
                "${KtorClient.getBaseUrl()}api/products/$productId/stock-sheet"
            ) {
                parameter("vendor_id", vendorId)
            }.body()
            if (!response.status) {
                Result.failure(Exception(response.message ?: "Could not open stock sheet"))
            } else {
                Result.success(
                    StockSheet(
                        originalName = response.originalName.orEmpty(),
                        viewerName = response.viewerName.orEmpty(),
                        viewedAt = response.viewedAt.orEmpty(),
                        headers = response.headers,
                        rows = response.rows,
                        truncated = response.truncated
                    )
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    private suspend fun uploadStockSheet(
        vendorId: Int,
        productId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<Boolean> {
        if (bytes.size > 5 * 1024 * 1024) {
            return Result.failure(Exception("Excel file must be 5 MB or smaller"))
        }
        return try {
            val safeName = fileName.substringAfterLast('/').substringAfterLast('\\').ifBlank { "stock.xlsx" }
            val response: AddProductResponse = KtorClient.httpClient.post(
                "${KtorClient.getBaseUrl()}api/products/$productId/stock-sheet"
            ) {
                timeout {
                    requestTimeoutMillis = 120_000
                    socketTimeoutMillis = 120_000
                }
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("vendor_id", vendorId.toString())
                            append(
                                "file",
                                bytes,
                                Headers.build {
                                    append(
                                        HttpHeaders.ContentType,
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    )
                                    append(HttpHeaders.ContentDisposition, "filename=\"$safeName\"")
                                }
                            )
                        }
                    )
                )
            }.body()
            if (response.status) Result.success(true)
            else Result.failure(Exception(response.message ?: "Could not save the Excel sheet"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception(friendlyNetworkError(e)))
        }
    }

    private suspend fun deleteStockSheet(vendorId: Int, productId: String): Result<Boolean> {
        return try {
            val response: AddProductResponse = KtorClient.httpClient.submitForm(
                url = "${KtorClient.getBaseUrl()}api/products/$productId/stock-sheet/delete",
                formParameters = Parameters.build {
                    append("vendor_id", vendorId.toString())
                }
            ).body()
            if (response.status) Result.success(true)
            else Result.failure(Exception(response.message ?: "Could not remove the Excel sheet"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    private fun ProductResponse.toDomain(): Product {
        val imageList: List<String> = when {
            !imageUrls.isNullOrEmpty() -> imageUrls.filter { it.isNotBlank() }
            !mainImage.isNullOrBlank() -> listOf(mainImage)
            !images.isNullOrBlank() -> try {
                json.decodeFromString<List<String>>(images)
            } catch (_: Exception) {
                emptyList()
            }
            else -> emptyList()
        }

        val mediaList = imageList.mapNotNull { img ->
            val url = when {
                img.startsWith("http://") || img.startsWith("https://") -> img
                img.isBlank() -> return@mapNotNull null
                else -> "${baseImageUrl}${img.trimStart('/')}"
            }
            Media(url, MediaType.IMAGE)
        }

        return Product(
            id = id?.toString() ?: "",
            name = productName ?: "Unknown Product",
            category = category,
            price = price,
            description = description ?: "",
            stockStatus = if ((stock ?: 0) > 0)
                StockStatus.AVAILABLE else StockStatus.OUT_OF_STOCK,
            stockCount = stock ?: 0,
            providerId = vendorId?.toString() ?: "1",
            providerName = vendorName ?: "Unknown Vendor",
            providerNumber = vendorNumber ?: "Number Not Available",
            media = mediaList,
            videoUrl = if (youtubeVideo.isNullOrBlank()) null else youtubeVideo,
            isActive = (status ?: 1) == 1,
            providerLogoUrl = resolveLogoFromResponse(vendorLogoUrl, vendorLogo),
            hasStockSheet = hasStockSheet == true,
            shopTrusted = vendorShopTrusted == true,
            vendorState = vendorState.orEmpty(),
            vendorCity = vendorCity.orEmpty()
        )
    }

    private fun resolveLogoFromResponse(logoUrl: String?, logoFile: String?): String? {
        if (!logoUrl.isNullOrBlank()) return logoUrl
        val file = logoFile?.substringAfterLast('/')?.trim().orEmpty()
        if (file.isBlank()) return null
        return "$baseVendorLogoUrl$file"
    }

    private suspend fun enrichMissingVendorLogos(products: List<Product>): List<Product> {
        // Don't resolve/expose vendor logos unless a vendor is logged in
        if (SessionManager.currentSession.value == null) {
            return products.map { it.copy(providerLogoUrl = null) }
        }

        val missing = products
            .filter { it.providerLogoUrl.isNullOrBlank() }
            .map { it.providerId to it.providerName }
            .distinctBy { it.first }

        for ((vendorId, vendorName) in missing) {
            resolveVendorLogo(vendorId, vendorName, null)
        }

        return products.map { product ->
            val cached = vendorLogoCache[product.providerId]
            if (!product.providerLogoUrl.isNullOrBlank()) {
                vendorLogoCache[product.providerId] = product.providerLogoUrl
                product
            } else if (!cached.isNullOrBlank()) {
                product.copy(providerLogoUrl = cached)
            } else {
                product
            }
        }
    }

    private suspend fun resolveVendorLogo(
        vendorId: String,
        vendorName: String?,
        existing: String?
    ): String? {
        if (!existing.isNullOrBlank()) {
            vendorLogoCache[vendorId] = existing
            return existing
        }
        if (vendorLogoCache.containsKey(vendorId)) {
            return vendorLogoCache[vendorId]
        }
        val name = vendorName?.takeIf { it.isNotBlank() }
        if (name == null) {
            vendorLogoCache[vendorId] = null
            return null
        }
        val fetched = fetchVendorLogoFromWebApi(vendorId, name)
        vendorLogoCache[vendorId] = fetched
        return fetched
    }

    private suspend fun fetchVendorLogoFromWebApi(vendorId: String, vendorName: String): String? {
        return try {
            val response: WebProductsResponse = KtorClient.httpClient.get(
                "${KtorClient.getBaseUrl()}api/web/products"
            ) {
                parameter("search", vendorName)
            }.body()
            response.vendors.firstOrNull { it.vendorId?.toString() == vendorId }?.vendorLogoUrl
                ?: response.vendors.firstOrNull {
                    it.vendorName.equals(vendorName, ignoreCase = true)
                }?.vendorLogoUrl
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            null
        }
    }
}