package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ProductRepository
import com.mylaptopmarket.util.OfflineSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyListingsViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMyListings()
        viewModelScope.launch {
            OfflineSync.revision.collect { revision ->
                if (revision > 0) loadMyListings()
            }
        }
    }

    fun loadMyListings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val session = SessionManager.currentSession.value
                if (session != null) {
                    repository.getMyListings(session.id.toString()).collect { products ->
                        _myProducts.value = products
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}