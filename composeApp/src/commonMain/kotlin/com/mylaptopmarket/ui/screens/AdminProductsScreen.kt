package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mylaptopmarket.repository.AdminProductData
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SuccessGreen
import com.mylaptopmarket.ui.theme.SuccessGreenLight
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.viewmodel.AdminProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    viewModel: AdminProductsViewModel,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    var pendingDelete by remember { mutableStateOf<AdminProductData?>(null) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    statusMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearStatus() },
            confirmButton = { TextButton(onClick = { viewModel.clearStatus() }) { Text("OK") } },
            title = { Text("Done") },
            text = { Text(message) }
        )
    }

    pendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(product.id)
                    pendingDelete = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
            title = { Text("Delete product?") },
            text = {
                Text(
                    "\"${product.productName.ifBlank { "This product" }}\" from ${product.vendorName ?: "this vendor"} will be removed from the market."
                )
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Products", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (products.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "No products yet", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        item { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                    }
                    items(products, key = { it.id }) { product ->
                        AdminProductCard(product, onDelete = { pendingDelete = product })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProductCard(product: AdminProductData, onDelete: () -> Unit) {
    val active = product.status == 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.mainImage.isNullOrBlank()) {
                    AsyncImage(
                        model = product.mainImage,
                        contentDescription = product.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = PrimaryBlue)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    product.productName.ifBlank { "Product #${product.id}" },
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(product.vendorName ?: "Vendor", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                if (!product.category.isNullOrBlank()) {
                    Text(product.category, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Spacer(Modifier.height(4.dp))
                Text("Rs ${product.price.toInt()}", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            Column(horizontalAlignment = Alignment.End) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (active) "Active" else "Inactive") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (active) SuccessGreenLight else ErrorRed.copy(alpha = 0.12f),
                        labelColor = if (active) SuccessGreen else ErrorRed
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                }
            }
        }
    }
}
