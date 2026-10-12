package com.mylaptopmarket.network

import com.mylaptopmarket.util.readAdminSessionJson
import com.mylaptopmarket.util.writeAdminSessionJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AdminSession(
    val id: Int,
    val username: String,
    val token: String
)

object AdminSessionManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val _currentSession = MutableStateFlow(loadSaved())
    val currentSession: StateFlow<AdminSession?> = _currentSession.asStateFlow()

    fun login(session: AdminSession) {
        _currentSession.value = session
        persist(session)
    }

    fun logout() {
        _currentSession.value = null
        persist(null)
    }

    private fun loadSaved(): AdminSession? {
        val raw = readAdminSessionJson()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { json.decodeFromString<AdminSession>(raw) }.getOrNull()
    }

    private fun persist(session: AdminSession?) {
        writeAdminSessionJson(session?.let { json.encodeToString(it) })
    }
}
