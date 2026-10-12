package com.mylaptopmarket.network

import com.mylaptopmarket.util.readSessionJson
import com.mylaptopmarket.util.writeSessionJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class VendorSession(
    val id: Int,
    val username: String,
    val shopName: String,
    val email: String,
    val mobile: String = "",
    val location: String = "",
    val state: String = "",
    val isVerified: Boolean = false,
    val subscriptionEnd: String? = null,
    val daysLeft: Int = 0,
    val logoUrl: String? = null,
    val gstin: String = "",
    val gstVerified: Boolean = false,
    val gstProfile: GstProfile? = null,
    val shopPhotoUrl: String? = null,
    val shopTrusted: Boolean = false,
    val adminWarning: String = ""
)

@Serializable
data class GstProfile(
    val legalName: String = "",
    val tradeName: String = "",
    val status: String = "",
    val constitution: String = "",
    val taxpayerType: String = "",
    val registrationDate: String = "",
    val stateJurisdiction: String = "",
    val centralJurisdiction: String = "",
    val address: String = ""
)

object SessionManager {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _currentSession = MutableStateFlow(loadSaved())
    val currentSession: StateFlow<VendorSession?> = _currentSession.asStateFlow()

    private val _shopTrustPrompt = MutableStateFlow(false)
    val shopTrustPrompt: StateFlow<Boolean> = _shopTrustPrompt.asStateFlow()

    fun login(vendor: VendorSession, promptShopTrust: Boolean = false) {
        _currentSession.value = vendor
        persist(vendor)
        if (promptShopTrust) _shopTrustPrompt.value = true
    }

    fun dismissShopTrustPrompt() {
        _shopTrustPrompt.value = false
    }

    fun update(transform: (VendorSession) -> VendorSession) {
        val next = _currentSession.value?.let(transform)
        _currentSession.value = next
        persist(next)
    }

    fun logout() {
        _currentSession.value = null
        _shopTrustPrompt.value = false
        persist(null)
    }

    fun restore() {
        if (_currentSession.value == null) {
            _currentSession.value = loadSaved()
        }
    }

    private fun loadSaved(): VendorSession? {
        val raw = readSessionJson()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { json.decodeFromString<VendorSession>(raw) }.getOrNull()
    }

    private fun persist(session: VendorSession?) {
        writeSessionJson(session?.let { json.encodeToString(it) })
    }
}
