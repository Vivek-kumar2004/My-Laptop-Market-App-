package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.Provider
import com.mylaptopmarket.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    private val _provider = MutableStateFlow<Provider?>(null)
    val provider: StateFlow<Provider?> = _provider.asStateFlow()

    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            repository.getProductById(productId).collect { prod ->
                _product.value = prod
                prod?.let {
                    repository.getProviderById(it.providerId).collect { prov ->
                        _provider.value = prov
                    }
                }
            }
        }
    }
}
