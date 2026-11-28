package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.network.FavoriteDealer
import com.mylaptopmarket.network.FavoriteDealersManager
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.network.VendorSession
import com.mylaptopmarket.repository.ApiAuthRepository
import com.mylaptopmarket.ui.theme.*
import com.mylaptopmarket.util.NetworkStatus
import com.mylaptopmarket.util.OfflineSync
import com.mylaptopmarket.util.openUrl
import com.mylaptopmarket.viewmodel.VendorProductsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProductsScreen(
    viewModel: VendorProductsViewModel,
    vendorId: String,
    vendorName: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val online by NetworkStatus.online.collectAsState()
    val catalogRevision by OfflineSync.revision.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val logoUrl by viewModel.logoUrl.collectAsState()
    val resolvedVendorName by viewModel.resolvedVendorName.collectAsState()
    val titleName = resolvedVendorName?.takeIf { it.isNotBlank() } ?: vendorName
    val favorites by FavoriteDealersManager.items.collectAsState()
    val isFavorite = favorites.any { it.id == vendorId }
    val isSelf = SessionManager.currentSession.value?.id?.toString() == vendorId
    val scope = rememberCoroutineScope()
    val authRepository = remember { ApiAuthRepository() }
    var showProfile by remember { mutableStateOf(false) }
    var profile by remember(vendorId) { mutableStateOf<VendorSession?>(null) }
    var profileLoading by remember(vendorId) { mutableStateOf(false) }
    var profileError by remember(vendorId) { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openProfile = {
        showProfile = true
        if (profile == null && !profileLoading) {
            profileLoading = true
            scope.launch {
                authRepository.getVendorStatus(vendorId.toIntOrNull() ?: 0)
                    .onSuccess {
                        profile = it
                        profileError = null
                    }
                    .onFailure { profileError = it.message ?: "Could not load profile" }
                profileLoading = false
            }
        }
    }

    LaunchedEffect(catalogRevision) {
        viewModel.load()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(1.dp, CardBorder),
                shadowElevation = 1.dp
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = openProfile)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!logoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = logoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    titleName,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    if (online) "${products.size} products" else "Saved stock · ${products.size}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View profile",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    actions = {
                        if (!isSelf) {
                            IconButton(
                                onClick = {
                                    if (isFavorite) {
                                        FavoriteDealersManager.remove(vendorId)
                                    } else {
                                        scope.launch {
                                            val location = authRepository
                                                .getVendorStatus(vendorId.toIntOrNull() ?: 0)
                                                .getOrNull()
                                                ?.location
                                                .orEmpty()
                                            FavoriteDealersManager.add(
                                                FavoriteDealer(
                                                    id = vendorId,
                                                    name = titleName,
                                                    location = location,
                                                    logoUrl = logoUrl,
                                                    phone = products.firstOrNull()?.providerNumber.orEmpty()
                                                )
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite dealer",
                                    tint = if (isFavorite) Color(0xFFE11D48) else PrimaryBlue
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            }
        }
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }

            products.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, null, tint = PrimaryBlue, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("No products from this vendor", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(BackgroundLight),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(product = product, onClick = { onProductClick(product.id) })
                }
            }
        }
    }

    if (showProfile) {
        ModalBottomSheet(
            onDismissRequest = { showProfile = false },
            sheetState = sheetState,
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            VendorPublicProfile(
                fallbackName = titleName,
                fallbackLogo = logoUrl,
                profile = profile,
                loading = profileLoading,
                error = profileError
            )
        }
    }
}

@Composable
private fun VendorPublicProfile(
    fallbackName: String,
    fallbackLogo: String?,
    profile: VendorSession?,
    loading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        val seller = profile
        val name = seller?.shopName?.takeIf { it.isNotBlank() } ?: fallbackName
        val logo = seller?.logoUrl?.takeIf { it.isNotBlank() } ?: fallbackLogo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                if (!logo.isNullOrBlank()) {
                    AsyncImage(
                        model = logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                if (seller?.shopTrusted == true) {
                    Spacer(Modifier.height(6.dp))
                    ShopVerifiedStamp()
                }
            }
        }

        when {
            loading && seller == null -> {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            error != null && seller == null -> {
                Spacer(Modifier.height(16.dp))
                Text(error, color = ErrorRed)
            }
            seller != null -> {
                if (!seller.shopPhotoUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    AsyncImage(
                        model = seller.shopPhotoUrl,
                        contentDescription = "Shop photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(16.dp))
                ProfileLine("City", seller.location)
                ProfileLine("State", seller.state)
                if (seller.mobile.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { openUrl("tel:${seller.mobile}") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(seller.mobile, fontWeight = FontWeight.Bold)
                    }
                }
                ProfileLine("Email", seller.email)
                ProfileLine("GSTIN", seller.gstin)
                val registry = seller.gstProfile
                if (registry != null) {
                    Spacer(Modifier.height(12.dp))
                    GstDetailsCard(registry)
                } else if (seller.gstin.isBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("GST details are not added yet.", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ProfileLine(label: String, value: String) {
    if (value.isBlank()) return
    Spacer(Modifier.height(10.dp))
    Text(label, fontSize = 12.sp, color = TextSecondary)
    Text(value, fontWeight = FontWeight.SemiBold, color = TextPrimary)
}
