package com.mylaptopmarket.repository

import com.mylaptopmarket.network.AdminSession
import com.mylaptopmarket.network.AdminSessionManager
import com.mylaptopmarket.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class AdminLoginResponse(
    val success: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val admin: AdminUserData? = null
)

@Serializable
data class AdminUserData(
    val id: Int = 0,
    val username: String = ""
)

@Serializable
data class AdminDashboardData(
    @SerialName("active_vendors") val activeVendors: Int = 0,
    @SerialName("inactive_vendors") val inactiveVendors: Int = 0,
    @SerialName("total_vendors") val totalVendors: Int = 0,
    @SerialName("new_inquiries") val newInquiries: Int = 0,
    @SerialName("total_inquiries") val totalInquiries: Int = 0,
    @SerialName("total_invoices") val totalInvoices: Int = 0,
    @SerialName("total_products") val totalProducts: Int = 0,
    @SerialName("active_requirements") val activeRequirements: Int = 0,
    @SerialName("total_requirements") val totalRequirements: Int = 0
)

@Serializable
data class AdminDashboardResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminDashboardData? = null
)

@Serializable
data class AdminVendorData(
    val id: Int = 0,
    val username: String = "",
    @SerialName("shop_name") val shopName: String? = null,
    val mobile: String? = null,
    val location: String? = null,
    val status: Int = 0,
    @SerialName("subscription_start") val subscriptionStart: String? = null,
    @SerialName("subscription_end") val subscriptionEnd: String? = null,
    @SerialName("password_plain") val passwordPlain: String? = null,
    val gstin: String? = null,
    val state: String? = null,
    @SerialName("state_code") val stateCode: String? = null,
    @SerialName("billing_address") val billingAddress: String? = null,
    @SerialName("contact_person") val contactPerson: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("admin_warning") val adminWarning: String? = null
)

@Serializable
data class AdminVendorsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<AdminVendorData> = emptyList()
)

@Serializable
data class AdminVendorResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminVendorData? = null
)

@Serializable
data class AdminMessageResponse(
    val success: Boolean = false,
    val message: String? = null
)

@Serializable
data class AdminWarnRequest(val message: String)

@Serializable
data class AnnouncementData(val text: String = "")

@Serializable
data class AnnouncementResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AnnouncementData? = null
)

@Serializable
data class AdminInquiryData(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "read",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminInquiriesResponse(
    val success: Boolean = false,
    val message: String? = null,
    @SerialName("new_count") val newCount: Int = 0,
    val data: List<AdminInquiryData> = emptyList()
)

@Serializable
data class AdminInquiryResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminInquiryData? = null
)

@Serializable
data class AdminInvoiceData(
    val id: Int = 0,
    @SerialName("invoice_number") val invoiceNumber: String? = null,
    @SerialName("vendor_id") val vendorId: Int? = null,
    @SerialName("vendor_name") val vendorName: String? = null,
    @SerialName("vendor_mobile") val vendorMobile: String? = null,
    @SerialName("vendor_gstin") val vendorGstin: String? = null,
    val amount: Double = 0.0,
    val subtotal: Double? = null,
    val cgst: Double? = null,
    val sgst: Double? = null,
    val igst: Double? = null,
    @SerialName("razorpay_payment_id") val razorpayPaymentId: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AdminInvoiceResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminInvoiceData? = null
)

@Serializable
data class AdminInvoicesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<AdminInvoiceData> = emptyList()
)

@Serializable
data class AdminProductData(
    val id: Int = 0,
    @SerialName("product_name") val productName: String = "",
    val category: String? = null,
    val price: Double = 0.0,
    val stock: Int = 0,
    val status: Int = 0,
    @SerialName("vendor_id") val vendorId: Int? = null,
    @SerialName("vendor_name") val vendorName: String? = null,
    @SerialName("main_image") val mainImage: String? = null
)

@Serializable
data class AdminProductsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<AdminProductData> = emptyList()
)

@Serializable
data class AdminRequirementData(
    val id: Int = 0,
    @SerialName("vendor_id") val vendorId: Int = 0,
    @SerialName("vendor_name") val vendorName: String = "",
    @SerialName("vendor_username") val vendorUsername: String? = null,
    @SerialName("vendor_mobile") val vendorMobile: String? = null,
    @SerialName("vendor_logo_url") val vendorLogoUrl: String? = null,
    val title: String = "",
    @SerialName("canonical_title") val canonicalTitle: String? = null,
    val category: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AdminRequirementVendorData(
    val id: Int = 0,
    @SerialName("shop_name") val shopName: String = "",
    val username: String? = null,
    val mobile: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("requirement_id") val requirementId: Int? = null,
    val title: String? = null,
    @SerialName("canonical_title") val canonicalTitle: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AdminRequirementGroupData(
    @SerialName("title_key") val titleKey: String? = null,
    @SerialName("cluster_key") val clusterKey: String? = null,
    val title: String = "",
    @SerialName("canonical_title") val canonicalTitle: String? = null,
    val category: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    val count: Int = 0,
    @SerialName("active_count") val activeCount: Int = 0,
    @SerialName("latest_at") val latestAt: String? = null,
    val vendors: List<AdminRequirementVendorData> = emptyList()
)

@Serializable
data class AdminRequirementsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<AdminRequirementGroupData> = emptyList()
)

@Serializable
data class AdminRequirementResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminRequirementData? = null
)

@Serializable
data class AdminVendorWriteRequest(
    val shop_name: String,
    val username: String,
    val mobile: String,
    val password: String? = null,
    val location: String? = null,
    val gstin: String? = null,
    val state: String? = null,
    val state_code: String? = null,
    val billing_address: String? = null,
    val contact_person: String? = null
)

@Serializable
data class AdminPushTokenRequest(
    val token: String,
    val platform: String = "ios",
    val environment: String = "sandbox"
)

class ApiAdminRepository {
    private val client = KtorClient.httpClient
    private val baseUrl = "${KtorClient.getBaseUrl()}api/admin"

    suspend fun login(username: String, password: String): Result<AdminSession> {
        return try {
            val payload = com.mylaptopmarket.network.AppJson.encodeToString(
                LoginRequest(username, password)
            )
            val httpResponse = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val text = httpResponse.bodyAsText()
            if (httpResponse.status.value !in 200..299) {
                return Result.failure(
                    Exception(
                        "Login failed (${httpResponse.status.value}). " +
                            "Agar ye error bar-bar aaye to server par /api/admin/login check karein."
                    )
                )
            }
            val response = com.mylaptopmarket.network.AppJson.decodeFromString(
                AdminLoginResponse.serializer(),
                text
            )
            if (response.success && !response.token.isNullOrBlank() && response.admin != null) {
                Result.success(
                    AdminSession(
                        id = response.admin.id,
                        username = response.admin.username,
                        token = response.token
                    )
                )
            } else {
                Result.failure(Exception(response.message ?: "Invalid username or password"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(com.mylaptopmarket.network.friendlyNetworkError(e)))
        }
    }

    suspend fun registerPushToken(token: String, environment: String): Result<Unit> {
        return try {
            val response: AdminMessageResponse = client.post("$baseUrl/push-token") {
                applyAdminAuth(requireToken())
                contentType(ContentType.Application.Json)
                setBody(
                    AdminPushTokenRequest(
                        token = token,
                        platform = "ios",
                        environment = environment
                    )
                )
            }.body()
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Could not save push token"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not save push token"))
        }
    }

    suspend fun unregisterPushToken(token: String): Result<Unit> {
        return try {
            val response: AdminMessageResponse = client.post("$baseUrl/push-token/delete") {
                applyAdminAuth(requireToken())
                contentType(ContentType.Application.Json)
                setBody(AdminPushTokenRequest(token = token))
            }.body()
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Could not remove push token"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not remove push token"))
        }
    }

    suspend fun dashboard(): Result<AdminDashboardData> {
        return try {
            val response: AdminDashboardResponse = authedGet("dashboard").body()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Could not load dashboard"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load dashboard"))
        }
    }

    suspend fun vendors(filter: String = "all"): Result<List<AdminVendorData>> {
        return try {
            val response: AdminVendorsResponse = authedGet(
                "vendors",
                extraParams = if (filter == "all") emptyMap() else mapOf("filter" to filter)
            ).body()
            if (response.success) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Could not load vendors"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load vendors"))
        }
    }

    suspend fun products(): Result<List<AdminProductData>> {
        return try {
            val response: AdminProductsResponse = authedGet("products").body()
            if (response.success) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Could not load products"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load products"))
        }
    }

    suspend fun deleteProduct(id: Int): Result<String> {
        return try {
            val response: AdminMessageResponse = authedPost("products/$id/delete").body()
            if (response.success) Result.success(response.message ?: "Product deleted")
            else Result.failure(Exception(response.message ?: "Could not delete product"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not delete product"))
        }
    }

    suspend fun currentAnnouncement(): Result<String> {
        return try {
            val response: AnnouncementResponse = client.get("${KtorClient.getBaseUrl()}api/announcement").body()
            if (response.success) Result.success(response.data?.text.orEmpty())
            else Result.failure(Exception(response.message ?: "Could not load notification"))
        } catch (e: Exception) {
            Result.failure(Exception(com.mylaptopmarket.network.friendlyNetworkError(e)))
        }
    }

    suspend fun saveAnnouncement(message: String): Result<String> {
        return try {
            val token = requireToken()
            val response: AnnouncementResponse = client.post("$baseUrl/announcement") {
                applyAdminAuth(token)
                contentType(ContentType.Application.Json)
                setBody(AdminWarnRequest(message))
            }.body()
            if (response.success) Result.success(response.message ?: "Notification sent")
            else Result.failure(Exception(response.message ?: "Could not send notification"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not send notification"))
        }
    }

    suspend fun warnVendor(id: Int, message: String): Result<String> {
        return try {
            val token = requireToken()
            val response: AdminMessageResponse = client.post("$baseUrl/vendors/$id/warn") {
                applyAdminAuth(token)
                contentType(ContentType.Application.Json)
                setBody(AdminWarnRequest(message))
            }.body()
            if (response.success) Result.success(response.message ?: "Warning sent")
            else Result.failure(Exception(response.message ?: "Could not send warning"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not send warning"))
        }
    }

    suspend fun requirements(filter: String = "all"): Result<List<AdminRequirementGroupData>> {
        return try {
            val response: AdminRequirementsResponse = authedGet(
                "requirements",
                extraParams = if (filter == "all") emptyMap() else mapOf("filter" to filter)
            ).body()
            if (response.success) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Could not load requirements"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load requirements"))
        }
    }

    suspend fun requirement(id: Int): Result<AdminRequirementData> {
        return try {
            val response: AdminRequirementResponse = authedGet("requirements/$id").body()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Requirement not found"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load requirement"))
        }
    }

    suspend fun closeRequirement(id: Int): Result<String> {
        return try {
            val response: AdminMessageResponse = authedPost("requirements/$id/close").body()
            if (response.success) Result.success(response.message ?: "Requirement closed")
            else Result.failure(Exception(response.message ?: "Could not close requirement"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not close requirement"))
        }
    }

    suspend fun vendor(id: Int): Result<AdminVendorData> {
        return try {
            val response: AdminVendorResponse = authedGet("vendors/$id").body()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Vendor not found"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load vendor"))
        }
    }

    suspend fun saveVendor(id: Int?, request: AdminVendorWriteRequest): Result<AdminVendorData> {
        return try {
            val path = if (id == null) "vendors" else "vendors/$id"
            val token = requireToken()
            val response: AdminVendorResponse = client.post("$baseUrl/$path") {
                applyAdminAuth(token)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Could not save vendor"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not save vendor"))
        }
    }

    suspend fun activateVendor(id: Int): Result<String> = vendorAction("vendors/$id/activate")
    suspend fun deactivateVendor(id: Int): Result<String> = vendorAction("vendors/$id/deactivate")
    suspend fun deleteVendor(id: Int): Result<String> = vendorAction("vendors/$id/delete")

    suspend fun inquiries(): Result<Pair<List<AdminInquiryData>, Int>> {
        return try {
            val response: AdminInquiriesResponse = authedGet("inquiries").body()
            if (response.success) Result.success(response.data to response.newCount)
            else Result.failure(Exception(response.message ?: "Could not load inquiries"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load inquiries"))
        }
    }

    suspend fun inquiry(id: Int): Result<AdminInquiryData> {
        return try {
            val response: AdminInquiryResponse = authedGet("inquiries/$id").body()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Inquiry not found"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load inquiry"))
        }
    }

    suspend fun deleteInquiry(id: Int): Result<String> {
        return try {
            val response: AdminMessageResponse = authedPost("inquiries/$id/delete").body()
            if (response.success) Result.success(response.message ?: "Inquiry deleted")
            else Result.failure(Exception(response.message ?: "Could not delete inquiry"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not delete inquiry"))
        }
    }

    suspend fun invoices(): Result<List<AdminInvoiceData>> {
        return try {
            val response: AdminInvoicesResponse = authedGet("invoices").body()
            if (response.success) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Could not load invoices"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load invoices"))
        }
    }

    suspend fun invoice(id: Int): Result<AdminInvoiceData> {
        return try {
            val response: AdminInvoiceResponse = authedGet("invoices/$id").body()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Invoice not found"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Could not load invoice"))
        }
    }

    fun invoiceHtmlUrl(id: Int): String {
        val token = AdminSessionManager.currentSession.value?.token.orEmpty()
        return "$baseUrl/invoices/$id/html?token=$token"
    }

    private suspend fun vendorAction(path: String): Result<String> {
        return try {
            val response: AdminMessageResponse = authedPost(path).body()
            if (response.success) Result.success(response.message ?: "Done")
            else Result.failure(Exception(response.message ?: "Action failed"))
        } catch (e: Exception) {
            Result.failure(authOrNetworkError(e, "Action failed"))
        }
    }

    private suspend fun authedGet(
        path: String,
        extraParams: Map<String, String> = emptyMap()
    ): HttpResponse {
        val token = requireToken()
        return client.get("$baseUrl/$path") {
            applyAdminAuth(token)
            extraParams.forEach { (key, value) -> parameter(key, value) }
        }
    }

    private suspend fun authedPost(path: String): HttpResponse {
        val token = requireToken()
        return client.post("$baseUrl/$path") {
            applyAdminAuth(token)
        }
    }

    private fun HttpRequestBuilder.applyAdminAuth(token: String) {
        // CWP/Apache often strips Authorization; send the token three ways.
        header(HttpHeaders.Authorization, "Bearer $token")
        header("X-Admin-Token", token)
        parameter("token", token)
    }

    private fun requireToken(): String {
        return AdminSessionManager.currentSession.value?.token
            ?: throw UnauthorizedAdminException()
    }

    private fun authOrNetworkError(e: Exception, fallback: String): Exception {
        if (e is UnauthorizedAdminException) {
            AdminSessionManager.logout()
            return Exception("Please login again")
        }
        return Exception(e.message ?: fallback)
    }
}

class UnauthorizedAdminException : Exception("Admin login required")
