package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.Provider
import com.mylaptopmarket.repository.ProductRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultsProducts = MutableStateFlow<List<Product>>(emptyList())
    val searchResultsProducts: StateFlow<List<Product>> = _searchResultsProducts.asStateFlow()

    private val _searchResultsProviders = MutableStateFlow<List<Provider>>(emptyList())
    val searchResultsProviders: StateFlow<List<Provider>> = _searchResultsProviders.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.length >= 2) {
                        repository.searchProducts(query).collect {
                            _searchResultsProducts.value = it
                        }
                        repository.searchProviders(query).collect {
                            _searchResultsProviders.value = it
                        }
                    } else {
                        _searchResultsProducts.value = emptyList()
                        _searchResultsProviders.value = emptyList()
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
