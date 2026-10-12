package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.network.AdminPushEvents
import com.mylaptopmarket.network.AdminSessionManager
import com.mylaptopmarket.repository.AdminDashboardData
import com.mylaptopmarket.repository.AdminInquiryData
import com.mylaptopmarket.repository.AdminInvoiceData
import com.mylaptopmarket.repository.AdminProductData
import com.mylaptopmarket.repository.AdminRequirementData
import com.mylaptopmarket.repository.AdminRequirementGroupData
import com.mylaptopmarket.repository.AdminVendorData
import com.mylaptopmarket.repository.AdminVendorWriteRequest
import com.mylaptopmarket.repository.ApiAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AdminVendorFilter(val title: String, val emptyMessage: String) {
    ALL("Vendors", "No vendors yet"),
    ACTIVE("Active vendors", "No active vendors"),
    INACTIVE("Inactive vendors", "No inactive vendors")
}

class AdminAuthViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _loginSuccess.value = false
            repository.login(username.trim(), password)
                .onSuccess { session ->
                    AdminSessionManager.login(session)
                    _loginSuccess.value = true
                }
                .onFailure { _errorMessage.value = it.message ?: "Login failed" }
            _isLoading.value = false
        }
    }

    fun consumeLoginSuccess() {
        _loginSuccess.value = false
    }

    fun logout() {
        viewModelScope.launch {
            val token = AdminPushEvents.token.value?.token
            if (!token.isNullOrBlank() && AdminSessionManager.currentSession.value != null) {
                runCatching { repository.unregisterPushToken(token) }
            }
            _loginSuccess.value = false
            _errorMessage.value = null
            AdminSessionManager.logout()
        }
    }
}

class AdminDashboardViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _data = MutableStateFlow<AdminDashboardData?>(null)
    val data: StateFlow<AdminDashboardData?> = _data.asStateFlow()

    private val _announcement = MutableStateFlow("")
    val announcement: StateFlow<String> = _announcement.asStateFlow()

    private val _announceStatus = MutableStateFlow<String?>(null)
    val announceStatus: StateFlow<String?> = _announceStatus.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.dashboard()
                .onSuccess { _data.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadAnnouncement() {
        viewModelScope.launch {
            repository.currentAnnouncement()
                .onSuccess { _announcement.value = it }
        }
    }

    fun sendAnnouncement(message: String) {
        viewModelScope.launch {
            _announceStatus.value = null
            repository.saveAnnouncement(message.trim())
                .onSuccess {
                    _announcement.value = message.trim()
                    _announceStatus.value = it
                }
                .onFailure { _announceStatus.value = it.message }
        }
    }

    fun clearAnnounceStatus() {
        _announceStatus.value = null
    }
}

class AdminVendorsViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _filter = MutableStateFlow(AdminVendorFilter.ALL)
    val filter: StateFlow<AdminVendorFilter> = _filter.asStateFlow()

    private val _vendors = MutableStateFlow<List<AdminVendorData>>(emptyList())
    val vendors: StateFlow<List<AdminVendorData>> = _vendors.asStateFlow()

    fun setFilter(filter: AdminVendorFilter) {
        _filter.value = filter
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.vendors()
                .onSuccess { _vendors.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun activate(id: Int) = runAction { repository.activateVendor(id) }
    fun deactivate(id: Int) = runAction { repository.deactivateVendor(id) }
    fun delete(id: Int) = runAction { repository.deleteVendor(id) }
    fun warn(id: Int, message: String) = runAction { repository.warnVendor(id, message) }

    fun clearStatus() {
        _statusMessage.value = null
    }

    private fun runAction(block: suspend () -> Result<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            block()
                .onSuccess {
                    _statusMessage.value = it
                    repository.vendors()
                        .onSuccess { list -> _vendors.value = list }
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }
}

class AdminVendorFormViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _vendor = MutableStateFlow<AdminVendorData?>(null)
    val vendor: StateFlow<AdminVendorData?> = _vendor.asStateFlow()

    fun load(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.vendor(id)
                .onSuccess { _vendor.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun save(
        id: Int?,
        shopName: String,
        username: String,
        mobile: String,
        password: String,
        location: String,
        gstin: String,
        state: String,
        stateCode: String,
        billingAddress: String,
        contactPerson: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _saved.value = false
            val request = AdminVendorWriteRequest(
                shop_name = shopName.trim(),
                username = username.trim(),
                mobile = mobile.trim(),
                password = password.takeIf { it.isNotBlank() },
                location = location.trim().ifBlank { null },
                gstin = gstin.trim().ifBlank { null },
                state = state.trim().ifBlank { null },
                state_code = stateCode.trim().ifBlank { null },
                billing_address = billingAddress.trim().ifBlank { null },
                contact_person = contactPerson.trim().ifBlank { null }
            )
            repository.saveVendor(id, request)
                .onSuccess { _saved.value = true }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }
}

class AdminInquiriesViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _inquiries = MutableStateFlow<List<AdminInquiryData>>(emptyList())
    val inquiries: StateFlow<List<AdminInquiryData>> = _inquiries.asStateFlow()

    private val _newCount = MutableStateFlow(0)
    val newCount: StateFlow<Int> = _newCount.asStateFlow()

    private val _detail = MutableStateFlow<AdminInquiryData?>(null)
    val detail: StateFlow<AdminInquiryData?> = _detail.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.inquiries()
                .onSuccess { (list, count) ->
                    _inquiries.value = list
                    _newCount.value = count
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun load(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.inquiry(id)
                .onSuccess { _detail.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.deleteInquiry(id)
                .onSuccess {
                    _deleted.value = true
                    refresh()
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }
}

class AdminInvoicesViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _invoices = MutableStateFlow<List<AdminInvoiceData>>(emptyList())
    val invoices: StateFlow<List<AdminInvoiceData>> = _invoices.asStateFlow()

    private val _detail = MutableStateFlow<AdminInvoiceData?>(null)
    val detail: StateFlow<AdminInvoiceData?> = _detail.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.invoices()
                .onSuccess { _invoices.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun load(id: Int) {
        val cached = _invoices.value.firstOrNull { it.id == id }
        if (cached != null) _detail.value = cached
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.invoice(id)
                .onSuccess { _detail.value = it }
                .onFailure { error ->
                    if (_detail.value == null) _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    fun htmlUrl(id: Int): String = repository.invoiceHtmlUrl(id)
}

class AdminProductsViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _products = MutableStateFlow<List<AdminProductData>>(emptyList())
    val products: StateFlow<List<AdminProductData>> = _products.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.products()
                .onSuccess { _products.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.deleteProduct(id)
                .onSuccess { message ->
                    _products.value = _products.value.filter { it.id != id }
                    _statusMessage.value = message
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}

enum class AdminRequirementFilter(val apiValue: String, val title: String) {
    ALL("all", "All"),
    ACTIVE("active", "Active"),
    CLOSED("closed", "Closed")
}

class AdminRequirementsViewModel(private val repository: ApiAdminRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _filter = MutableStateFlow(AdminRequirementFilter.ALL)
    val filter: StateFlow<AdminRequirementFilter> = _filter.asStateFlow()

    private val _requirements = MutableStateFlow<List<AdminRequirementGroupData>>(emptyList())
    val requirements: StateFlow<List<AdminRequirementGroupData>> = _requirements.asStateFlow()

    private val _detail = MutableStateFlow<AdminRequirementData?>(null)
    val detail: StateFlow<AdminRequirementData?> = _detail.asStateFlow()

    fun setFilter(filter: AdminRequirementFilter) {
        if (_filter.value == filter) return
        _filter.value = filter
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.requirements(_filter.value.apiValue)
                .onSuccess { _requirements.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun load(id: Int) {
        val group = _requirements.value.firstOrNull { g -> g.vendors.any { it.requirementId == id } }
        val cached = group?.vendors?.firstOrNull { it.requirementId == id }
        if (cached != null && _detail.value == null) {
            _detail.value = AdminRequirementData(
                id = cached.requirementId ?: id,
                vendorId = cached.id,
                vendorName = cached.shopName,
                vendorUsername = cached.username,
                vendorMobile = cached.mobile,
                vendorLogoUrl = cached.logoUrl,
                title = cached.title.orEmpty(),
                canonicalTitle = cached.canonicalTitle,
                category = group.category,
                quantity = cached.quantity,
                description = cached.description,
                status = cached.status,
                createdAt = cached.createdAt,
                updatedAt = cached.updatedAt
            )
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.requirement(id)
                .onSuccess { _detail.value = it }
                .onFailure { error ->
                    if (_detail.value == null) _errorMessage.value = error.message
                }
            _isLoading.value = false
        }
    }

    fun close(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.closeRequirement(id)
                .onSuccess {
                    repository.requirement(id)
                        .onSuccess { _detail.value = it }
                    repository.requirements(_filter.value.apiValue)
                        .onSuccess { _requirements.value = it }
                }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }
}
