package com.mylaptopmarket.ui.components

data class ResolvedPlace(
    val state: String = "",
    val city: String = ""
)

object VendorPlace {
    fun resolve(state: String, location: String): ResolvedPlace {
        val written = "${state.trim()} ${location.trim()}".trim().lowercase()
        val city = matchCity(written)
        var resolvedState = matchState(written)
        if (resolvedState.isEmpty() && city.isNotEmpty()) {
            resolvedState = stateForCity(city)
        }
        val resolvedCity = if (city.isEmpty() && resolvedState == "Delhi") "Delhi" else city
        return ResolvedPlace(state = resolvedState, city = resolvedCity)
    }

    private fun matchCity(text: String): String {
        if (text.isEmpty()) return ""
        cities.forEach { (city, aliases) ->
            if (aliases.any { has(text, it) }) return city
        }
        return ""
    }

    private fun matchState(text: String): String {
        if (text.isEmpty()) return ""
        states.forEach { (state, aliases) ->
            if (aliases.any { has(text, it) }) return state
        }
        return ""
    }

    private fun has(text: String, alias: String): Boolean {
        val needle = alias.trim().lowercase()
        if (needle.isEmpty()) return false
        return Regex("(?<![a-z0-9])${Regex.escape(needle)}(?![a-z0-9])").containsMatchIn(text)
    }

    private fun stateForCity(city: String): String = when (city) {
        "Delhi" -> "Delhi"
        "Gurgaon", "Faridabad" -> "Haryana"
        "Noida", "Ghaziabad", "Lucknow" -> "Uttar Pradesh"
        "Jaipur" -> "Rajasthan"
        "Chennai" -> "Tamil Nadu"
        "Mumbai", "Pune" -> "Maharashtra"
        "Bangalore" -> "Karnataka"
        "Hyderabad" -> "Telangana"
        "Kolkata" -> "West Bengal"
        "Ahmedabad" -> "Gujarat"
        "Chandigarh" -> "Chandigarh"
        "Balasore", "Bhubaneswar" -> "Odisha"
        else -> ""
    }

    private val cities = listOf(
        "Delhi" to listOf(
            "new delhi", "east delhi", "south delhi", "north delhi", "west delhi", "delhi",
            "nehru place", "gaffar market", "karol bagh", "laxmi nagar", "shakarpur",
            "katwaria sarai", "hauz khas", "lajpat nagar", "chandni chowk", "kashmere gate",
            "wazirpur", "kirti nagar", "naraina", "mayapuri", "okhla", "bhogal", "kalkaji",
            "govindpuri", "munirka", "ber sarai", "jia sarai", "green park", "dwarka",
            "rohini", "janakpuri", "rajouri garden", "uttam nagar", "tilak nagar",
            "patparganj", "shahdara", "sadar bazar", "nabi karim", "moti nagar",
            "connaught place", "paharganj", "azadpur", "pitampura", "malviya nagar", "saket"
        ),
        "Gurgaon" to listOf("gurugram", "gurgaon"),
        "Faridabad" to listOf("faridabad"),
        "Noida" to listOf("noida"),
        "Ghaziabad" to listOf("ghaziabad"),
        "Jaipur" to listOf("jaipur"),
        "Chennai" to listOf("chennai", "madras"),
        "Mumbai" to listOf("mumbai", "bombay"),
        "Pune" to listOf("pune"),
        "Bangalore" to listOf("bengaluru", "bangalore"),
        "Hyderabad" to listOf("hyderabad"),
        "Kolkata" to listOf("kolkata", "calcutta"),
        "Ahmedabad" to listOf("ahmedabad"),
        "Lucknow" to listOf("lucknow"),
        "Chandigarh" to listOf("chandigarh"),
        "Balasore" to listOf("balasore", "baleswar", "baleshwar"),
        "Bhubaneswar" to listOf("bhubaneswar", "bhubaneshwar")
    )

    private val states = listOf(
        "Delhi" to listOf("new delhi", "nct", "delhi"),
        "Haryana" to listOf("haryana"),
        "Uttar Pradesh" to listOf("uttar pradesh"),
        "Rajasthan" to listOf("rajasthan"),
        "Tamil Nadu" to listOf("tamil nadu", "tamilnadu"),
        "Maharashtra" to listOf("maharashtra"),
        "Karnataka" to listOf("karnataka"),
        "Telangana" to listOf("telangana"),
        "West Bengal" to listOf("west bengal"),
        "Gujarat" to listOf("gujarat"),
        "Odisha" to listOf("odisha", "orissa"),
        "Chandigarh" to listOf("chandigarh"),
        "Punjab" to listOf("punjab"),
        "Bihar" to listOf("bihar"),
        "Madhya Pradesh" to listOf("madhya pradesh"),
        "Kerala" to listOf("kerala"),
        "Andhra Pradesh" to listOf("andhra pradesh")
    )
}
