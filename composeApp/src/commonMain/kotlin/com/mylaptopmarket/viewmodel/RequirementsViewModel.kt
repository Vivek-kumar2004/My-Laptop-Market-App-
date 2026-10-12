package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.model.MyRequirement
import com.mylaptopmarket.model.RequirementGroup
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ApiRequirementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequirementsViewModel(
    private val repository: ApiRequirementRepository
) : ViewModel() {
    private val _feed = MutableStateFlow<List<RequirementGroup>>(emptyList())
    val feed: StateFlow<List<RequirementGroup>> = _feed.asStateFlow()

    private val _mine = MutableStateFlow<List<MyRequirement>>(emptyList())
    val mine: StateFlow<List<MyRequirement>> = _mine.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val vendorId = SessionManager.currentSession.value?.id
                _feed.value = repository.getFeed(vendorId)
                if (vendorId != null) {
                    _mine.value = repository.getMine(vendorId)
                } else {
                    _mine.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load requirements"
            }
            _isLoading.value = false
        }
    }

    fun closeMine(requirementId: Int) {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        viewModelScope.launch {
            repository.close(vendorId, requirementId)
                .onSuccess { refresh() }
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteMine(requirementId: Int) {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        viewModelScope.launch {
            repository.delete(vendorId, requirementId)
                .onSuccess { refresh() }
                .onFailure { _error.value = it.message }
        }
    }
}

class AddRequirementViewModel(
    private val repository: ApiRequirementRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun post(title: String, category: String, quantity: Int?, description: String?) {
        val vendorId = SessionManager.currentSession.value?.id
        if (vendorId == null) {
            _error.value = "Please log in first"
            return
        }
        if (title.isBlank()) {
            _error.value = "Title is required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false
            repository.create(vendorId, title.trim(), category, quantity, description)
                .onSuccess { _success.value = true }
                .onFailure { _error.value = it.message ?: "Failed to post" }
            _isLoading.value = false
        }
    }
}
