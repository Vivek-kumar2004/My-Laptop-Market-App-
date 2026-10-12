package com.mylaptopmarket.network

import com.mylaptopmarket.util.readWishlistJson
import com.mylaptopmarket.util.writeWishlistJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FavoriteDealer(
    val id: String,
    val name: String,
    val location: String = "",
    val logoUrl: String? = null,
    val phone: String = ""
)

object FavoriteDealersManager {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _items = MutableStateFlow(loadSaved())
    val items: StateFlow<List<FavoriteDealer>> = _items.asStateFlow()

    fun contains(vendorId: String): Boolean =
        _items.value.any { it.id == vendorId }

    fun add(dealer: FavoriteDealer) {
        val current = _items.value.filterNot { it.id == dealer.id }.toMutableList()
        current.add(0, dealer)
        _items.value = current
        persist(current)
    }

    fun update(dealer: FavoriteDealer) {
        val current = _items.value.map { if (it.id == dealer.id) dealer else it }
        _items.value = current
        persist(current)
    }

    fun remove(vendorId: String) {
        val next = _items.value.filterNot { it.id == vendorId }
        _items.value = next
        persist(next)
    }

    private fun loadSaved(): List<FavoriteDealer> {
        val raw = readWishlistJson()?.takeIf { it.isNotBlank() } ?: return emptyList()
        return runCatching { json.decodeFromString<List<FavoriteDealer>>(raw) }.getOrDefault(emptyList())
    }

    private fun persist(items: List<FavoriteDealer>) {
        writeWishlistJson(if (items.isEmpty()) null else json.encodeToString(items))
    }
}
