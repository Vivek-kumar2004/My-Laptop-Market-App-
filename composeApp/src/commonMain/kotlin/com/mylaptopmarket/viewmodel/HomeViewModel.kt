package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.Provider
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _featuredProducts = MutableStateFlow<List<Product>>(emptyList())
    val featuredProducts: StateFlow<List<Product>> = _featuredProducts.asStateFlow()

    private val _topProducts = MutableStateFlow<List<Product>>(emptyList())
    val topProducts: StateFlow<List<Product>> = _topProducts.asStateFlow()

    private val _topProviders = MutableStateFlow<List<Provider>>(emptyList())
    val topProviders: StateFlow<List<Provider>> = _topProviders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                launch {
                    repository.getFeaturedProducts().collect {
                        _featuredProducts.value = it
                    }
                }
                launch {
                    try {
                        repository.getTopProducts().collect {
                            _topProducts.value = it
                            _isLoading.value = false
                            if (it.isEmpty()) {
                                _errorMessage.value = "Abhi koi live product listing nahi hai."
                            }
                        }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        _isLoading.value = false
                        _errorMessage.value = com.mylaptopmarket.network.friendlyNetworkError(e)
                        println("HomeViewModel error: ${e.message}")
                        e.printStackTrace()
                    }
                }
                launch {
                    repository.getTopProviders().collect {
                        _topProviders.value = it
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Failed to load products: ${e.message}"
                println("HomeViewModel error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun ensureSessionLogo(vendorId: Int, shopName: String) {
        viewModelScope.launch {
            val current = SessionManager.currentSession.value ?: return@launch
            if (!current.logoUrl.isNullOrBlank()) return@launch

            var logo = repository.getMyListings(vendorId.toString()).firstOrNull()
                ?.firstOrNull()
                ?.providerLogoUrl

            if (logo.isNullOrBlank()) {
                logo = repository.getAllProducts()
                    .firstOrNull { it.providerId == vendorId.toString() }
                    ?.providerLogoUrl
            }

            if (logo.isNullOrBlank()) {
                logo = repository.getProviderById(vendorId.toString()).firstOrNull()?.logoUrl
            }

            if (!logo.isNullOrBlank()) {
                SessionManager.update { it.copy(logoUrl = logo) }
            }
        }
    }
}
