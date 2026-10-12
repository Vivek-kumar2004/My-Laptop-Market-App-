package com.mylaptopmarket.service

import com.mylaptopmarket.config.AIConfig
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

data class ChatbotReply(
    val text: String,
    val fromAi: Boolean
)

interface ChatbotAIService {
    suspend fun processQuery(
        query: String,
        language: String,
        productsContext: List<Product>
    ): ChatbotReply

    suspend fun checkConnection(): Boolean
}

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart> = emptyList()
)

@Serializable
data class GeminiPart(
    val text: String = ""
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent = GeminiContent()
)

class GeminiChatbotService(private val apiKey: String = "") : ChatbotAIService {

    private val geminiUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/${AIConfig.GEMINI_MODEL}:generateContent"

    override suspend fun processQuery(
        query: String,
        language: String,
        productsContext: List<Product>
    ): ChatbotReply {
        if (apiKey.isBlank()) {
            println("No Gemini API key found. Using offline replies.")
            return ChatbotReply(
                text = generateMockResponse(query, language, productsContext),
                fromAi = false
            )
        }

        return try {
            val prompt = buildPrompt(query, language, buildProductContext(productsContext))
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )

            val httpResponse = postToGemini(request)

            if (!httpResponse.status.isSuccess()) {
                val errorBody = httpResponse.bodyAsText()
                println("Gemini API HTTP ${httpResponse.status}: $errorBody")
                return ChatbotReply(
                    text = "Couldn't reach the floor (HTTP ${httpResponse.status.value}). $errorBody",
                    fromAi = false
                )
            }

            val response: GeminiResponse = httpResponse.body()
            val aiText = response.candidates.firstOrNull()?.content?.parts
                ?.firstOrNull { it.text.isNotBlank() }
                ?.text

            if (aiText.isNullOrBlank()) {
                ChatbotReply(
                    text = "No reply came back. Try asking again.",
                    fromAi = false
                )
            } else {
                println("Gemini AI reply received (${aiText.length} chars)")
                ChatbotReply(text = aiText.trim(), fromAi = true)
            }
        } catch (e: Exception) {
            println("Gemini API Error: ${e.message}")
            e.printStackTrace()
            val timedOut = e.message?.contains("timeout", ignoreCase = true) == true
            val message = if (timedOut) {
                "The floor timed out. Please ask again."
            } else {
                "Couldn't reach the floor: ${e.message}"
            }
            ChatbotReply(text = message, fromAi = false)
        }
    }

    private suspend fun postToGemini(request: GeminiRequest): HttpResponse {
        suspend fun send(): HttpResponse {
            return KtorClient.httpClient.post(geminiUrl) {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 60_000
                    connectTimeoutMillis = 20_000
                }
                setBody(request)
            }
        }

        return try {
            send()
        } catch (e: Exception) {
            if (e.message?.contains("timeout", ignoreCase = true) == true) {
                println("Gemini timed out, retrying once...")
                send()
            } else {
                throw e
            }
        }
    }

    override suspend fun checkConnection(): Boolean {
        if (apiKey.isBlank()) return false
        return try {
            val response = KtorClient.httpClient.get(
                "https://generativelanguage.googleapis.com/v1beta/models/${AIConfig.GEMINI_MODEL}"
            ) {
                header("x-goog-api-key", apiKey)
                timeout {
                    requestTimeoutMillis = 8_000
                    connectTimeoutMillis = 8_000
                    socketTimeoutMillis = 8_000
                }
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("Gemini status check failed: ${e.message}")
            false
        }
    }

    private fun buildProductContext(products: List<Product>): String {
        val categories = products.mapNotNull { it.category }.distinct()
        val availableProducts = products.filter { it.stockStatus.name == "AVAILABLE" }
        val priceRange = products.mapNotNull { it.price?.toIntOrNull() }
            .let { prices ->
                if (prices.isNotEmpty()) {
                    "₹${prices.minOrNull()} - ₹${prices.maxOrNull()}"
                } else "Variable"
            }
        
        return """
            Total Products: ${products.size}
            Available Products: ${availableProducts.size}
            Categories: ${categories.joinToString(", ")}
            Price Range: $priceRange
            Popular Vendors: ${products.map { it.providerName }.distinct().take(5).joinToString(", ")}
        """.trimIndent()
    }
    
    private fun buildPrompt(query: String, language: String, context: String): String {
        return """
            You are a helpful B2B laptop and hardware marketplace assistant for vendors.
            
            Context about available products:
            $context
            
            User Query: $query
            
            Instructions:
            - Reply in the same language the user used (Hindi, English, or Hinglish).
            - Be concise and helpful.
            - Never use markdown. Do not use asterisks, hashes, or backticks.
            - Write vendor names and important words in plain text only.
            - If asked about products, mention available categories and vendors.
            - If asked about prices, provide the range.
            - Be friendly and professional.
            
            Provide a helpful response:
        """.trimIndent()
    }
    
    private fun generateMockResponse(query: String, language: String, products: List<Product>): String {
        val queryLower = query.lowercase()
        val availableProducts = products.filter { it.stockStatus.name == "AVAILABLE" }
        val totalVendors = products.map { it.providerName }.distinct().size
        
        // Greeting detection
        if (queryLower.matches(Regex(".*\\b(hi|hello|hey|namaste|नमस्ते|haan|yes)\\b.*"))) {
            return when (language) {
                "hi" -> "नमस्ते! मैं आपका B2B प्लेटफ़ॉर्म असिस्टेंट हूं। ${totalVendors} वेंडर्स हमारे साथ हैं और ${availableProducts.size} प्रोडक्ट्स लिस्टेड हैं। आपको क्या जानना है - प्रोडक्ट लिस्टिंग, प्राइसिंग, या मार्केट trends?"
                "hinglish" -> "Hello! Main aapka B2B platform assistant hun. ${totalVendors} vendors active hain aur ${availableProducts.size} products listed hain. Aapko kya jaanna hai - product listing, pricing, ya market insights?"
                else -> "Hello! I'm your B2B platform assistant. We have ${totalVendors} active vendors with ${availableProducts.size} products listed. How can I help you today - listing products, checking market prices, or platform navigation?"
            }
        }
        
        // How to list/add products
        if (queryLower.contains("list") || queryLower.contains("add") || queryLower.contains("upload") ||
            queryLower.contains("product") && (queryLower.contains("kaise") || queryLower.contains("how"))) {
            
            return when (language) {
                "hi" -> "प्रोडक्ट लिस्ट करना होम स्क्रीन से होता है। वहाँ Add Product खोलो, फोटो, नाम, कैटेगरी, प्राइस और स्टॉक डालो।"
                "hinglish" -> "Product listing home screen se hoti hai. Wahan Add Product kholo, photo, name, category, price aur stock daalo."
                else -> "Product listing is on the home screen. Open Add Product there, then add photos, name, category, price, and stock."
            }
        }
        
        // Pricing queries
        if (queryLower.contains("price") || queryLower.contains("pricing") || 
            queryLower.contains("kitne") || queryLower.contains("कीमत") ||
            queryLower.contains("rate")) {
            
            val categoryPrices = products.groupBy { it.category }
                .mapValues { (_, prods) ->
                    prods.mapNotNull { it.price?.toIntOrNull() }.average().toInt()
                }
            
            val topCategory = categoryPrices.entries.firstOrNull()
            
            return when (language) {
                "hi" -> "मार्केट में अभी ये प्राइसिंग trends हैं: ${topCategory?.let { "${it.key} का average ₹${it.value}" } ?: "varies by category"}। आपका कौन सा प्रोडक्ट है? मैं competitive pricing suggest करूंगा!"
                "hinglish" -> "Market mein abhi yeh pricing hai: ${topCategory?.let { "${it.key} average ₹${it.value}" } ?: "category ke hisaab se"}। Aapka product kaunsa hai? Main competitive price bataunga!"
                else -> "Current market pricing: ${topCategory?.let { "${it.key} averages ₹${it.value}" } ?: "varies by category"}. Tell me your product category and I'll suggest competitive pricing to help you sell faster!"
            }
        }
        
        // Stock/inventory queries
        if (queryLower.contains("stock") || queryLower.contains("inventory") ||
            queryLower.contains("स्टॉक") || queryLower.contains("माल")) {
            
            val inStock = products.count { it.stockStatus.name == "AVAILABLE" }
            val outOfStock = products.count { it.stockStatus.name == "OUT_OF_STOCK" }
            
            return when (language) {
                "hi" -> "प्लेटफ़ॉर्म पर अभी $inStock प्रोडक्ट्स in-stock हैं और $outOfStock out-of-stock। अपनी inventory update करना चाहते हैं? 'My Listings' में जाकर stock status change कर सकते हैं।"
                "hinglish" -> "Platform par abhi $inStock products in-stock hain, $outOfStock out-of-stock. Apni inventory update karni hai? 'My Listings' mein jao aur stock change karo."
                else -> "Currently $inStock products are in-stock on the platform and $outOfStock are out-of-stock. Need to update your inventory? Go to 'My Listings' and update stock status anytime."
            }
        }
        
        // Competition/market analysis
        if (queryLower.contains("competition") || queryLower.contains("market") ||
            queryLower.contains("competitor") || queryLower.contains("other seller") ||
            queryLower.contains("प्रतिस्पर्धा")) {
            
            val categories = products.groupBy { it.category }
            val mostCompetitive = categories.maxByOrNull { it.value.size }
            
            return when (language) {
                "hi" -> "अभी ${mostCompetitive?.key ?: "Laptops"} में सबसे ज्यादा competition है (${mostCompetitive?.value?.size ?: 0} प्रोडक्ट्स)। आप unique products या niche categories में focus करें - ज्यादा margin मिलेगा!"
                "hinglish" -> "${mostCompetitive?.key ?: "Laptops"} mein abhi sabse zyada competition hai (${mostCompetitive?.value?.size ?: 0} products). Aap unique products ya niche categories mein focus karo - better margins milenge!"
                else -> "${mostCompetitive?.key ?: "Laptops"} is the most competitive category right now with ${mostCompetitive?.value?.size ?: 0} products. Consider focusing on unique products or niche categories for better margins!"
            }
        }
        
        // Best selling/trending
        if (queryLower.contains("best") || queryLower.contains("trend") ||
            queryLower.contains("popular") || queryLower.contains("ट्रेंडिंग") ||
            queryLower.contains("बिकता")) {
            
            val categoryCount = products.groupBy { it.category }
                .mapValues { it.value.size }
                .entries.sortedByDescending { it.value }
                .take(3)
            
            return when (language) {
                "hi" -> "अभी ये categories सबसे ज्यादा demand में हैं: ${categoryCount.joinToString(", ") { it.key ?: "Other" }}। इनमें listing करोगे तो तेजी से बिकेगा!"
                "hinglish" -> "Abhi yeh categories high demand mein hain: ${categoryCount.joinToString(", ") { it.key ?: "Other" }}. In mein listing karo toh fast sale hoga!"
                else -> "These categories are trending right now: ${categoryCount.joinToString(", ") { it.key ?: "Other" }}. List products in these categories for faster sales!"
            }
        }
        
        // Buyer/retailer queries
        if (queryLower.contains("buyer") || queryLower.contains("customer") ||
            queryLower.contains("retailer") || queryLower.contains("खरीददार") ||
            queryLower.contains("ग्राहक")) {
            
            return when (language) {
                "hi" -> "हमारे प्लेटफ़ॉर्म पर रिटेलर्स और रीसेलर्स आपके प्रोडक्ट्स देख सकते हैं। अच्छी quality फोटो, detailed description और competitive pricing रखिए - ज्यादा inquiries आएंगी!"
                "hinglish" -> "Platform par retailers aur resellers aapke products dekh sakte hain. Good quality photos, proper description aur competitive price rakho - zyada inquiries aayengi!"
                else -> "Retailers and resellers browse our platform daily. Keep high-quality photos, detailed descriptions, and competitive pricing to attract more buyer inquiries!"
            }
        }
        
        // Verification queries
        if (queryLower.contains("verif") || queryLower.contains("trust") ||
            queryLower.contains("badge") || queryLower.contains("सत्यापित")) {
            
            return when (language) {
                "hi" -> "Verified badge मिलने के लिए आपको बस बिजनेस डॉक्यूमेंट्स submit करने हैं। Verified sellers को ज्यादा inquiries मिलती हैं और buyers का trust बढ़ता है। Apply करें?"
                "hinglish" -> "Verified badge ke liye bas business documents submit karo. Verified sellers ko zyada inquiries milti hain aur trust badge se credibility badhti hai. Apply karna hai?"
                else -> "To get the Verified badge, simply submit your business documents. Verified sellers get 3x more buyer inquiries and build instant trust. Ready to apply?"
            }
        }
        
        // Messaging/contact queries
        if (queryLower.contains("message") || queryLower.contains("contact") ||
            queryLower.contains("chat") || queryLower.contains("संपर्क") ||
            queryLower.contains("बात")) {
            
            return when (language) {
                "hi" -> "आप directly buyers से chat कर सकते हैं! जब कोई आपके प्रोडक्ट में interest दिखाता है, 'Messages' section में notification आएगी। तुरंत reply करें - fast response से deals close होती हैं!"
                "hinglish" -> "Aap directly buyers se chat kar sakte ho! Jab koi interested hota hai, 'Messages' mein notification aati hai. Turant reply karo - quick response se deals close hoti hain!"
                else -> "You can chat directly with interested buyers! When someone shows interest in your products, you'll get notified in the 'Messages' section. Quick responses lead to faster deals!"
            }
        }
        
        // Category suggestions
        if (queryLower.contains("category") || queryLower.contains("categories") ||
            queryLower.contains("श्रेणी")) {
            
            val categories = listOf("Laptops", "Desktop", "TFT/Screen", "Server", "Storage", "Accessories", "Graphics Card")
            
            return when (language) {
                "hi" -> "हम ${categories.size} categories support करते हैं: ${categories.joinToString(", ")}। आपके पास कौन सा hardware है? सही category में list करोगे तो सही buyers तक पहुंचेगा!"
                "hinglish" -> "Hum ${categories.size} categories support karte hain: ${categories.joinToString(", ")}. Aapke paas kaunsa hardware hai? Right category mein list karo toh right buyers tak pahunchega!"
                else -> "We support ${categories.size} categories: ${categories.joinToString(", ")}. What hardware do you have? Listing in the right category connects you with the right buyers!"
            }
        }
        
        // Help/platform navigation
        if (queryLower.contains("help") || queryLower.contains("how") ||
            queryLower.contains("kaise") || queryLower.contains("मदद") ||
            queryLower.contains("guide")) {
            
            return when (language) {
                "hi" -> "मैं आपकी पूरी मदद करूंगा! आप पूछ सकते हैं: 'प्रोडक्ट कैसे add करें', 'मार्केट प्राइसिंग क्या है', 'stock कैसे update करें', 'buyers से कैसे contact करें' - कुछ भी!"
                "hinglish" -> "Main puri help karunga! Pooch sakte ho: 'product kaise add kare', 'market pricing kya hai', 'stock update kaise kare', 'buyers se kaise baat kare' - kuch bhi!"
                else -> "I'm here to help you succeed as a vendor! Ask me anything: 'How to list products', 'What's market pricing', 'How to update stock', 'How to contact buyers' - I've got you covered!"
            }
        }
        
        // Smart default for vendors
        return when (language) {
            "hi" -> "नमस्ते! मैं आपका B2B असिस्टेंट हूं। ${totalVendors} vendors हमारे साथ हैं। मैं आपकी मदद कर सकता हूं: प्रोडक्ट listing, pricing strategy, inventory management, buyer connections। क्या जानना चाहते हैं?"
            "hinglish" -> "Hello! Main aapka B2B assistant hun. ${totalVendors} vendors already platform par hain. Main help kar sakta hun: product listing, pricing tips, inventory management, buyer connections. Kya jaanna hai?"
            else -> "Hi! I'm your B2B platform assistant. We have ${totalVendors} vendors already selling successfully. I can help with: product listings, pricing strategies, inventory management, and connecting with buyers. What would you like to know?"
        }
    }
}
