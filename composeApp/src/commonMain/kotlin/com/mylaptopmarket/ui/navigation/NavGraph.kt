package com.mylaptopmarket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.network.VendorPushEvents
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mylaptopmarket.repository.ApiProductRepository
import com.mylaptopmarket.service.SpeechRecognitionService
import com.mylaptopmarket.service.GeminiChatbotService
import com.mylaptopmarket.ui.screens.*
import com.mylaptopmarket.util.RequestMicrophonePermission
import com.mylaptopmarket.util.ensureSpeechServiceInitialized
import com.mylaptopmarket.util.ensureVendorNotifications
import com.mylaptopmarket.viewmodel.*
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search?query={query}") { fun createRoute(query: String) = "search?query=$query" }
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object SellerVerification : Screen("seller_verification?vendorId={vendorId}") {
        fun createRoute(vendorId: Int? = null) =
            if (vendorId != null) "seller_verification?vendorId=$vendorId" else "seller_verification"
    }
    data object Messages : Screen("messages")
    data object Chat : Screen("chat/{contactId}/{contactName}?productId={productId}") {
        fun createRoute(contactId: Int, contactName: String, productId: String? = null): String {
            val base = "chat/$contactId/${contactName.replace("/", "-")}"
            return if (!productId.isNullOrBlank()) "$base?productId=$productId" else base
        }
    }
    data object AddProduct : Screen("add_product")
    data object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    data object MyListings : Screen("my_listings")
    data object Requirements : Screen("requirements")
    data object AddRequirement : Screen("add_requirement")
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    data object VendorProducts : Screen("vendor_products/{vendorId}/{vendorName}") {
        fun createRoute(vendorId: String, vendorName: String) =
            "vendor_products/$vendorId/${vendorName.replace("/", "-")}"
    }
    data object VoiceChatbot : Screen("voice_chatbot")
    data object Profile : Screen("profile")
    data object FavoriteDealers : Screen("favorite_dealers")
    data object StockSheet : Screen("stock_sheet/{productId}") {
        fun createRoute(productId: String) = "stock_sheet/$productId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    val repository = remember { ApiProductRepository() }
    val authRepository = remember { com.mylaptopmarket.repository.ApiAuthRepository() }
    val msgRepository = remember { com.mylaptopmarket.repository.ApiMessageRepository() }
    val requirementRepository = remember { com.mylaptopmarket.repository.ApiRequirementRepository() }
    val msgViewModel = remember { MessagesViewModel(msgRepository) }
    val conversations by msgViewModel.conversations.collectAsState()
    val unreadMessages = conversations.sumOf { it.unread_count }
    val session by SessionManager.currentSession.collectAsState()
    val pushToken by VendorPushEvents.token.collectAsState()
    val pushOpen by VendorPushEvents.open.collectAsState()

    LaunchedEffect(session?.id) {
        ensureVendorNotifications(session?.id)
    }

    LaunchedEffect(session?.id) {
        val vendorId = session?.id ?: return@LaunchedEffect
        while (true) {
            msgViewModel.refreshUnread()
            delay(15_000)
            if (SessionManager.currentSession.value?.id != vendorId) break
        }
    }

    LaunchedEffect(session?.id, pushToken?.token, pushToken?.environment) {
        val token = pushToken ?: return@LaunchedEffect
        val current = session
        if (current == null) {
            authRepository.clearPushToken(token.token)
        } else {
            authRepository.savePushToken(current.id, token.token, token.environment)
        }
    }

    LaunchedEffect(session?.id) {
        val vendorId = session?.id ?: return@LaunchedEffect
        authRepository.getVendorStatus(vendorId).onSuccess { loaded ->
            SessionManager.update { current ->
                if (current.id != vendorId) current else current.copy(adminWarning = loaded.adminWarning)
            }
        }
    }

    LaunchedEffect(pushOpen, session?.id) {
        val event = pushOpen ?: return@LaunchedEffect
        if (session == null) return@LaunchedEffect
        when (event.type) {
            "requirement" -> navController.navigate(Screen.Requirements.route) {
                launchSingleTop = true
            }
            "product" -> if (event.targetId.isNotBlank()) {
                navController.navigate(Screen.ProductDetail.createRoute(event.targetId))
            }
            "message" -> event.targetId.toIntOrNull()?.let { contactId ->
                navController.navigate(
                    Screen.Chat.createRoute(contactId, event.targetName.ifBlank { "Vendor" })
                )
            }
            "warning" -> {
                val vendorId = session?.id ?: return@LaunchedEffect
                authRepository.getVendorStatus(vendorId).onSuccess { loaded ->
                    SessionManager.update { current ->
                        if (current.id != vendorId) current else current.copy(adminWarning = loaded.adminWarning)
                    }
                }
            }
        }
        VendorPushEvents.consumeOpen()
    }

    fun navigateMainTab(tab: com.mylaptopmarket.ui.components.MainTab) {
        val session = com.mylaptopmarket.network.SessionManager.currentSession.value
        when (tab) {
            com.mylaptopmarket.ui.components.MainTab.Home -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            com.mylaptopmarket.ui.components.MainTab.Listings -> {
                if (session != null) {
                    navController.navigate(Screen.MyListings.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else {
                    navController.navigate(Screen.Login.route)
                }
            }
            com.mylaptopmarket.ui.components.MainTab.Messages -> {
                if (session != null) {
                    navController.navigate(Screen.Messages.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else {
                    navController.navigate(Screen.Login.route)
                }
            }
            com.mylaptopmarket.ui.components.MainTab.Profile -> {
                if (session != null) {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else {
                    navController.navigate(Screen.Login.route)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel = remember { HomeViewModel(repository) }
            HomeScreen(
                viewModel = viewModel,
                onSearchClick = { navController.navigate("search") },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onProviderClick = { /* Can navigate to provider detail if needed */ },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onMessagesClick = { navController.navigate(Screen.Messages.route) },
                onAddProductClick = { navController.navigate(Screen.AddProduct.route) },
                onMyListingsClick = { navController.navigate(Screen.MyListings.route) },
                onRequirementsClick = { navController.navigate(Screen.Requirements.route) },
                onSellerVerificationClick = { navController.navigate(Screen.SellerVerification.createRoute()) },
                onProfileClick = {
                    val loggedIn = com.mylaptopmarket.network.SessionManager.currentSession.value
                    if (loggedIn != null) {
                        navController.navigate(Screen.Profile.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onCategoryClick = { category -> navController.navigate(Screen.Search.createRoute(category)) },
                onVoiceChatbotClick = { navController.navigate(Screen.VoiceChatbot.route) },
                onWishlistClick = { navController.navigate(Screen.FavoriteDealers.route) },
                onBuyClick = { navController.navigate("search") },
                messagesBadge = unreadMessages,
                onMainTabSelected = { navigateMainTab(it) }
            )
        }

        composable(Screen.Login.route) {
            val viewModel = remember { AuthViewModel(authRepository) }
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { navController.popBackStack() },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                onPendingVerification = { vendorId ->
                    navController.navigate(Screen.SellerVerification.createRoute(vendorId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            val viewModel = remember { AuthViewModel(authRepository) }
            ForgotPasswordScreen(
                viewModel = viewModel,
                onResetDone = {
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Register.route) {
            val viewModel = remember { AuthViewModel(authRepository) }
            RegisterScreen(
                viewModel = viewModel,
                onRegistered = { vendorId ->
                    navController.navigate(Screen.SellerVerification.createRoute(vendorId)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SellerVerification.route,
            arguments = listOf(
                navArgument("vendorId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val vendorId = backStackEntry.arguments?.getString("vendorId")?.toIntOrNull()
            val viewModel = remember { AuthViewModel(authRepository) }
            SellerVerificationScreen(
                vendorId = vendorId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
            if (mySession == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
                return@composable
            }
            val viewModel = remember { ProfileViewModel(authRepository) }
            ProfileScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onMainTabSelected = { navigateMainTab(it) }
            )
        }

        composable(Screen.Messages.route) {
            ConversationsScreen(
                viewModel = msgViewModel,
                onChatClick = { contactId, contactName -> 
                    navController.navigate(Screen.Chat.createRoute(contactId, contactName))
                },
                onBackClick = { navController.popBackStack() },
                onMainTabSelected = { navigateMainTab(it) }
            )
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("contactId") { type = NavType.IntType },
                navArgument("contactName") { type = NavType.StringType },
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: return@composable
            val contactName = backStackEntry.arguments?.getString("contactName") ?: "Vendor"
            val productId = backStackEntry.arguments?.getString("productId")
            ChatScreen(
                contactId = contactId,
                contactName = contactName,
                productId = productId,
                productRepository = repository,
                viewModel = msgViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddProduct.route) {
            val viewModel = remember { AddProductViewModel(repository) }
            AddProductScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId").orEmpty()
            val viewModel = remember(productId) { AddProductViewModel(repository) }
            AddProductScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                productId = productId
            )
        }

        composable(Screen.MyListings.route) {
            val viewModel = remember { MyListingsViewModel(repository) }
            MyListingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.EditProduct.createRoute(productId))
                },
                onMainTabSelected = { navigateMainTab(it) }
            )
        }

        composable(Screen.FavoriteDealers.route) {
            FavoriteDealersScreen(
                onBackClick = { navController.popBackStack() },
                onDealerClick = { vendorId, vendorName ->
                    val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
                    if (mySession != null) {
                        navController.navigate(Screen.VendorProducts.createRoute(vendorId, vendorName))
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onMainTabSelected = { navigateMainTab(it) }
            )
        }

        composable(Screen.Requirements.route) {
            val viewModel = remember { RequirementsViewModel(requirementRepository) }
            LaunchedEffect(Unit) { viewModel.refresh() }
            RequirementsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAddClick = { navController.navigate(Screen.AddRequirement.route) },
                onChatWithVendor = { vendorId, vendorName ->
                    val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
                    if (mySession != null) {
                        navController.navigate(Screen.Chat.createRoute(vendorId, vendorName))
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onOpenVendorProducts = { vendorId, vendorName ->
                    val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
                    if (mySession != null) {
                        navController.navigate(Screen.VendorProducts.createRoute(vendorId.toString(), vendorName))
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                }
            )
        }

        composable(Screen.AddRequirement.route) {
            val viewModel = remember { AddRequirementViewModel(requirementRepository) }
            AddRequirementScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.VoiceChatbot.route) {
            val speechService = remember { SpeechRecognitionService() }
            val aiService = remember { GeminiChatbotService(apiKey = com.mylaptopmarket.config.AIConfig.GEMINI_API_KEY) }
            val viewModel = remember { VoiceChatbotViewModel(speechService, aiService, repository) }
            RequestMicrophonePermission()

            // Initialize speech service on first composition
            LaunchedEffect(Unit) {
                try {
                    ensureSpeechServiceInitialized(speechService)
                } catch (e: Exception) {
                    println("Failed to initialize speech service: ${e.message}")
                }
            }
            
            VoiceChatbotScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Search.route,
            arguments = listOf(navArgument("query") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")
            val viewModel = remember { SearchViewModel(repository) }
            androidx.compose.runtime.LaunchedEffect(query) {
                if (!query.isNullOrEmpty()) {
                    viewModel.onSearchQueryChange(query)
                }
            }
            SearchScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onProviderClick = { providerId, providerName ->
                    val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
                    if (mySession != null) {
                        navController.navigate(Screen.VendorProducts.createRoute(providerId, providerName))
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            val viewModel = remember { ProductDetailViewModel(repository) }
            ProductDetailScreen(
                productId = productId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate("search") },
                onChatWithSellerClick = { vendorId, vendorName, chatProductId ->
                    val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
                    if (mySession != null) {
                        navController.navigate(
                            Screen.Chat.createRoute(
                                vendorId.toIntOrNull() ?: 0,
                                vendorName,
                                chatProductId
                            )
                        )
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onSellerClick = { vendorId, vendorName ->
                    val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
                    if (mySession != null) {
                        navController.navigate(Screen.VendorProducts.createRoute(vendorId, vendorName))
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onViewStockSheet = {
                    navController.navigate(Screen.StockSheet.createRoute(productId))
                }
            )
        }

        composable(
            route = Screen.StockSheet.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sheetProductId = backStackEntry.arguments?.getString("productId") ?: return@composable
            StockSheetScreen(
                productId = sheetProductId,
                repository = repository,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(
            route = Screen.VendorProducts.route,
            arguments = listOf(
                navArgument("vendorId") { type = NavType.StringType },
                navArgument("vendorName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mySession = com.mylaptopmarket.network.SessionManager.currentSession.value
            if (mySession == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
                return@composable
            }
            val vendorId = backStackEntry.arguments?.getString("vendorId") ?: return@composable
            val vendorName = backStackEntry.arguments?.getString("vendorName") ?: "Vendor"
            val viewModel = remember(vendorId) { VendorProductsViewModel(repository, vendorId) }
            VendorProductsScreen(
                viewModel = viewModel,
                vendorId = vendorId,
                vendorName = vendorName,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }
    }
    val trustSession by SessionManager.currentSession.collectAsState()
    val showTrust by SessionManager.shopTrustPrompt.collectAsState()
    val activeTrustSession = trustSession
    val warning = activeTrustSession?.adminWarning?.trim().orEmpty()
    if (activeTrustSession != null && warning.isNotEmpty()) {
        VendorWarningDialog(
            vendorId = activeTrustSession.id,
            message = warning,
            repository = authRepository
        )
    } else if (activeTrustSession != null && showTrust) {
        ShopTrustPopup(
            session = activeTrustSession,
            repository = authRepository,
            onDismiss = { SessionManager.dismissShopTrustPrompt() }
        )
    }
}
