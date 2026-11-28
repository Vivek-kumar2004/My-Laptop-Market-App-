package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.model.StockStatus
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.PrimaryLight
import com.mylaptopmarket.ui.theme.SurfaceHover
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.viewmodel.ProductDetailViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.mylaptopmarket.util.YoutubeVideoPlayer
import com.mylaptopmarket.util.NetworkStatus
import com.mylaptopmarket.util.OfflineSync
import com.mylaptopmarket.util.makeCall
import com.mylaptopmarket.util.openUrl
import io.ktor.http.encodeURLParameter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Login
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mylaptopmarket.network.FavoriteDealer
import com.mylaptopmarket.network.FavoriteDealersManager
import com.mylaptopmarket.repository.ApiAuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductDetailViewModel,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChatWithSellerClick: (String, String, String) -> Unit,
    onSellerClick: (String, String) -> Unit = { _, _ -> },
    onLoginClick: () -> Unit = {},
    onViewStockSheet: () -> Unit = {}
) {
    val product by viewModel.product.collectAsState()
    val provider by viewModel.provider.collectAsState()
    val online by NetworkStatus.online.collectAsState()
    val catalogRevision by OfflineSync.revision.collectAsState()
    val session by SessionManager.currentSession.collectAsState()
    val isVendorLoggedIn = session != null

    var showFullScreenViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(productId, catalogRevision) {
        viewModel.loadProductDetail(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = product?.name ?: "Product Details",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (product != null && !online) {
                            Text("Saved copy", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (isVendorLoggedIn) {
                val phone = provider?.contactPhone
                if (!phone.isNullOrBlank()) {
                    ExtendedFloatingActionButton(
                        onClick = { makeCall(phone) },
                        icon = { Icon(Icons.Default.Call, contentDescription = null) },
                        text = {
                            Text("Call Seller", fontWeight = FontWeight.Bold)
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        product?.let { prod ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                val pagerState = rememberPagerState(pageCount = { prod.media.size })
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = prod.media[page].url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    selectedImageIndex = page
                                    showFullScreenViewer = true
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )

                    Row(
                        Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(prod.media.size) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.White.copy(
                                    alpha = 0.6f
                                )
                            Box(
                                modifier = Modifier
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(
                                        if (pagerState.currentPage == iteration) 16.dp else 8.dp,
                                        8.dp
                                    )
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-16).dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            prod.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val priceText = if (!prod.price.isNullOrBlank() && prod.price != "0") "₹" + prod.price else "Call for Price"
                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (isVendorLoggedIn) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    shareProductOnWhatsApp(prod)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Text("Share on WhatsApp", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!prod.category.isNullOrBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        prod.category,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val stockColor = if (prod.stockCount > 5) Color(0xFF4CAF50) else Color.Red
                            Surface(
                                color = stockColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    if (prod.stockStatus == StockStatus.OUT_OF_STOCK) "Out of Stock" else "${prod.stockCount} Available",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = stockColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        DetailSectionHeader("Product Gallery")
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 24.dp)
                        ) {
                            itemsIndexed(prod.media) { index, item ->
                                Card(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clickable {
                                            selectedImageIndex = index
                                            showFullScreenViewer = true
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    AsyncImage(
                                        model = item.url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        DetailSectionHeader("Description")
                        Text(
                            prod.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp),
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!prod.videoUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            DetailSectionHeader("Product Video")
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                YoutubeVideoPlayer(
                                    videoUrl = prod.videoUrl,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (prod.hasStockSheet) {
                            Spacer(modifier = Modifier.height(24.dp))
                            DetailSectionHeader("Stock sheet")
                            Spacer(modifier = Modifier.height(12.dp))
                            if (isVendorLoggedIn) {
                                Button(
                                    onClick = onViewStockSheet,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("View stock sheet", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Login required", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Stock sheet rows stay inside the app. Log in to view them.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                                            Text("Login as Vendor")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        DetailSectionHeader("Seller Information")
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isVendorLoggedIn && provider != null) {
                            val prov = provider!!
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.2f
                                    )
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { onSellerClick(prov.id, prov.name) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(PrimaryLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            var logoFailed by remember(prov.id, prov.logoUrl) { mutableStateOf(false) }
                                            if (!prov.logoUrl.isNullOrBlank() && !logoFailed) {
                                                AsyncImage(
                                                    model = prov.logoUrl,
                                                    contentDescription = "${prov.name} logo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop,
                                                    onError = { logoFailed = true }
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Storefront,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                prov.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 20.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (prod.shopTrusted) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                ShopVerifiedStamp()
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "View all products",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }

                                    val favorites by FavoriteDealersManager.items.collectAsState()
                                    val isFavorite = favorites.any { it.id == prov.id }
                                    val isSelf = SessionManager.currentSession.value?.id?.toString() == prov.id
                                    val favoriteScope = rememberCoroutineScope()
                                    val authRepository = remember { ApiAuthRepository() }
                                    if (!isSelf) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedButton(
                                            onClick = {
                                                if (isFavorite) {
                                                    FavoriteDealersManager.remove(prov.id)
                                                } else {
                                                    favoriteScope.launch {
                                                        val location = authRepository
                                                            .getVendorStatus(prov.id.toIntOrNull() ?: 0)
                                                            .getOrNull()
                                                            ?.location
                                                            .orEmpty()
                                                        FavoriteDealersManager.add(
                                                            FavoriteDealer(
                                                                id = prov.id,
                                                                name = prov.name,
                                                                location = location,
                                                                logoUrl = prov.logoUrl,
                                                                phone = prov.contactPhone
                                                            )
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                tint = if (isFavorite) Color(0xFFE11D48) else PrimaryBlue
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                if (isFavorite) "Remove from Favorite Dealers" else "Add to Favorite Dealers",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            makeCall(prov.contactPhone)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            prov.contactPhone,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    if (session?.id?.toString() != prov.id) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = { onChatWithSellerClick(prov.id, prov.name, prod.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Chat with Seller")
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.2f
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "Seller details are hidden",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Login as a vendor to view shop name, logo, phone and chat.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = onLoginClick,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Login, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Login as Vendor")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                if (showFullScreenViewer) {
                    FullScreenImageViewer(
                        media = prod.media.map { it.url },
                        initialIndex = selectedImageIndex,
                        onDismiss = { showFullScreenViewer = false }
                    )
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun FullScreenImageViewer(media: List<String>, initialIndex: Int, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            val pagerState =
                rememberPagerState(initialPage = initialIndex, pageCount = { media.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = media[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                text = "${pagerState.currentPage + 1} / ${media.size}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun DetailSectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp, 16.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun shareProductOnWhatsApp(product: Product) {
    val priceText = if (!product.price.isNullOrBlank() && product.price != "0") {
        "₹${product.price}"
    } else {
        "Call for Price"
    }
    val stockText = if (product.stockStatus == StockStatus.OUT_OF_STOCK) {
        "Out of Stock"
    } else {
        "${product.stockCount} Available"
    }
    val productUrl = "https://mylaptopmarket.com/product.html?id=${product.id}"
    val message = buildString {
        appendLine(product.name)
        appendLine(productUrl)
        if (!product.category.isNullOrBlank()) appendLine("Category: ${product.category}")
        appendLine("Price: $priceText")
        appendLine("Stock: $stockText")
        if (product.description.isNotBlank()) {
            appendLine()
            appendLine(product.description.trim())
        }
        if (!product.videoUrl.isNullOrBlank()) {
            appendLine()
            appendLine("Video: ${product.videoUrl}")
        }
    }.trim()
    openUrl("https://wa.me/?text=${message.encodeURLParameter()}")
}
