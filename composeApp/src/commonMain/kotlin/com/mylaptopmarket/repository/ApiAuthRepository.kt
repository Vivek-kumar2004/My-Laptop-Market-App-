package com.mylaptopmarket.repository

import com.mylaptopmarket.network.GstProfile
import com.mylaptopmarket.network.KtorClient
import com.mylaptopmarket.util.SHOW_VENDOR_PAYMENT
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.network.VendorSession
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class FirebaseCodeRequest(val code: String)

@Serializable
data class RegisterRequest(
    val shop_name: String,
    val username: String,
    val location: String,
    val city: String = "",
    val state: String = "",
    val mobile: String,
    val email: String,
    val password: String,
    val email_otp: String = "",
    val mobile_otp: String = "",
    val gstin: String = ""
)

@Serializable
data class CompleteProfileRequest(
    val vendor_id: Int,
    val shop_name: String,
    val username: String,
    val city: String,
    val state: String,
    val mobile: String,
    val mobile_otp: String = "",
    val password: String = "",
    val gstin: String = ""
)

@Serializable
data class GstCheckRequest(val gstin: String)

@Serializable
data class UsernameAvailabilityResponse(
    val ok: Boolean = false,
    val available: Boolean = false,
    val username: String? = null,
    val suggestion: String? = null,
    val message: String? = null
)

@Serializable
data class GstCheckResponse(
    val ok: Boolean = false,
    val empty: Boolean = false,
    val verified: Boolean = false,
    val gstin: String? = null,
    val message: String? = null
)

data class UsernameAvailability(
    val ok: Boolean,
    val available: Boolean,
    val username: String,
    val suggestion: String,
    val message: String
)

data class GstCheckResult(
    val ok: Boolean,
    val empty: Boolean,
    val verified: Boolean,
    val gstin: String,
    val message: String
)

@Serializable
data class EmailOtpRequest(val email: String)

@Serializable
data class VerifyEmailOtpRequest(val email: String, val email_otp: String)

@Serializable
data class VerifyMobileTokenRequest(val mobile: String, val firebase_token: String)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String
)

@Serializable
data class DeleteAccountRequest(
    val vendor_id: Int,
    val password: String
)

@Serializable
data class VendorPushTokenRequest(
    val vendor_id: Int,
    val token: String,
    val platform: String = "ios",
    val environment: String = "sandbox"
)

@Serializable
data class AcknowledgeWarningRequest(val vendor_id: Int)

@Serializable
data class VendorData(
    val id: Int,
    val username: String,
    val shop_name: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val location: String? = null,
    val state: String? = null,
    val status: Int? = null,
    val verified: Boolean? = null,
    @SerialName("profile_complete") val profileComplete: Boolean? = null,
    val subscription_start: String? = null,
    val subscription_end: String? = null,
    val logo: String? = null,
    val logo_url: String? = null,
    val gstin: String? = null,
    @SerialName("gst_verified") val gstVerified: Boolean? = null,
    @SerialName("gst_details") val gstDetails: GstDetailsData? = null,
    @SerialName("shop_photo_url") val shopPhotoUrl: String? = null,
    @SerialName("shop_trusted") val shopTrusted: Boolean? = null,
    @SerialName("admin_warning") val adminWarning: String? = null
)

@Serializable
data class GstDetailsData(
    @SerialName("legal_name") val legalName: String? = null,
    @SerialName("trade_name") val tradeName: String? = null,
    val status: String? = null,
    val constitution: String? = null,
    @SerialName("taxpayer_type") val taxpayerType: String? = null,
    @SerialName("registration_date") val registrationDate: String? = null,
    @SerialName("state_jurisdiction") val stateJurisdiction: String? = null,
    @SerialName("central_jurisdiction") val centralJurisdiction: String? = null,
    val address: String? = null
)

@Serializable
data class LoginResponse(
    val success: Boolean = false,
    val message: String? = null,
    val code: String? = null,
    val vendor_id: Int? = null,
    val vendor: VendorData? = null
)

@Serializable
data class VendorStatusResponse(
    val success: Boolean = false,
    val message: String? = null,
    val verified: Boolean = false,
    val days_left: Double = 0.0,
    val vendor: VendorData? = null
)

data class AuthPending(
    val vendorId: Int,
    val code: String,
    val message: String
)

data class ShopTrustSave(
    val session: VendorSession,
    val message: String
)

class ApiAuthRepository {
    private val client = KtorClient.httpClient
    private val baseUrl = "${KtorClient.getBaseUrl()}api"

    fun subscriptionCheckoutUrl(vendorId: Int): String {
        return "${KtorClient.getBaseUrl()}subscription/checkout?vendor_id=$vendorId&source=app"
    }

    suspend fun login(request: LoginRequest): Result<VendorSession> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response.asSessionResult("Invalid username or password")
        } catch (e: AuthPendingException) {
            Result.failure(e)
        } catch (e: AuthNeedsProfileException) {
            Result.failure(e)
        } catch (e: Exception) {
            val errorMsg = if (e.message?.contains("required", ignoreCase = true) == true) {
                "Server error: Database not reachable. Please start MySQL in XAMPP."
            } else {
                e.message ?: "Login failed"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun loginWithFirebaseCode(code: String): Result<VendorSession> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/firebase-code") {
                contentType(ContentType.Application.Json)
                setBody(FirebaseCodeRequest(code))
            }.body()
            response.asSessionResult("Google sign-in could not be finished")
        } catch (e: AuthPendingException) {
            Result.failure(e)
        } catch (e: AuthNeedsProfileException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Google sign-in could not be finished"))
        }
    }

    private fun LoginResponse.asSessionResult(fallback: String): Result<VendorSession> {
        val needsProfile = code == "needs_profile" ||
            (success && vendor != null && vendor.profileComplete == false)
        if (needsProfile) {
            val id = vendor_id ?: vendor?.id ?: 0
            return Result.failure(
                AuthNeedsProfileException(
                    message = message ?: "Finish a few business details to continue.",
                    vendorId = id,
                    email = vendor?.email.orEmpty()
                )
            )
        }
        return when {
            success && vendor != null -> Result.success(vendor.toSession(daysLeft = 0))
            code == "pending_verification" || code == "expired" -> Result.failure(
                AuthPendingException(
                    message = message ?: "Seller verification pending",
                    pending = AuthPending(
                        vendorId = vendor_id ?: vendor?.id ?: 0,
                        code = code ?: "pending_verification",
                        message = message ?: if (SHOW_VENDOR_PAYMENT) {
                            "Activate subscription to get verified."
                        } else {
                            "Seller verification pending"
                        }
                    )
                )
            )
            else -> Result.failure(Exception(message ?: fallback))
        }
    }

    suspend fun usernameAvailable(username: String = "", shop: String = ""): Result<UsernameAvailability> {
        return try {
            val response: UsernameAvailabilityResponse = client.get("$baseUrl/vendor/username-available") {
                if (username.isNotBlank()) parameter("u", username.trim())
                if (shop.isNotBlank()) parameter("shop", shop.trim())
            }.body()
            Result.success(
                UsernameAvailability(
                    ok = response.ok,
                    available = response.available,
                    username = response.username.orEmpty(),
                    suggestion = response.suggestion ?: response.username.orEmpty(),
                    message = response.message.orEmpty()
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not check username"))
        }
    }

    suspend fun gstCheck(gstin: String): Result<GstCheckResult> {
        return try {
            val response: GstCheckResponse = client.post("$baseUrl/vendor/gst-check") {
                contentType(ContentType.Application.Json)
                setBody(GstCheckRequest(gstin.trim()))
            }.body()
            Result.success(
                GstCheckResult(
                    ok = response.ok,
                    empty = response.empty,
                    verified = response.verified,
                    gstin = response.gstin.orEmpty(),
                    message = response.message.orEmpty()
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not check GSTIN"))
        }
    }

    suspend fun completeProfile(request: CompleteProfileRequest): Result<AuthPending> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/complete-profile") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            if (response.success && (response.vendor_id != null || response.vendor != null)) {
                Result.success(
                    AuthPending(
                        vendorId = response.vendor_id ?: response.vendor!!.id,
                        code = "pending_verification",
                        message = response.message ?: if (SHOW_VENDOR_PAYMENT) {
                            "Profile saved. Activate subscription to go live."
                        } else {
                            "Profile saved."
                        }
                    )
                )
            } else {
                Result.failure(Exception(response.message ?: "Could not save profile"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not save profile"))
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthPending> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            if (response.success && (response.vendor_id != null || response.vendor != null)) {
                Result.success(
                    AuthPending(
                        vendorId = response.vendor_id ?: response.vendor!!.id,
                        code = "pending_verification",
                        message = response.message ?: if (SHOW_VENDOR_PAYMENT) {
                            "Account created. Activate subscription to get verified."
                        } else {
                            "Account created."
                        }
                    )
                )
            } else {
                Result.failure(Exception(response.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration failed"))
        }
    }

    suspend fun getVendorStatus(vendorId: Int): Result<VendorSession> {
        return try {
            val response: VendorStatusResponse = client.get("$baseUrl/vendor/status/$vendorId").body()
            if (response.success && response.vendor != null) {
                Result.success(response.vendor.toSession(daysLeft = response.days_left.toInt()))
            } else {
                Result.failure(Exception(response.message ?: "Could not load seller status"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not load seller status"))
        }
    }

    suspend fun updateProfile(
        vendorId: Int,
        shopName: String,
        mobile: String,
        location: String,
        state: String,
        email: String,
        logo: ByteArray?,
        removeLogo: Boolean,
        gstin: String = "",
        shopPhoto: ByteArray? = null
    ): Result<VendorSession> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/profile") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("vendor_id", vendorId.toString())
                            append("shop_name", shopName)
                            append("mobile", mobile)
                            append("location", location)
                            append("city", location)
                            append("state", state)
                            append("email", email)
                            if (removeLogo && logo == null) {
                                append("remove_logo", "1")
                            }
                            if (logo != null) {
                                append(
                                    "logo",
                                    logo,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"logo.jpg\"")
                                    }
                                )
                            }
                            append("gstin", gstin)
                            if (shopPhoto != null) {
                                append(
                                    "shop_photo",
                                    shopPhoto,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"shop.jpg\"")
                                    }
                                )
                            }
                        }
                    )
                )
            }.body()
            if (response.success && response.vendor != null) {
                Result.success(
                    response.vendor.toSession(
                        daysLeft = SessionManager.currentSession.value?.daysLeft ?: 0
                    )
                )
            } else {
                Result.failure(Exception(response.message ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to update profile"))
        }
    }

    suspend fun saveShopTrust(
        vendorId: Int,
        gstin: String,
        shopPhoto: ByteArray?
    ): Result<ShopTrustSave> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/shop_trust") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("vendor_id", vendorId.toString())
                            append("gstin", gstin.trim())
                            if (shopPhoto != null) {
                                append(
                                    "shop_photo",
                                    shopPhoto,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"shop.jpg\"")
                                    }
                                )
                            }
                        }
                    )
                )
            }.body()
            if (response.success && response.vendor != null) {
                Result.success(
                    ShopTrustSave(
                        session = response.vendor.toSession(
                            daysLeft = SessionManager.currentSession.value?.daysLeft ?: 0
                        ),
                        message = response.message ?: "Saved"
                    )
                )
            } else {
                Result.failure(Exception(response.message ?: "Could not save shop verification"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not save shop verification"))
        }
    }

    suspend fun sendRegisterOtp(email: String): Result<String> {
        return postAuthMessage("$baseUrl/vendor/email_otp", EmailOtpRequest(email.trim()))
    }

    suspend fun verifyRegisterOtp(email: String, otp: String): Result<String> {
        return postAuthMessage(
            "$baseUrl/vendor/email_verify",
            VerifyEmailOtpRequest(email = email.trim(), email_otp = otp.trim())
        )
    }

    suspend fun verifyRegisterMobileToken(mobile: String, firebaseToken: String): Result<String> {
        return postAuthMessage(
            "$baseUrl/vendor/mobile_verify",
            VerifyMobileTokenRequest(mobile = mobile.trim(), firebase_token = firebaseToken)
        )
    }

    suspend fun sendPasswordOtp(email: String): Result<String> {
        return postAuthMessage("$baseUrl/vendor/forgot_password", ForgotPasswordRequest(email.trim()))
    }

    suspend fun resetPassword(email: String, otp: String, password: String): Result<String> {
        return postAuthMessage(
            "$baseUrl/vendor/reset_password",
            ResetPasswordRequest(email = email.trim(), otp = otp.trim(), password = password)
        )
    }

    private suspend inline fun <reified T> postAuthMessage(url: String, body: T): Result<String> {
        return try {
            val response: LoginResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
            if (response.success) Result.success(response.message ?: "Done")
            else Result.failure(Exception(response.message ?: "Request failed"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Request failed"))
        }
    }

    suspend fun deleteAccount(vendorId: Int, password: String): Result<String> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/delete_account") {
                contentType(ContentType.Application.Json)
                setBody(DeleteAccountRequest(vendor_id = vendorId, password = password))
            }.body()
            if (response.success) {
                Result.success(response.message ?: "Account deleted")
            } else {
                Result.failure(Exception(response.message ?: "Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to delete account"))
        }
    }

    suspend fun savePushToken(vendorId: Int, token: String, environment: String, platform: String = "ios") {
        runCatching {
            client.post("$baseUrl/vendor/push-token") {
                contentType(ContentType.Application.Json)
                setBody(
                    VendorPushTokenRequest(
                        vendor_id = vendorId,
                        token = token,
                        platform = platform,
                        environment = environment
                    )
                )
            }
        }
    }

    suspend fun clearPushToken(token: String) {
        runCatching {
            client.post("$baseUrl/vendor/push-token/delete") {
                contentType(ContentType.Application.Json)
                setBody(VendorPushTokenRequest(vendor_id = 0, token = token))
            }
        }
    }

    suspend fun acknowledgeWarning(vendorId: Int): Result<Unit> {
        return try {
            val response: LoginResponse = client.post("$baseUrl/vendor/warning/ack") {
                contentType(ContentType.Application.Json)
                setBody(AcknowledgeWarningRequest(vendor_id = vendorId))
            }.body()
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Could not acknowledge the warning"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not acknowledge the warning"))
        }
    }

    private fun VendorData.toSession(daysLeft: Int): VendorSession {
        val resolvedLogo = when {
            !logo_url.isNullOrBlank() -> logo_url
            !logo.isNullOrBlank() -> {
                val file = logo.substringAfterLast('/')
                "${KtorClient.getMediaBaseUrl()}assets/uploads/vendors/$file"
            }
            else -> null
        }
        return VendorSession(
            id = id,
            username = username,
            shopName = shop_name ?: username,
            email = email ?: "",
            mobile = mobile ?: "",
            location = location ?: "",
            state = state ?: "",
            isVerified = verified == true || status == 1,
            profileComplete = profileComplete != false,
            subscriptionEnd = subscription_end,
            daysLeft = daysLeft,
            logoUrl = resolvedLogo,
            gstin = gstin.orEmpty(),
            gstVerified = gstVerified == true,
            gstProfile = gstDetails?.toProfile(),
            shopPhotoUrl = shopPhotoUrl,
            shopTrusted = shopTrusted == true,
            adminWarning = adminWarning.orEmpty()
        )
    }

    private fun GstDetailsData.toProfile(): GstProfile? {
        val profile = GstProfile(
            legalName = legalName.orEmpty(),
            tradeName = tradeName.orEmpty(),
            status = status.orEmpty(),
            constitution = constitution.orEmpty(),
            taxpayerType = taxpayerType.orEmpty(),
            registrationDate = registrationDate.orEmpty(),
            stateJurisdiction = stateJurisdiction.orEmpty(),
            centralJurisdiction = centralJurisdiction.orEmpty(),
            address = address.orEmpty()
        )
        val hasAny = listOf(
            profile.legalName,
            profile.tradeName,
            profile.status,
            profile.constitution,
            profile.taxpayerType,
            profile.registrationDate,
            profile.address
        ).any { it.isNotBlank() }
        return if (hasAny) profile else null
    }
}

class AuthPendingException(
    message: String,
    val pending: AuthPending
) : Exception(message)

class AuthNeedsProfileException(
    message: String,
    val vendorId: Int,
    val email: String = ""
) : Exception(message)
