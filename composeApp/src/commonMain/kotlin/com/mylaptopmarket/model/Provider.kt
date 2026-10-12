package com.mylaptopmarket.model

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val id: String,
    val name: String,
    val contactPhone: String,
    val productCount: Int = 0,
    val logoUrl: String? = null
)
