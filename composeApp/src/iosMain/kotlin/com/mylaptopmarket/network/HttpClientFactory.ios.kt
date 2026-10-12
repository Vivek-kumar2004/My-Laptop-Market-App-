package com.mylaptopmarket.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    installAppPlugins(withKtorTimeout = false)
}
