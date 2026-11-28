package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.repository.ProductRepository
import com.mylaptopmarket.util.HomeCatalogStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class VendorProductsViewModel(
    private val repository: ProductRepository,
    private val vendorId: String
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _logoUrl = MutableStateFlow<String?>(null)
    val logoUrl: StateFlow<String?> = _logoUrl.asStateFlow()

    private val _resolvedVendorName = MutableStateFlow<String?>(null)
    val resolvedVendorName: StateFlow<String?> = _resolvedVendorName.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var list = repository.getMyListings(vendorId).firstOrNull().orEmpty()
                if (list.isEmpty()) {
                    list = repository.getAllProducts().filter { it.providerId == vendorId }
                }
                _products.value = list
                _logoUrl.value = list.firstOrNull()?.providerLogoUrl
                _resolvedVendorName.value = list.firstOrNull()?.providerName
            } catch (_: Exception) {
                val cached = HomeCatalogStore.productsForVendor(vendorId)
                if (cached.isNotEmpty()) {
                    _products.value = cached
                    _logoUrl.value = cached.firstOrNull()?.providerLogoUrl
                    _resolvedVendorName.value = cached.firstOrNull()?.providerName
                }
            }
            _isLoading.value = false
        }
    }
}
