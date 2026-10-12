package com.mylaptopmarket.repository

import com.mylaptopmarket.network.KtorClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChatMessage(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val message: String,
    val is_read: Int,
    val created_at: String
)

@Serializable
data class Conversation(
    val contact_id: Int,
    val contact_name: String,
    val last_message: String,
    val last_message_time: String,
    val unread_count: Int
)

@Serializable
data class Contact(
    val id: Int,
    val username: String,
    val shop_name: String?
)

@Serializable
data class ConversationsResponse(val success: Boolean, val conversations: List<Conversation> = emptyList())

@Serializable
data class ChatResponse(val success: Boolean, val messages: List<ChatMessage> = emptyList())

@Serializable
data class ContactsResponse(val success: Boolean, val contacts: List<Contact> = emptyList())

@Serializable
data class SendMessageRequest(
    val sender_id: Int,
    val receiver_id: Int,
    val message: String
)

@Serializable
data class SendMessageResponse(val success: Boolean, val message: ChatMessage? = null)

class ApiMessageRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    private val baseUrl = "${KtorClient.getBaseUrl()}api/messages"

    suspend fun getConversations(vendorId: Int): Result<List<Conversation>> {
        return try {
            val response: ConversationsResponse = client.get("$baseUrl/conversations/$vendorId").body()
            if (response.success) Result.success(response.conversations) else Result.failure(Exception("Failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChat(vendorId: Int, contactId: Int): Result<List<ChatMessage>> {
        return try {
            val response: ChatResponse = client.get("$baseUrl/chat/$vendorId/$contactId").body()
            if (response.success) Result.success(response.messages) else Result.failure(Exception("Failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(request: SendMessageRequest): Result<ChatMessage> {
        return try {
            val response: SendMessageResponse = client.post("$baseUrl/send") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            if (response.success && response.message != null) {
                Result.success(response.message)
            } else {
                Result.failure(Exception("Failed to send"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContacts(vendorId: Int): Result<List<Contact>> {
        return try {
            val response: ContactsResponse = client.get("$baseUrl/contacts/$vendorId").body()
            if (response.success) Result.success(response.contacts) else Result.failure(Exception("Failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
