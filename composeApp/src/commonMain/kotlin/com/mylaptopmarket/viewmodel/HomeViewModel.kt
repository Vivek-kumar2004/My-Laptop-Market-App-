package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.MarketListing
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.Provider
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ProductRepository
import com.mylaptopmarket.util.HomeCatalog
import com.mylaptopmarket.util.HomeCatalogStore
import com.mylaptopmarket.util.OfflineSync
import com.mylaptopmarket.util.prefetchImages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _featuredProducts = MutableStateFlow<List<Product>>(emptyList())
    val featuredProducts: StateFlow<List<Product>> = _featuredProducts.asStateFlow()

    private val _topProducts = MutableStateFlow<List<Product>>(emptyList())
    val topProducts: StateFlow<List<Product>> = _topProducts.asStateFlow()

    private val _topProviders = MutableStateFlow<List<Provider>>(emptyList())
    val topProviders: StateFlow<List<Provider>> = _topProviders.asStateFlow()

    private val _marketListings = MutableStateFlow<List<MarketListing>>(emptyList())
    val marketListings: StateFlow<List<MarketListing>> = _marketListings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var refreshJob: Job? = null
    private var refreshGeneration = 0

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showingSaved = MutableStateFlow(false)
    val showingSaved: StateFlow<Boolean> = _showingSaved.asStateFlow()

    init {
        applySaved(HomeCatalogStore.read())
        viewModelScope.launch {
            OfflineSync.revision.collect { revision ->
                if (revision == 0) return@collect
                applySaved(HomeCatalogStore.read())
                _showingSaved.value = false
                _errorMessage.value = null
            }
        }
    }

    private fun applySaved(saved: HomeCatalog?) {
        if (saved == null) return
        if (saved.products.isEmpty() && saved.listings.isEmpty()) return
        _topProducts.value = saved.products
        _featuredProducts.value = saved.products.take(5)
        _topProviders.value = providersFrom(saved.products)
        _marketListings.value = saved.listings
        _isLoading.value = false
    }

    fun refreshData(fromPull: Boolean = false) {
        if (fromPull) _isRefreshing.value = true
        val generation = ++refreshGeneration
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val showingSaved = _topProducts.value.isNotEmpty() || _marketListings.value.isNotEmpty()
            if (!showingSaved && !fromPull) {
                _isLoading.value = true
            }
            _errorMessage.value = null
            try {
                val products = repository.getAllProducts()
                val fetchedListings = runCatching { repository.getMarketListings().first() }.getOrDefault(emptyList())
                val listings = fetchedListings.ifEmpty { _marketListings.value }
                _topProducts.value = products
                _featuredProducts.value = products.take(5)
                _topProviders.value = providersFrom(products)
                _marketListings.value = listings
                if (products.isEmpty() && !showingSaved) {
                    _errorMessage.value = "Abhi koi live product listing nahi hai."
                }
                _showingSaved.value = false
                finishRefresh(generation)
                withContext(Dispatchers.Default) {
                    HomeCatalogStore.write(products, listings)
                }
                prefetchImages(imageUrls(products, listings))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (showingSaved) {
                    _showingSaved.value = true
                } else {
                    _errorMessage.value = com.mylaptopmarket.network.friendlyNetworkError(e)
                }
            } finally {
                finishRefresh(generation)
            }
        }
    }

    private fun finishRefresh(generation: Int) {
        if (generation != refreshGeneration) return
        _isLoading.value = false
        _isRefreshing.value = false
    }

    private fun providersFrom(products: List<Product>): List<Provider> {
        return products.groupBy { it.providerId }.map { (id, rows) ->
            val first = rows.first()
            Provider(
                id = id,
                name = first.providerName,
                contactPhone = first.providerNumber,
                productCount = rows.size,
                logoUrl = first.providerLogoUrl
            )
        }
    }

    private fun imageUrls(products: List<Product>, listings: List<MarketListing>): List<String> {
        val productPhotos = products.mapNotNull { it.media.firstOrNull()?.url }
        val listingPhotos = listings.mapNotNull { it.images.firstOrNull() }
        return productPhotos + listingPhotos
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
