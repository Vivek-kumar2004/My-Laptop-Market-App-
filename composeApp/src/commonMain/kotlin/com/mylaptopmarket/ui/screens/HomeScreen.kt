package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.StockStatus
import com.mylaptopmarket.ui.components.PlaceFilterBar
import com.mylaptopmarket.ui.components.productMatchesPlace
import com.mylaptopmarket.ui.tour.AppTour
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.ui.theme.*
import com.mylaptopmarket.util.currentHourOfDay
import com.mylaptopmarket.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import mylaptopmarket.composeapp.generated.resources.Res
import mylaptopmarket.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val tintColor: Color,
    val bgLightColor: Color
)

val categories = listOf(
    CategoryItem("Laptops", Icons.Default.Laptop, Color(0xFF2563EB), Color(0xFFEFF6FF)),
    CategoryItem("Desktop", Icons.Default.DesktopWindows, Color(0xFF6366F1), Color(0xFFEEF2FF)),
    CategoryItem("TFT/Screen", Icons.Default.DesktopMac, Color(0xFF0D9488), Color(0xFFF0FDFA)),
    CategoryItem("Server", Icons.Default.Dns, Color(0xFFD97706), Color(0xFFFEF3C7)),
    CategoryItem("Storage", Icons.Default.Storage, Color(0xFF059669), Color(0xFFECFDF5)),
    CategoryItem("Accessories", Icons.Default.Headset, Color(0xFFE11D48), Color(0xFFFFF1F2)),
    CategoryItem("Graphics Card", Icons.Default.Memory, Color(0xFF7C3AED), Color(0xFFF5F3FF)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSearchClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onProviderClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onMyListingsClick: () -> Unit,
    onRequirementsClick: () -> Unit = {},
    onSellerVerificationClick: () -> Unit = {},
    onVoiceChatbotClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onBuyClick: () -> Unit = onSearchClick,
    onMarketClick: (String) -> Unit = {},
    onMainTabSelected: (com.mylaptopmarket.ui.components.MainTab) -> Unit = {},
    messagesBadge: Int = 0
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val session by com.mylaptopmarket.network.SessionManager.currentSession.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()
    val marketListings by viewModel.marketListings.collectAsState()
    var marketChip by remember { mutableStateOf("All") }
    val marketShown = marketListings.filter { marketChip == "All" || it.category == marketChip }
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showingSaved by viewModel.showingSaved.collectAsState()
    var selectedState by remember { mutableStateOf("") }
    val shownProducts = topProducts.filter { productMatchesPlace(it, selectedState) }
    var homeNotice by remember { mutableStateOf("") }
    val announcementRepository = remember { com.mylaptopmarket.repository.ApiAdminRepository() }

    LaunchedEffect(session?.id) {
        if (session == null) {
            homeNotice = ""
            return@LaunchedEffect
        }
        val text = announcementRepository.currentAnnouncement(forVendorApp = true).getOrNull().orEmpty().trim()
        homeNotice = if (text.isNotEmpty() && text != com.mylaptopmarket.util.seenAnnouncement()) text else ""
    }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    LaunchedEffect(session?.id, session?.logoUrl) {
        val current = session ?: return@LaunchedEffect
        if (current.logoUrl.isNullOrBlank()) {
            viewModel.ensureSessionLogo(current.id, current.shopName)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceWhite,
                modifier = Modifier
                    .width(310.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(PrimaryBlue.copy(alpha = 0.08f), PrimaryLight)
                            )
                        )
                        .padding(24.dp)
                ) {
                    if (session == null) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = SurfaceWhite)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "My Laptop Market",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "B2B Hardware Marketplace",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onLoginClick()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Vendor Login", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.clickable {
                                scope.launch { drawerState.close() }
                                onProfileClick()
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                ) {
                                    VendorAvatar(
                                        shopName = session!!.shopName,
                                        logoUrl = session!!.logoUrl,
                                        size = 48.dp,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        session!!.shopName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "@${session!!.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (session!!.shopTrusted) SuccessGreenLight else Color(0xFFFFF7ED),
                                border = BorderStroke(
                                    1.dp,
                                    if (session!!.shopTrusted) SuccessGreen.copy(alpha = 0.3f) else WarningOrange.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (session!!.shopTrusted) SuccessGreen else WarningOrange)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (session!!.shopTrusted) "Verified Vendor" else "Pending Verification",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (session!!.shopTrusted) SuccessGreen else WarningOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Navigation Items
                NavigationDrawerItem(
                    label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryLight,
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Add a Product", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        when {
                            session == null -> onLoginClick()
                            !session!!.isVerified -> onSellerVerificationClick()
                            else -> onAddProductClick()
                        }
                    },
                    icon = { Icon(Icons.Default.AddCircleOutline, null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("My Listings", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        when {
                            session == null -> onLoginClick()
                            !session!!.isVerified -> onSellerVerificationClick()
                            else -> onMyListingsClick()
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Requirements", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (session != null) onRequirementsClick() else onLoginClick()
                    },
                    icon = { Icon(Icons.Default.Assignment, null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Messages", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (session != null) onMessagesClick() else onLoginClick()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Floor Desk", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        onVoiceChatbotClick()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.RecordVoiceOver, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = PrimaryBlue,
                        unselectedTextColor = PrimaryBlue
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("My Profile", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (session != null) onProfileClick() else onLoginClick()
                    },
                    icon = { Icon(Icons.Default.AccountCircle, null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Seller Verification", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (session != null) onSellerVerificationClick() else onLoginClick()
                    },
                    icon = { Icon(Icons.Default.VerifiedUser, null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = CardBorder)

                if (session != null) {
                    TextButton(
                        onClick = {
                            com.mylaptopmarket.network.SessionManager.logout()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Log Out", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    ) {
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        Scaffold(
            containerColor = BackgroundLight,
            topBar = {
                Surface(
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 1.dp
                ) {
                    TopAppBar(
                        windowInsets = TopAppBarDefaults.windowInsets,
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(Res.drawable.app_icon),
                                    contentDescription = "My Laptop Market",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "MY LAPTOP MARKET",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                    letterSpacing = 0.2.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.tourTarget("menu")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                            }
                        },
                        actions = {
                            Surface(
                                shape = CircleShape,
                                color = SurfaceHover,
                                modifier = Modifier
                                    .size(40.dp)
                                    .tourTarget("search")
                            ) {
                                IconButton(onClick = onSearchClick) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (session != null) onProfileClick() else onLoginClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val loggedIn = session
                                if (loggedIn != null) {
                                    VendorAvatar(
                                        shopName = loggedIn.shopName,
                                        logoUrl = loggedIn.logoUrl,
                                        size = 40.dp,
                                        fontSize = 15.sp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(SurfaceHover),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Login",
                                            tint = TextPrimary
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                    )
                }
            },
            bottomBar = {
                com.mylaptopmarket.ui.components.MainBottomBar(
                    selected = com.mylaptopmarket.ui.components.MainTab.Home,
                    messagesBadge = messagesBadge,
                    onTabSelected = onMainTabSelected,
                    registerTour = true
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onVoiceChatbotClick,
                    modifier = Modifier.tourTarget("ask"),
                    containerColor = PrimaryBlue,
                    contentColor = SurfaceWhite,
                    shape = RoundedCornerShape(28.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = "Ask Me",
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            "Ask Me",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(BackgroundLight)
        ) {
            val gridState = rememberLazyGridState()
            val errorShown = errorMessage != null && topProducts.isEmpty()
            val offlineNote = showingSaved && (topProducts.isNotEmpty() || marketListings.isNotEmpty())
            val emptyPlace = topProducts.isNotEmpty() && shownProducts.isEmpty()
            SideEffect {
                AppTour.scrollHome = { target ->
                    val index = when (target) {
                        "buy" -> 3
                        "sell" -> 4
                        "requirement" -> 5
                        "favorites" -> 6
                        "products" -> 7
                        "open-market" -> if (marketListings.isEmpty()) {
                            null
                        } else {
                            var start = 8
                            if (errorShown) start += 1
                            if (emptyPlace) start += 1
                            start + shownProducts.size
                        }
                        else -> null
                    }
                    if (index != null) gridState.animateScrollToItem(index)
                }
            }
            DisposableEffect(Unit) {
                onDispose { AppTour.scrollHome = null }
            }
            PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshData(fromPull = true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundLight)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 24.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                    item(span = { GridItemSpan(2) }) {
                        val vendorName = session?.shopName?.takeIf { it.isNotBlank() }
                        Text(
                            if (vendorName != null) "${homeGreeting()}, $vendorName" else homeGreeting(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    item(span = { GridItemSpan(2) }) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryBlue,
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
                            ) {
                                Text(
                                    "B2B Marketplace for Refurbished IT Assets",
                                    color = SurfaceWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    lineHeight = 22.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Buy, sell, source requirements, and save favorite dealers — all in one place.",
                                    color = SurfaceWhite.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    item(span = { GridItemSpan(2) }) {
                        SectionTitle(title = "Quick Actions")
                    }

                    items(
                        listOf(
                            HomeQuickAction(
                                title = "Buy",
                                subtitle = "Find Best Deals",
                                icon = Icons.Default.ShoppingCart,
                                tint = Color(0xFF2563EB),
                                bg = Color(0xFFEFF6FF),
                                tourId = "buy",
                                onClick = onBuyClick
                            ),
                            HomeQuickAction(
                                title = "Sell",
                                subtitle = "List Your Stock",
                                icon = Icons.Default.Storefront,
                                tint = Color(0xFF059669),
                                bg = Color(0xFFECFDF5),
                                tourId = "sell",
                                onClick = {
                                    if (session != null) onAddProductClick() else onLoginClick()
                                }
                            ),
                            HomeQuickAction(
                                title = "Requirement",
                                subtitle = "Post What You Need",
                                icon = Icons.Default.Assignment,
                                tint = Color(0xFFD97706),
                                bg = Color(0xFFFEF3C7),
                                tourId = "requirement",
                                onClick = {
                                    if (session != null) onRequirementsClick() else onLoginClick()
                                }
                            ),
                            HomeQuickAction(
                                title = "Favorite Dealers",
                                subtitle = "Saved Vendors",
                                icon = Icons.Default.Storefront,
                                tint = Color(0xFFE11D48),
                                bg = Color(0xFFFFF1F2),
                                tourId = "favorites",
                                onClick = onWishlistClick
                            )
                        )
                    ) { action ->
                        HomeQuickActionCard(action = action)
                    }

                    // Section: Recent Products
                    item(span = { GridItemSpan(2) }) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionTitle(title = "Recent Products", tourId = "products")
                            Spacer(Modifier.height(10.dp))
                            PlaceFilterBar(
                                products = topProducts,
                                selectedState = selectedState,
                                onStateChange = { selectedState = it }
                            )
                        }
                    }

                    // Error message
                    if (errorMessage != null && topProducts.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = ErrorRed,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        errorMessage ?: "Error loading products",
                                        color = ErrorRed,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.refreshData() },
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }

                    if (topProducts.isNotEmpty() && shownProducts.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                "No products in this state",
                                color = TextMuted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    items(shownProducts) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) }
                        )
                    }

                    if (marketListings.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            OpenMarketHeader(
                                listings = marketListings,
                                chip = marketChip,
                                shownCount = marketShown.size,
                                onChip = { marketChip = it }
                            )
                        }
                        items(marketShown, key = { it.id }) { listing ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0767D8))
                                    .padding(8.dp)
                            ) {
                                OpenMarketCard(
                                    listing = listing,
                                    onOpen = onMarketClick,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item(span = { GridItemSpan(2) }) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            if (offlineNote) {
                Text(
                    "No internet. Showing saved stock.",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
        }
    }
    if (!com.mylaptopmarket.ui.tour.AppTour.running.collectAsState().value && session != null && homeNotice.isNotBlank()) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {
                    com.mylaptopmarket.util.markAnnouncementSeen(homeNotice)
                    homeNotice = ""
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            title = { Text("My Laptop Market", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
            text = { Text(homeNotice) }
        )
    }
}

private data class HomeQuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val bg: Color,
    val tourId: String,
    val onClick: () -> Unit
)

@Composable
private fun HomeQuickActionCard(action: HomeQuickAction) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .tourTarget(action.tourId)
            .clickable(onClick = action.onClick),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(action.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.tint,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                action.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                action.subtitle,
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, tourId: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(if (tourId != null) Modifier.tourTarget(tourId) else Modifier)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 18.sp
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    val session by com.mylaptopmarket.network.SessionManager.currentSession.collectAsState()
    val showVendorDetails = session != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 2.dp
    ) {
        Column {
            // Image with Category Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(SurfaceHover)
            ) {
                val imageUrl = product.media.firstOrNull()?.url
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Laptop,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Category Badge Overlay
                if (!product.category.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SurfaceWhite.copy(alpha = 0.92f),
                        border = BorderStroke(0.5.dp, CardBorder),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Info Content
            Column(modifier = Modifier.padding(12.dp)) {
                // Title
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price & Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceText = if (!product.price.isNullOrBlank() && product.price != "0") {
                        "₹" + product.price
                    } else {
                        "Call for Price"
                    }
                    Text(
                        text = priceText,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue,
                        fontSize = 14.sp
                    )

                    // Stock Pill
                    val isAvailable = product.stockStatus == StockStatus.AVAILABLE
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isAvailable) SuccessGreenLight else SurfaceHover
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isAvailable) SuccessGreen else TextMuted)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isAvailable) "In Stock" else "Out",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAvailable) SuccessGreen else TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (showVendorDetails) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = product.providerName,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (product.shopTrusted) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorAvatar(
    shopName: String,
    logoUrl: String?,
    size: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    var logoFailed by remember(logoUrl) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(PrimaryBlue),
        contentAlignment = Alignment.Center
    ) {
        if (!logoUrl.isNullOrBlank() && !logoFailed) {
            AsyncImage(
                model = logoUrl,
                contentDescription = "$shopName logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { logoFailed = true }
            )
        } else {
            Text(
                shopName.take(1).ifBlank { "V" }.uppercase(),
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }
}

private fun homeGreeting(): String {
    return when (currentHourOfDay()) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello"
    }
}