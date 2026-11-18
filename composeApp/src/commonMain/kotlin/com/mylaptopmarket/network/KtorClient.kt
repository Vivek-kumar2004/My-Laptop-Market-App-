package com.mylaptopmarket.network

object KtorClient {
    // Vendor and admin apps both talk to the new API.
    private const val VENDOR_BASE_URL = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/"
    private const val ADMIN_BASE_URL = "https://asia-south1-mylaptopmarket-app.cloudfunctions.net/api/"

    // Older product photos and shop logos are still files on the live site.
    private const val MEDIA_BASE_URL = "https://mylaptopmarket.com/mylaptop/"

    val httpClient = createHttpClient()

    fun getBaseUrl() = VENDOR_BASE_URL

    fun getAdminBaseUrl() = ADMIN_BASE_URL

    fun getMediaBaseUrl() = MEDIA_BASE_URL
}
