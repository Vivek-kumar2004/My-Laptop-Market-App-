package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylaptopmarket.ui.theme.*
import com.mylaptopmarket.viewmodel.MyListingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    viewModel: MyListingsViewModel,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onMainTabSelected: ((com.mylaptopmarket.ui.components.MainTab) -> Unit)? = null
) {
    val myProducts by viewModel.myProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                            "My Listings",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
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
                com.mylaptopmarket.ui.components.MainBottomBar(
                    selected = com.mylaptopmarket.ui.components.MainTab.Listings,
                    onTabSelected = onMainTabSelected
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (myProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
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
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No Listings Found",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "You haven't added any products yet. Use 'Add a Product' in the drawer menu to post your first hardware item.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(BackgroundLight),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(myProducts) { product ->
                    ProductCard(product = product, onClick = { onProductClick(product.id) })
                }
            }
        }
    }
}