package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.network.VendorSession
import com.mylaptopmarket.repository.ApiAuthRepository
import com.mylaptopmarket.repository.AuthPending
import com.mylaptopmarket.repository.AuthPendingException
import com.mylaptopmarket.repository.LoginRequest
import com.mylaptopmarket.repository.RegisterRequest
import com.mylaptopmarket.service.PhoneAuthBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object SignInNotice {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun show(text: String) {
        _message.value = text
    }

    fun clear() {
        _message.value = null
    }
}

class AuthViewModel(private val repository: ApiAuthRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _pendingVerification = MutableStateFlow<AuthPending?>(null)
    val pendingVerification: StateFlow<AuthPending?> = _pendingVerification.asStateFlow()

    private val _sellerStatus = MutableStateFlow<VendorSession?>(null)
    val sellerStatus: StateFlow<VendorSession?> = _sellerStatus.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _passwordResetDone = MutableStateFlow(false)
    val passwordResetDone: StateFlow<Boolean> = _passwordResetDone.asStateFlow()

    private val _registerOtpEmail = MutableStateFlow<String?>(null)
    val registerOtpEmail: StateFlow<String?> = _registerOtpEmail.asStateFlow()

    private val _verifiedEmail = MutableStateFlow<String?>(null)
    val verifiedEmail: StateFlow<String?> = _verifiedEmail.asStateFlow()

    private val _verifiedOtp = MutableStateFlow("")
    val verifiedOtp: StateFlow<String> = _verifiedOtp.asStateFlow()

    private val _registerOtpMobile = MutableStateFlow<String?>(null)
    val registerOtpMobile: StateFlow<String?> = _registerOtpMobile.asStateFlow()

    private val _verifiedMobile = MutableStateFlow<String?>(null)
    val verifiedMobile: StateFlow<String?> = _verifiedMobile.asStateFlow()

    private val _verifiedMobileOtp = MutableStateFlow("")
    val verifiedMobileOtp: StateFlow<String> = _verifiedMobileOtp.asStateFlow()

    private val _phoneVerificationId = MutableStateFlow("")

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _loginSuccess.value = false
            _pendingVerification.value = null

            applyLogin(repository.login(LoginRequest(username, password)))
            _isLoading.value = false
        }
    }

    private fun applyLogin(result: Result<VendorSession>) {
        result.onSuccess { session ->
            SessionManager.login(session, promptShopTrust = true)
            _loginSuccess.value = true
        }.onFailure { error ->
            if (error is AuthPendingException) {
                _pendingVerification.value = error.pending
                _errorMessage.value = error.message
            } else {
                _errorMessage.value = error.message ?: "Unknown error occurred"
            }
        }
    }

    fun register(
        shopName: String,
        username: String,
        city: String,
        state: String,
        mobile: String,
        email: String,
        password: String,
        emailOtp: String,
        mobileOtp: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _pendingVerification.value = null

            val result = repository.register(
                RegisterRequest(
                    shop_name = shopName.trim(),
                    username = username.trim(),
                    location = city.trim(),
                    city = city.trim(),
                    state = state.trim(),
                    mobile = mobile.trim(),
                    email = email.trim(),
                    password = password,
                    email_otp = emailOtp.trim(),
                    mobile_otp = mobileOtp.trim()
                )
            )
            result.onSuccess { pending ->
                _pendingVerification.value = pending
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Registration failed"
            }
            _isLoading.value = false
        }
    }

    fun sendRegisterOtp(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            repository.sendRegisterOtp(email)
                .onSuccess {
                    _infoMessage.value = it
                    _registerOtpEmail.value = email.trim().lowercase()
                    _verifiedEmail.value = null
                    _verifiedOtp.value = ""
                }
                .onFailure { _errorMessage.value = it.message ?: "Could not send OTP" }
            _isLoading.value = false
        }
    }

    fun verifyRegisterOtp(email: String, otp: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            repository.verifyRegisterOtp(email, otp)
                .onSuccess {
                    _infoMessage.value = it
                    _verifiedEmail.value = email.trim().lowercase()
                    _verifiedOtp.value = otp.trim()
                }
                .onFailure { _errorMessage.value = it.message ?: "Could not verify this email" }
            _isLoading.value = false
        }
    }

    fun sendRegisterMobileOtp(mobile: String) {
        val start = PhoneAuthBridge.start
        if (start == null) {
            _errorMessage.value = "Phone verification is not ready in this build."
            return
        }
        bindPhoneCallbacks(mobile.trim())
        _isLoading.value = true
        _errorMessage.value = null
        _infoMessage.value = null
        _verifiedMobile.value = null
        _verifiedMobileOtp.value = ""
        _phoneVerificationId.value = ""
        start("+91" + mobile.trim())
    }

    fun verifyRegisterMobileOtp(mobile: String, otp: String) {
        val confirm = PhoneAuthBridge.confirm
        val verificationId = _phoneVerificationId.value
        if (confirm == null || verificationId.isBlank()) {
            _errorMessage.value = "Send an OTP to this mobile number first."
            return
        }
        bindPhoneCallbacks(mobile.trim())
        _isLoading.value = true
        _errorMessage.value = null
        _infoMessage.value = null
        confirm(verificationId, otp.trim())
        viewModelScope.launch {
            delay(20000)
            if (_isLoading.value && _verifiedMobile.value != mobile.trim()) {
                _isLoading.value = false
                if (_errorMessage.value.isNullOrBlank()) {
                    _errorMessage.value = "Could not verify this mobile number. Send the SMS again."
                }
            }
        }
    }

    private fun bindPhoneCallbacks(mobile: String) {
        PhoneAuthBridge.bind(
            onCodeSent = { verificationId ->
                _phoneVerificationId.value = verificationId
                _registerOtpMobile.value = mobile
                _infoMessage.value = "OTP sent to your mobile number."
                _isLoading.value = false
            },
            onVerified = { token ->
                viewModelScope.launch {
                    repository.verifyRegisterMobileToken(mobile, token)
                        .onSuccess {
                            _infoMessage.value = it
                            _verifiedMobile.value = mobile
                            _verifiedMobileOtp.value = ""
                        }
                        .onFailure {
                            _errorMessage.value = it.message ?: "Could not verify this mobile number"
                        }
                    _isLoading.value = false
                }
            },
            onError = { message ->
                _errorMessage.value = message
                _isLoading.value = false
            }
        )
    }

    fun sendPasswordOtp(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            repository.sendPasswordOtp(email)
                .onSuccess { _infoMessage.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Could not send OTP" }
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String, otp: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _passwordResetDone.value = false
            repository.resetPassword(email, otp, password)
                .onSuccess {
                    _infoMessage.value = it
                    _passwordResetDone.value = true
                }
                .onFailure { _errorMessage.value = it.message ?: "Could not reset password" }
            _isLoading.value = false
        }
    }

    fun loadSellerStatus(vendorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getVendorStatus(vendorId)
            result.onSuccess { loaded ->
                _sellerStatus.value = loaded
                val current = SessionManager.currentSession.value
                if (current != null && current.id == loaded.id) {
                    SessionManager.login(
                        loaded.copy(daysLeft = if (loaded.daysLeft > 0) loaded.daysLeft else current.daysLeft)
                    )
                } else if (loaded.isVerified) {
                    SessionManager.login(loaded)
                }
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
            _isLoading.value = false
        }
    }

    fun subscriptionUrl(vendorId: Int): String {
        return repository.subscriptionCheckoutUrl(vendorId)
    }

    fun saveShopVerification(gstin: String, shopPhoto: ByteArray?) {
        val vendorId = _sellerStatus.value?.id ?: SessionManager.currentSession.value?.id
        if (vendorId == null) {
            _errorMessage.value = "Login first to verify your shop."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _infoMessage.value = null
            repository.saveShopTrust(vendorId, gstin, shopPhoto)
                .onSuccess { saved ->
                    val current = SessionManager.currentSession.value
                    val merged = saved.session.copy(
                        daysLeft = if (saved.session.daysLeft > 0) saved.session.daysLeft else current?.daysLeft ?: 0
                    )
                    _sellerStatus.value = merged
                    if (current == null || current.id == merged.id) {
                        SessionManager.login(merged)
                    }
                    _infoMessage.value = saved.message
                }
                .onFailure { _errorMessage.value = it.message ?: "Could not save verification" }
            _isLoading.value = false
        }
    }

    fun clearPending() {
        _pendingVerification.value = null
    }
}
