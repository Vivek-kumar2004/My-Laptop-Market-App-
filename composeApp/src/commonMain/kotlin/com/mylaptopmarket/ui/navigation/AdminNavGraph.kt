package com.mylaptopmarket.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.mylaptopmarket.network.AdminPushEvents
import com.mylaptopmarket.network.AdminSessionManager
import com.mylaptopmarket.repository.ApiAdminRepository
import com.mylaptopmarket.ui.screens.AdminChatDetailScreen
import com.mylaptopmarket.ui.screens.AdminChatsScreen
import com.mylaptopmarket.ui.screens.AdminDashboardScreen
import com.mylaptopmarket.ui.screens.AdminInquiriesScreen
import com.mylaptopmarket.ui.screens.AdminInquiryDetailScreen
import com.mylaptopmarket.ui.screens.AdminInvoiceDetailScreen
import com.mylaptopmarket.ui.screens.AdminInvoicesScreen
import com.mylaptopmarket.ui.screens.AdminLoginScreen
import com.mylaptopmarket.ui.screens.AdminProductsScreen
import com.mylaptopmarket.ui.screens.AdminRequirementDetailScreen
import com.mylaptopmarket.ui.screens.AdminRequirementsScreen
import com.mylaptopmarket.ui.screens.AdminVendorDetailScreen
import com.mylaptopmarket.ui.screens.AdminVendorFormScreen
import com.mylaptopmarket.ui.screens.AdminVendorsScreen
import com.mylaptopmarket.viewmodel.AdminAuthViewModel
import com.mylaptopmarket.viewmodel.AdminChatsViewModel
import com.mylaptopmarket.viewmodel.AdminDashboardViewModel
import com.mylaptopmarket.viewmodel.AdminInquiriesViewModel
import com.mylaptopmarket.viewmodel.AdminInvoicesViewModel
import com.mylaptopmarket.viewmodel.AdminProductsViewModel
import com.mylaptopmarket.viewmodel.AdminRequirementsViewModel
import com.mylaptopmarket.viewmodel.AdminVendorDetailViewModel
import com.mylaptopmarket.viewmodel.AdminVendorFilter
import com.mylaptopmarket.viewmodel.AdminVendorFormViewModel
import com.mylaptopmarket.viewmodel.AdminVendorsViewModel

sealed class AdminScreen(val route: String) {
    data object Login : AdminScreen("admin_login")
    data object Dashboard : AdminScreen("admin_dashboard")
    data object Vendors : AdminScreen("admin_vendors")
    data object VendorDetail : AdminScreen("admin_vendor/{vendorId}") {
        fun createRoute(id: Int) = "admin_vendor/$id"
    }
    data object VendorForm : AdminScreen("admin_vendor_form?vendorId={vendorId}") {
        fun createRoute(vendorId: Int? = null) =
            if (vendorId != null) "admin_vendor_form?vendorId=$vendorId" else "admin_vendor_form"
    }
    data object Chats : AdminScreen("admin_chats")
    data object ChatDetail : AdminScreen("admin_chat/{leftId}/{rightId}") {
        fun createRoute(leftId: Int, rightId: Int) = "admin_chat/$leftId/$rightId"
    }
    data object Inquiries : AdminScreen("admin_inquiries")
    data object InquiryDetail : AdminScreen("admin_inquiry/{inquiryId}") {
        fun createRoute(id: Int) = "admin_inquiry/$id"
    }
    data object Invoices : AdminScreen("admin_invoices")
    data object InvoiceDetail : AdminScreen("admin_invoice/{invoiceId}") {
        fun createRoute(id: Int) = "admin_invoice/$id"
    }
    data object Products : AdminScreen("admin_products")
    data object Requirements : AdminScreen("admin_requirements")
    data object RequirementDetail : AdminScreen("admin_requirement/{requirementId}") {
        fun createRoute(id: Int) = "admin_requirement/$id"
    }
}

private val adminTabs = listOf(
    AdminScreen.Dashboard.route to ("Home" to Icons.Default.Dashboard),
    AdminScreen.Vendors.route to ("Vendors" to Icons.Default.Store),
    AdminScreen.Inquiries.route to ("Inquiries" to Icons.Default.Mail),
    AdminScreen.Invoices.route to ("Invoices" to Icons.Default.Description)
)

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    standalone: Boolean,
    onExit: (() -> Unit)? = null
) {
    val repository = remember { ApiAdminRepository() }
    val authViewModel = remember { AdminAuthViewModel(repository) }
    val dashboardViewModel = remember { AdminDashboardViewModel(repository) }
    val vendorsViewModel = remember { AdminVendorsViewModel(repository) }
    val inquiriesViewModel = remember { AdminInquiriesViewModel(repository) }
    val chatsViewModel = remember { AdminChatsViewModel(repository) }
    val invoicesViewModel = remember { AdminInvoicesViewModel(repository) }
    val productsViewModel = remember { AdminProductsViewModel(repository) }
    val requirementsViewModel = remember { AdminRequirementsViewModel(repository) }

    val session by AdminSessionManager.currentSession.collectAsState()
    val pushToken by AdminPushEvents.token.collectAsState()
    val openTarget by AdminPushEvents.openTarget.collectAsState()
    val backStack by navController.currentBackStackEntryAsState()

    LaunchedEffect(session?.token, pushToken) {
        val token = pushToken ?: return@LaunchedEffect
        if (session == null) return@LaunchedEffect
        repository.registerPushToken(token.token, token.environment)
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(AdminScreen.Dashboard.route) {
                saveState = true
                inclusive = false
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun isTabSelected(route: String, current: String?): Boolean {
        return when (route) {
            AdminScreen.Dashboard.route ->
                current == route ||
                    current == AdminScreen.Products.route ||
                    current == AdminScreen.Requirements.route ||
                    current?.startsWith("admin_requirement/") == true ||
                    current == AdminScreen.Chats.route ||
                    current?.startsWith("admin_chat/") == true
            AdminScreen.Inquiries.route ->
                current == route || current?.startsWith("admin_inquiry/") == true
            AdminScreen.Invoices.route ->
                current == route || current?.startsWith("admin_invoice/") == true
            AdminScreen.Vendors.route ->
                current == route ||
                    current?.startsWith("admin_vendor/") == true ||
                    current?.startsWith("admin_vendor_form") == true
            else -> current == route
        }
    }

    @Composable
    fun BottomBar() {
        val backStack by navController.currentBackStackEntryAsState()
        val current = backStack?.destination?.route
        NavigationBar {
            adminTabs.forEach { (route, labelIcon) ->
                NavigationBarItem(
                    selected = isTabSelected(route, current),
                    onClick = {
                        if (route == AdminScreen.Vendors.route) {
                            vendorsViewModel.setFilter(AdminVendorFilter.ALL)
                        }
                        navigateToTab(route)
                    },
                    icon = { Icon(labelIcon.second, contentDescription = labelIcon.first) },
                    label = { Text(labelIcon.first) }
                )
            }
        }
    }

    fun goLoggedIn() {
        navController.navigate(AdminScreen.Dashboard.route) {
            popUpTo(AdminScreen.Login.route) { inclusive = true }
        }
    }

    fun goLogin() {
        authViewModel.logout()
        navController.navigate(AdminScreen.Login.route) {
            popUpTo(AdminScreen.Dashboard.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(session?.token, backStack?.destination?.route) {
        if (session != null && backStack?.destination?.route == AdminScreen.Login.route) {
            goLoggedIn()
        }
    }

    LaunchedEffect(openTarget, session, backStack?.destination?.route) {
        val target = openTarget ?: return@LaunchedEffect
        if (session == null) return@LaunchedEffect
        val current = backStack?.destination?.route
        if (current.isNullOrBlank() || current == AdminScreen.Login.route) return@LaunchedEffect
        when (target) {
            "vendor_register" -> {
                vendorsViewModel.setFilter(AdminVendorFilter.INACTIVE)
                vendorsViewModel.refresh()
                navigateToTab(AdminScreen.Vendors.route)
            }
            "inquiry" -> {
                inquiriesViewModel.refresh()
                navigateToTab(AdminScreen.Inquiries.route)
            }
        }
        AdminPushEvents.consumeOpen()
    }

    NavHost(
        navController = navController,
        startDestination = if (session != null) AdminScreen.Dashboard.route else AdminScreen.Login.route
    ) {
        composable(AdminScreen.Login.route) {
            AdminLoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { goLoggedIn() },
                onBackClick = if (standalone) {
                    null
                } else {
                    {
                        onExit?.invoke() ?: navController.popBackStack()
                        Unit
                    }
                }
            )
        }

        composable(AdminScreen.Dashboard.route) {
            AdminDashboardScreen(
                viewModel = dashboardViewModel,
                onActiveVendorsClick = {
                    vendorsViewModel.setFilter(AdminVendorFilter.ACTIVE)
                    navigateToTab(AdminScreen.Vendors.route)
                },
                onInactiveVendorsClick = {
                    vendorsViewModel.setFilter(AdminVendorFilter.INACTIVE)
                    navigateToTab(AdminScreen.Vendors.route)
                },
                onInquiriesClick = { navigateToTab(AdminScreen.Inquiries.route) },
                onInvoicesClick = { navigateToTab(AdminScreen.Invoices.route) },
                onProductsClick = { navController.navigate(AdminScreen.Products.route) },
                onRequirementsClick = { navController.navigate(AdminScreen.Requirements.route) },
                onChatsClick = { navController.navigate(AdminScreen.Chats.route) },
                onLogout = { goLogin() },
                bottomBar = { BottomBar() }
            )
        }

        composable(AdminScreen.Vendors.route) {
            AdminVendorsScreen(
                viewModel = vendorsViewModel,
                onAddClick = { navController.navigate(AdminScreen.VendorForm.createRoute()) },
                onVendorClick = { id -> navController.navigate(AdminScreen.VendorDetail.createRoute(id)) },
                onEditClick = { id -> navController.navigate(AdminScreen.VendorForm.createRoute(id)) },
                onBackClick = { navigateToTab(AdminScreen.Dashboard.route) },
                bottomBar = { BottomBar() }
            )
        }

        composable(
            route = AdminScreen.VendorDetail.route,
            arguments = listOf(navArgument("vendorId") { type = NavType.IntType })
        ) { backStackEntry ->
            val vendorId = backStackEntry.arguments?.getInt("vendorId") ?: return@composable
            val detailViewModel = remember(vendorId) { AdminVendorDetailViewModel(repository) }
            AdminVendorDetailScreen(
                vendorId = vendorId,
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(AdminScreen.VendorForm.createRoute(vendorId)) },
                bottomBar = { BottomBar() }
            )
        }

        composable(
            route = AdminScreen.VendorForm.route,
            arguments = listOf(
                navArgument("vendorId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val vendorId = backStackEntry.arguments?.getString("vendorId")?.toIntOrNull()
            val formViewModel = remember(vendorId) { AdminVendorFormViewModel(repository) }
            AdminVendorFormScreen(
                vendorId = vendorId,
                viewModel = formViewModel,
                onBackClick = { navController.popBackStack() },
                onSaved = {
                    vendorsViewModel.refresh()
                    navController.popBackStack()
                }
            )
        }

        composable(AdminScreen.Chats.route) {
            AdminChatsScreen(
                viewModel = chatsViewModel,
                onChatClick = { left, right ->
                    navController.navigate(AdminScreen.ChatDetail.createRoute(left, right))
                },
                onBackClick = { navController.popBackStack() },
                bottomBar = { BottomBar() }
            )
        }

        composable(
            route = AdminScreen.ChatDetail.route,
            arguments = listOf(
                navArgument("leftId") { type = NavType.IntType },
                navArgument("rightId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val leftId = backStackEntry.arguments?.getInt("leftId") ?: return@composable
            val rightId = backStackEntry.arguments?.getInt("rightId") ?: return@composable
            AdminChatDetailScreen(
                leftId = leftId,
                rightId = rightId,
                viewModel = chatsViewModel,
                onBackClick = { navController.popBackStack() },
                bottomBar = { BottomBar() }
            )
        }

        composable(AdminScreen.Inquiries.route) {
            AdminInquiriesScreen(
                viewModel = inquiriesViewModel,
                onInquiryClick = { id -> navController.navigate(AdminScreen.InquiryDetail.createRoute(id)) },
                bottomBar = { BottomBar() }
            )
        }

        composable(
            route = AdminScreen.InquiryDetail.route,
            arguments = listOf(navArgument("inquiryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val inquiryId = backStackEntry.arguments?.getInt("inquiryId") ?: return@composable
            AdminInquiryDetailScreen(
                inquiryId = inquiryId,
                viewModel = inquiriesViewModel,
                onBackClick = { navController.popBackStack() },
                bottomBar = { BottomBar() }
            )
        }

        composable(AdminScreen.Invoices.route) {
            AdminInvoicesScreen(
                viewModel = invoicesViewModel,
                onInvoiceClick = { id -> navController.navigate(AdminScreen.InvoiceDetail.createRoute(id)) },
                bottomBar = { BottomBar() }
            )
        }

        composable(
            route = AdminScreen.InvoiceDetail.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getInt("invoiceId") ?: return@composable
            AdminInvoiceDetailScreen(
                invoiceId = invoiceId,
                viewModel = invoicesViewModel,
                onBackClick = { navController.popBackStack() },
                bottomBar = { BottomBar() }
            )
        }

        composable(AdminScreen.Products.route) {
            AdminProductsScreen(
                viewModel = productsViewModel,
                onBackClick = { navController.popBackStack() },
                bottomBar = { BottomBar() }
            )
        }

        composable(AdminScreen.Requirements.route) {
            AdminRequirementsScreen(
                viewModel = requirementsViewModel,
                onRequirementClick = { id -> navController.navigate(AdminScreen.RequirementDetail.createRoute(id)) },
                onBackClick = { navController.popBackStack() },
                bottomBar = { BottomBar() }
            )
        }

        composable(
            route = AdminScreen.RequirementDetail.route,
            arguments = listOf(navArgument("requirementId") { type = NavType.IntType })
        ) { backStackEntry ->
            val requirementId = backStackEntry.arguments?.getInt("requirementId") ?: return@composable
            AdminRequirementDetailScreen(
                requirementId = requirementId,
                viewModel = requirementsViewModel,
                onBackClick = { navController.popBackStack() },
                bottomBar = { BottomBar() }
            )
        }
    }
}
