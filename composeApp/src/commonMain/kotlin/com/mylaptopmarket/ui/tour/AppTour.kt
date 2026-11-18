package com.mylaptopmarket.ui.tour

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.mylaptopmarket.network.VendorSession
import com.mylaptopmarket.util.markVendorTutorialSeen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

enum class TourPlace {
    Home,
    Search,
    Sell,
    Requirements,
    Favorites,
    Listings,
    Messages,
    Profile,
    Ask
}

data class TourCue(
    val place: TourPlace,
    val targetId: String,
    val title: String,
    val body: String,
    val wide: Boolean = false
) {
    fun resolvedTarget(session: VendorSession?): String {
        val loggedOut = session == null
        val unverified = session != null && !session.isVerified
        return when (place) {
            TourPlace.Sell -> when {
                loggedOut -> "page-login"
                unverified -> "page-verify"
                else -> targetId
            }
            TourPlace.Requirements,
            TourPlace.Listings,
            TourPlace.Messages,
            TourPlace.Profile -> if (loggedOut) "page-login" else targetId
            else -> targetId
        }
    }
}

object AppTour {
    val cues = listOf(
        TourCue(
            TourPlace.Home,
            "menu",
            "Menu",
            "The menu on the left opens every screen. Tap Next and the app will open each part for you."
        ),
        TourCue(TourPlace.Home, "search", "Search", "Search finds stock by name or category."),
        TourCue(
            TourPlace.Search,
            "page-search",
            "Search",
            "Type a name here. Products and vendors show up below.",
            wide = true
        ),
        TourCue(TourPlace.Home, "buy", "Buy", "Buy opens the deals screen."),
        TourCue(TourPlace.Home, "sell", "Sell", "Sell is where you list your own stock."),
        TourCue(
            TourPlace.Sell,
            "page-sell",
            "Your stock",
            "Add a photo, price, and details. The listing goes live after verification.",
            wide = true
        ),
        TourCue(TourPlace.Home, "requirement", "Requirement", "Post what you need from here."),
        TourCue(
            TourPlace.Requirements,
            "page-requirement",
            "Requirements",
            "Other dealers see this and can message you.",
            wide = true
        ),
        TourCue(TourPlace.Home, "favorites", "Favorite Dealers", "Save vendors you want to buy from again."),
        TourCue(
            TourPlace.Favorites,
            "page-favorites",
            "Saved vendors",
            "Open a saved vendor to see their stock.",
            wide = true
        ),
        TourCue(
            TourPlace.Home,
            "products",
            "Recent Products",
            "Dealer stock is listed here. Tap a card for the photo and price."
        ),
        TourCue(
            TourPlace.Home,
            "open-market",
            "Open Market",
            "These listings are not linked to a vendor account."
        ),
        TourCue(TourPlace.Home, "tab-listings", "Listings", "Your own stock lives in Listings."),
        TourCue(
            TourPlace.Listings,
            "page-listings",
            "My Listings",
            "Edit your listings from this screen.",
            wide = true
        ),
        TourCue(TourPlace.Home, "tab-messages", "Messages", "Talk to dealers from Messages."),
        TourCue(
            TourPlace.Messages,
            "page-messages",
            "Messages",
            "Tap a dealer to open the chat.",
            wide = true
        ),
        TourCue(TourPlace.Home, "tab-profile", "Profile", "Shop name, logo, and phone are in Profile."),
        TourCue(
            TourPlace.Profile,
            "page-profile",
            "Profile",
            "Update your shop details here.",
            wide = true
        ),
        TourCue(TourPlace.Home, "ask", "Ask Me", "Ask Me lets you ask a question by voice."),
        TourCue(
            TourPlace.Ask,
            "page-ask",
            "Floor Desk",
            "Tap the mic or type a question. The app answers.",
            wide = true
        )
    )

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _index = MutableStateFlow(0)
    val index: StateFlow<Int> = _index.asStateFlow()

    val bounds = mutableStateMapOf<String, Rect>()

    var navigator: ((TourPlace) -> Unit)? = null
    var scrollHome: (suspend (String) -> Unit)? = null

    fun start() {
        if (_running.value) return
        _index.value = 0
        _running.value = true
        markVendorTutorialSeen()
    }

    fun finish() {
        if (!_running.value) return
        _running.value = false
        markVendorTutorialSeen()
        navigator?.invoke(TourPlace.Home)
    }

    fun next() {
        if (!_running.value) return
        if (_index.value >= cues.lastIndex) finish() else _index.value += 1
    }

    fun updateBound(id: String, rect: Rect) {
        val current = bounds[id]
        if (current != null &&
            kotlin.math.abs(current.left - rect.left) < 0.5f &&
            kotlin.math.abs(current.top - rect.top) < 0.5f &&
            kotlin.math.abs(current.right - rect.right) < 0.5f &&
            kotlin.math.abs(current.bottom - rect.bottom) < 0.5f
        ) {
            return
        }
        bounds[id] = rect
    }

    fun clearBound(id: String) {
        bounds.remove(id)
    }

    suspend fun waitFor(targetId: String, timeoutMs: Long = 1800): Boolean {
        return withTimeoutOrNull(timeoutMs) {
            snapshotFlow { bounds[targetId] }.first { rect ->
                rect != null && rect.width > 8f && rect.height > 8f
            }
        } != null
    }
}

fun Modifier.tourTarget(id: String): Modifier = composed {
    val currentId by rememberUpdatedState(id)
    DisposableEffect(currentId) {
        onDispose { AppTour.clearBound(currentId) }
    }
    onGloballyPositioned { coords ->
        if (!coords.isAttached) return@onGloballyPositioned
        AppTour.updateBound(currentId, coords.boundsInRoot())
    }
}
