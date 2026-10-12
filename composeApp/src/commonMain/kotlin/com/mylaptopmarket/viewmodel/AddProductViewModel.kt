package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AddProductViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    fun loadForEdit(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isEditMode.value = true
            try {
                val vendorId = SessionManager.currentSession.value?.id?.toString()
                val product = if (vendorId != null) {
                    val listings = repository.getMyListings(vendorId).firstOrNull().orEmpty()
                    listings.firstOrNull { it.id == productId }
                        ?: repository.getProductById(productId).firstOrNull()
                } else {
                    repository.getProductById(productId).firstOrNull()
                }
                if (product == null) {
                    _error.value = "Product not found"
                    _editingProduct.value = null
                } else {
                    _editingProduct.value = product
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = e.message ?: "Failed to load product"
            }
            _isLoading.value = false
        }
    }

    fun addProduct(
        productName: String,
        category: String,
        description: String,
        price: Double,
        stock: Int,
        youtubeVideo: String? = null,
        status: Int = 1,
        images: List<ByteArray>,
        stockSheetName: String? = null,
        stockSheetBytes: ByteArray? = null
    ) {
        val vendorId = SessionManager.currentSession.value?.id
        if (vendorId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false

            val result = repository.addProduct(
                vendorId = vendorId,
                productName = productName,
                category = category,
                description = description,
                price = price,
                stock = stock,
                youtubeVideo = youtubeVideo,
                status = status,
                images = images,
                stockSheetName = stockSheetName,
                stockSheetBytes = stockSheetBytes
            )

            result.onSuccess {
                _success.value = true
            }.onFailure {
                _error.value = it.message ?: "Failed to add product"
            }
            _isLoading.value = false
        }
    }

    fun updateProduct(
        productId: String,
        productName: String,
        category: String,
        description: String,
        price: Double,
        stock: Int,
        youtubeVideo: String? = null,
        status: Int = 1,
        images: List<ByteArray>,
        keepImageNames: List<String>? = null,
        stockSheetName: String? = null,
        stockSheetBytes: ByteArray? = null,
        removeStockSheet: Boolean = false
    ) {
        val vendorId = SessionManager.currentSession.value?.id
        if (vendorId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false

            val result = repository.updateProduct(
                vendorId = vendorId,
                productId = productId,
                productName = productName,
                category = category,
                description = description,
                price = price,
                stock = stock,
                youtubeVideo = youtubeVideo,
                status = status,
                images = images,
                keepImageNames = keepImageNames,
                stockSheetName = stockSheetName,
                stockSheetBytes = stockSheetBytes,
                removeStockSheet = removeStockSheet
            )

            result.onSuccess {
                _success.value = true
            }.onFailure {
                _error.value = it.message ?: "Failed to update product"
            }
            _isLoading.value = false
        }
    }

    fun deleteProduct(productId: String) {
        val vendorId = SessionManager.currentSession.value?.id
        if (vendorId == null) {
            _error.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false

            repository.deleteProduct(vendorId, productId)
                .onSuccess { _success.value = true }
                .onFailure { _error.value = it.message ?: "Failed to delete product" }
            _isLoading.value = false
        }
    }
}
