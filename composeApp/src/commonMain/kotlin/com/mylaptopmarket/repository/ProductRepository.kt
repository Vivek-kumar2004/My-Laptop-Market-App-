package com.mylaptopmarket.repository

import com.mylaptopmarket.model.MarketListing
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.Provider
import com.mylaptopmarket.model.StockSheet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ProductRepository {
    fun getTopProducts(): Flow<List<Product>>
    fun getTopProviders(): Flow<List<Provider>>
    fun getMyListings(vendorId: String): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    fun getProviderById(id: String): Flow<Provider?>
    fun searchProducts(query: String): Flow<List<Product>>
    fun searchProviders(query: String): Flow<List<Provider>>
    fun getFeaturedProducts(): Flow<List<Product>>
    
    suspend fun addProduct(
        vendorId: Int,
        productName: String,
        category: String,
        description: String,
        price: Double,
        stock: Int,
        youtubeVideo: String? = null,
        status: Int = 1,
        images: List<ByteArray>,
        frontImage: ByteArray? = null,
        stockSheetName: String? = null,
        stockSheetBytes: ByteArray? = null
    ): Result<Boolean>

    suspend fun updateProduct(
        vendorId: Int,
        productId: String,
        productName: String,
        category: String,
        description: String,
        price: Double,
        stock: Int,
        youtubeVideo: String? = null,
        status: Int = 1,
        images: List<ByteArray> = emptyList(),
        keepImageNames: List<String>? = null,
        frontImage: ByteArray? = null,
        removeFrontImage: Boolean = false,
        stockSheetName: String? = null,
        stockSheetBytes: ByteArray? = null,
        removeStockSheet: Boolean = false
    ): Result<Boolean>

    suspend fun deleteProduct(vendorId: Int, productId: String): Result<Boolean>

    suspend fun getStockSheet(vendorId: Int, productId: String): Result<StockSheet>
    
    // For chatbot context
    suspend fun getAllProducts(): List<Product>

    fun getMarketListings(): Flow<List<MarketListing>> = flow { emit(emptyList()) }

    suspend fun getMarketListing(id: String): MarketListing? = null
}