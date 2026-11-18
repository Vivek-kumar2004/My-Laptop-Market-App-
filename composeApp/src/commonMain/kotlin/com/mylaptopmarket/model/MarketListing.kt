package com.mylaptopmarket.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketListing(
    val id: Int = 0,
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val price: String = "0",
    val stock: Int = 0,
    val images: List<String> = emptyList(),
    val specs: List<String> = emptyList(),
    val condition: String = "",
    @SerialName("contact_name") val contactName: String = "",
    @SerialName("contact_phone") val contactPhone: String = "",
    val city: String = "",
    val state: String = "",
    val independent: Boolean = true
)
