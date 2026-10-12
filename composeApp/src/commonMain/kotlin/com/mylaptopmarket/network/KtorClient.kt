package com.mylaptopmarket.network

object KtorClient {
    // Live website — phone ko internet chahiye, XAMPP nahi
    private const val BASE_URL = "https://mylaptopmarket.com/mylaptop/"

    val httpClient = createHttpClient()

    fun getBaseUrl() = BASE_URL
}
