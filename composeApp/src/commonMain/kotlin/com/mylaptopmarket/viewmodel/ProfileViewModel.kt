package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.network.VendorSession
import com.mylaptopmarket.repository.ApiAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ApiAuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _profile = MutableStateFlow<VendorSession?>(SessionManager.currentSession.value)
    val profile: StateFlow<VendorSession?> = _profile.asStateFlow()

    private val _accountDeleted = MutableStateFlow(false)
    val accountDeleted: StateFlow<Boolean> = _accountDeleted.asStateFlow()

    fun load() {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.getVendorStatus(vendorId)
            result.onSuccess { session ->
                _profile.value = session
                SessionManager.update { current ->
                    session.copy(daysLeft = if (session.daysLeft > 0) session.daysLeft else current.daysLeft)
                }
            }.onFailure { error ->
                _error.value = error.message
            }
            _isLoading.value = false
        }
    }

    fun save(
        shopName: String,
        mobile: String,
        location: String,
        state: String,
        email: String,
        logo: ByteArray?,
        removeLogo: Boolean,
        gstin: String = "",
        shopPhoto: ByteArray? = null
    ) {
        val vendorId = SessionManager.currentSession.value?.id
        if (vendorId == null) {
            _error.value = "User not logged in"
            return
        }
        if (shopName.isBlank()) {
            _error.value = "Shop name is required"
            return
        }
        if (mobile.isNotBlank() && (mobile.length != 10 || mobile.any { !it.isDigit() })) {
            _error.value = "Enter a valid 10-digit mobile number"
            return
        }
        if (email.isNotBlank() && !email.trim().let { value ->
                val parts = value.lowercase().split("@")
                parts.size == 2 && parts[0].isNotBlank() && parts[1].contains(".") && !value.contains(" ")
            }
        ) {
            _error.value = "Enter a valid email address"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null
            val currentDays = _profile.value?.daysLeft ?: SessionManager.currentSession.value?.daysLeft ?: 0
            val result = repository.updateProfile(
                vendorId = vendorId,
                shopName = shopName.trim(),
                mobile = mobile.trim(),
                location = location.trim(),
                state = state.trim(),
                email = email.trim(),
                logo = logo,
                removeLogo = removeLogo,
                gstin = gstin.trim(),
                shopPhoto = shopPhoto
            )
            result.onSuccess { session ->
                val updated = session.copy(daysLeft = if (session.daysLeft > 0) session.daysLeft else currentDays)
                _profile.value = updated
                SessionManager.login(updated)
                _successMessage.value = "Profile updated"
            }.onFailure { error ->
                _error.value = error.message ?: "Failed to update profile"
            }
            _isLoading.value = false
        }
    }

    fun deleteAccount(password: String) {
        val vendorId = SessionManager.currentSession.value?.id
        if (vendorId == null) {
            _error.value = "User not logged in"
            return
        }
        if (password.isBlank()) {
            _error.value = "Enter your password to confirm deletion"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null
            val result = repository.deleteAccount(vendorId, password)
            result.onSuccess {
                SessionManager.logout()
                _profile.value = null
                _accountDeleted.value = true
                _successMessage.value = it
            }.onFailure { error ->
                _error.value = error.message ?: "Failed to delete account"
            }
            _isLoading.value = false
        }
    }

    fun consumeMessages() {
        _error.value = null
        _successMessage.value = null
    }
}
