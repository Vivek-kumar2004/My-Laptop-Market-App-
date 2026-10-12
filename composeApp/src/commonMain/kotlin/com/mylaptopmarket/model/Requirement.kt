package com.mylaptopmarket.model

data class RequirementVendor(
    val id: Int,
    val shopName: String,
    val logoUrl: String? = null,
    val requirementId: Int? = null,
    val title: String? = null,
    val canonicalTitle: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    val updatedAt: String? = null
)

data class RequirementGroup(
    val titleKey: String,
    val title: String,
    val category: String,
    val quantity: Int? = null,
    val description: String? = null,
    val vendors: List<RequirementVendor> = emptyList(),
    val count: Int = 0,
    val latestAt: String? = null,
    val myRequirementId: Int? = null
)

data class MyRequirement(
    val id: Int,
    val title: String,
    val category: String,
    val quantity: Int? = null,
    val description: String? = null,
    val status: String,
    val updatedAt: String? = null
)
