package com.mylaptopmarket.repository

import com.mylaptopmarket.model.MyRequirement
import com.mylaptopmarket.model.RequirementGroup
import com.mylaptopmarket.model.RequirementVendor
import com.mylaptopmarket.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class RequirementFeedResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<RequirementGroupDto> = emptyList()
)

@Serializable
private data class RequirementGroupDto(
    @SerialName("title_key") val titleKey: String? = null,
    @SerialName("cluster_key") val clusterKey: String? = null,
    val title: String? = null,
    @SerialName("canonical_title") val canonicalTitle: String? = null,
    val category: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    val vendors: List<RequirementVendorDto> = emptyList(),
    val count: Int? = null,
    @SerialName("latest_at") val latestAt: String? = null,
    @SerialName("my_requirement_id") val myRequirementId: Int? = null
)

@Serializable
private data class RequirementVendorDto(
    val id: Int? = null,
    @SerialName("shop_name") val shopName: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("requirement_id") val requirementId: Int? = null,
    val title: String? = null,
    @SerialName("canonical_title") val canonicalTitle: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
private data class MyRequirementsResponse(
    val status: Boolean = false,
    val message: String? = null,
    val data: List<MyRequirementDto> = emptyList()
)

@Serializable
private data class MyRequirementDto(
    val id: Int? = null,
    val title: String? = null,
    val category: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    val status: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
private data class SimpleStatusResponse(
    val status: Boolean = false,
    val message: String? = null
)

@Serializable
private data class LaravelErrorResponse(
    val message: String? = null
)

class ApiRequirementRepository {
    private val baseUrl = "${KtorClient.getBaseUrl()}api"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun getFeed(vendorId: Int?): List<RequirementGroup> {
        val http = KtorClient.httpClient.get("$baseUrl/requirements") {
            if (vendorId != null) parameter("vendor_id", vendorId)
        }
        val text = http.bodyAsText()
        if (!http.status.isSuccess()) {
            throw Exception(friendlyApiError(text, http.status.value))
        }
        val response = json.decodeFromString<RequirementFeedResponse>(text)
        if (!response.status) {
            throw Exception(response.message ?: "Failed to load requirements")
        }
        return response.data.map { it.toDomain() }
    }

    suspend fun getMine(vendorId: Int): List<MyRequirement> {
        val http = KtorClient.httpClient.get("$baseUrl/requirements/mine") {
            parameter("vendor_id", vendorId)
        }
        val text = http.bodyAsText()
        if (!http.status.isSuccess()) {
            throw Exception(friendlyApiError(text, http.status.value))
        }
        val response = json.decodeFromString<MyRequirementsResponse>(text)
        if (!response.status) return emptyList()
        return response.data.mapNotNull { dto ->
            val id = dto.id ?: return@mapNotNull null
            MyRequirement(
                id = id,
                title = dto.title.orEmpty(),
                category = dto.category.orEmpty(),
                quantity = dto.quantity,
                description = dto.description,
                status = dto.status ?: "active",
                updatedAt = dto.updatedAt
            )
        }
    }

    suspend fun create(
        vendorId: Int,
        title: String,
        category: String,
        quantity: Int?,
        description: String?
    ): Result<Boolean> {
        return try {
            val http = KtorClient.httpClient.submitForm(
                url = "$baseUrl/requirements",
                formParameters = Parameters.build {
                    append("vendor_id", vendorId.toString())
                    append("title", title)
                    append("category", category)
                    if (quantity != null) append("quantity", quantity.toString())
                    if (!description.isNullOrBlank()) append("description", description)
                }
            )
            val text = http.bodyAsText()
            if (!http.status.isSuccess()) {
                return Result.failure(Exception(friendlyApiError(text, http.status.value)))
            }
            val response = json.decodeFromString<SimpleStatusResponse>(text)
            if (response.status) Result.success(true)
            else Result.failure(Exception(response.message ?: "Failed to post requirement"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun close(vendorId: Int, requirementId: Int): Result<Boolean> {
        return try {
            val http = KtorClient.httpClient.submitForm(
                url = "$baseUrl/requirements/$requirementId/close",
                formParameters = Parameters.build {
                    append("vendor_id", vendorId.toString())
                }
            )
            val text = http.bodyAsText()
            if (!http.status.isSuccess()) {
                return Result.failure(Exception(friendlyApiError(text, http.status.value)))
            }
            val response = json.decodeFromString<SimpleStatusResponse>(text)
            if (response.status) Result.success(true)
            else Result.failure(Exception(response.message ?: "Failed to close"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(vendorId: Int, requirementId: Int): Result<Boolean> {
        return try {
            val http = KtorClient.httpClient.submitForm(
                url = "$baseUrl/requirements/$requirementId/delete",
                formParameters = Parameters.build {
                    append("vendor_id", vendorId.toString())
                }
            )
            val text = http.bodyAsText()
            if (!http.status.isSuccess()) {
                return Result.failure(Exception(friendlyApiError(text, http.status.value)))
            }
            val response = json.decodeFromString<SimpleStatusResponse>(text)
            if (response.status) Result.success(true)
            else Result.failure(Exception(response.message ?: "Failed to delete"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun friendlyApiError(body: String, code: Int): String {
        val laravelMessage = runCatching {
            json.decodeFromString<LaravelErrorResponse>(body).message
        }.getOrNull()
        if (laravelMessage?.contains("could not be found", ignoreCase = true) == true || code == 404) {
            return "Requirements API not deployed on server yet. Upload backend files and run DB SQL first."
        }
        return laravelMessage ?: "Server error ($code)"
    }

    private fun RequirementGroupDto.toDomain() = RequirementGroup(
        titleKey = clusterKey?.takeIf { it.isNotBlank() } ?: titleKey.orEmpty(),
        title = canonicalTitle?.takeIf { it.isNotBlank() } ?: title.orEmpty(),
        category = category.orEmpty(),
        quantity = quantity,
        description = description,
        vendors = vendors.map {
            RequirementVendor(
                id = it.id ?: 0,
                shopName = it.shopName ?: "Vendor",
                logoUrl = it.logoUrl,
                requirementId = it.requirementId,
                title = it.title,
                canonicalTitle = it.canonicalTitle,
                quantity = it.quantity,
                description = it.description,
                updatedAt = it.updatedAt
            )
        },
        count = count ?: vendors.size,
        latestAt = latestAt,
        myRequirementId = myRequirementId
    )
}
