package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.network.FavoriteDealersManager
import com.mylaptopmarket.repository.ApiAuthRepository
import com.mylaptopmarket.ui.components.MainBottomBar
import com.mylaptopmarket.ui.components.MainTab
import com.mylaptopmarket.ui.theme.BackgroundLight
import com.mylaptopmarket.ui.theme.CardBorder
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.PrimaryLight
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.TextPrimary
import com.mylaptopmarket.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteDealersScreen(
    onBackClick: () -> Unit,
    onDealerClick: (String, String) -> Unit,
    onMainTabSelected: ((MainTab) -> Unit)? = null
) {
    val dealers by FavoriteDealersManager.items.collectAsState()
    val authRepository = remember { ApiAuthRepository() }

    LaunchedEffect(dealers.map { it.id to it.location }) {
        dealers.filter { it.location.isBlank() }.forEach { dealer ->
            val vendorId = dealer.id.toIntOrNull() ?: return@forEach
            val location = authRepository.getVendorStatus(vendorId).getOrNull()?.location.orEmpty()
            if (location.isNotBlank()) {
                FavoriteDealersManager.update(dealer.copy(location = location))
            }
        }
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
                        Text(
                            "Favorite Dealers",
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            }
        },
        bottomBar = {
            if (onMainTabSelected != null) {
                MainBottomBar(selected = MainTab.Home, onTabSelected = onMainTabSelected)
            }
        }
    ) { padding ->
        if (dealers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No favorite dealers yet", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Open a seller and tap Favorite Dealer to save them here.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dealers, key = { it.id }) { dealer ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDealerClick(dealer.id, dealer.name) },
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = PrimaryLight,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (!dealer.logoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = dealer.logoUrl,
                                            contentDescription = dealer.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    dealer.name,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        dealer.location.ifBlank { "Address not available" },
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
