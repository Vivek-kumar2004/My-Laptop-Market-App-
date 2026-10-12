package com.mylaptopmarket.util

import com.mylaptopmarket.model.Product

object ProductMessageContext {
    private const val SITE = "https://mylaptopmarket.com"

    fun draft(product: Product): String {
        val priceBit = formatInr(product.price)?.let { " · $it" }.orEmpty()
        return "Regarding product: ${product.name}\n" +
            "Listing #${product.id}$priceBit\n" +
            "$SITE/product.html?id=${product.id}"
    }

    fun containsProduct(text: String, productId: String): Boolean {
        return text.contains("product.html?id=$productId")
    }

    private fun formatInr(raw: String?): String? {
        val digits = raw?.replace(",", "")?.replace("₹", "")?.trim().orEmpty()
        val amount = digits.toLongOrNull() ?: digits.toDoubleOrNull()?.toLong() ?: return null
        if (amount <= 0) return null
        val source = amount.toString()
        val parts = ArrayList<String>()
        var index = source.length
        while (index > 0) {
            val start = maxOf(0, index - 3)
            parts.add(0, source.substring(start, index))
            index = start
        }
        return "₹" + parts.joinToString(",")
    }
}
