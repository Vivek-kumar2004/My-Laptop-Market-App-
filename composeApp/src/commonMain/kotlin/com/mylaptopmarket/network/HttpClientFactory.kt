package com.mylaptopmarket.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val AppJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
}

internal fun HttpClientConfig<*>.installAppPlugins(withKtorTimeout: Boolean = true) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(AppJson)
    }
    if (withKtorTimeout) {
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 20_000
            socketTimeoutMillis = 20_000
        }
    }
    defaultRequest {
        header(HttpHeaders.Accept, "application/json")
        header(HttpHeaders.UserAgent, "MyLaptopMarket/1.0")
    }
}

expect fun createHttpClient(): HttpClient

fun friendlyNetworkError(error: Throwable): String {
    val msg = error.message.orEmpty()
    return when {
        msg.contains("timeout", ignoreCase = true) ||
            msg.contains("timed out", ignoreCase = true) ->
            "Server se jawab nahi mila. Internet check karke phir try karein."
        msg.contains("Unable to resolve", ignoreCase = true) ||
            msg.contains("UnknownHost", ignoreCase = true) ||
            msg.contains("offline", ignoreCase = true) ->
            "Internet connection nahi mil raha."
        else -> msg.ifBlank { "Network request failed" }
    }
}
