package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.model.RequirementGroup
import com.mylaptopmarket.model.RequirementVendor
import com.mylaptopmarket.ui.theme.BackgroundLight
import com.mylaptopmarket.ui.theme.CardBorder
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.PrimaryLight
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.TextPrimary
import com.mylaptopmarket.ui.theme.TextSecondary
import com.mylaptopmarket.ui.theme.WarningOrange
import com.mylaptopmarket.viewmodel.RequirementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementsScreen(
    viewModel: RequirementsViewModel,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onChatWithVendor: (Int, String) -> Unit = { _, _ -> },
    onOpenVendorProducts: (Int, String) -> Unit = { _, _ -> }
) {
    val feed by viewModel.feed.collectAsState()
    val mine by viewModel.mine.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedVendor by remember { mutableStateOf<RequirementVendor?>(null) }
    var selectedGroupCategory by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                            "Requirements",
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
                    actions = {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Default.Add, contentDescription = "Post", tint = PrimaryBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = PrimaryBlue,
                contentColor = SurfaceWhite,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Post requirement", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (!error.isNullOrBlank()) {
                    item {
                        Text(error ?: "", color = ErrorRed, fontSize = 13.sp)
                    }
                }

                item {
                    Text(
                        "Similar product needs are grouped into one card. Tap a vendor to open their requirement.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                if (!isLoading && feed.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No active requirements", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Be the first to post what you need.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                items(feed, key = { it.titleKey.ifBlank { it.title } }) { group ->
                    RequirementGroupCard(
                        group = group,
                        onCloseMine = { id -> viewModel.closeMine(id) },
                        onVendorClick = { vendor ->
                            selectedGroupCategory = group.category
                            selectedVendor = vendor
                        }
                    )
                }

                if (mine.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "My posts",
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                    items(mine, key = { "mine_${it.id}" }) { myReq ->
                        MyRequirementCard(
                            title = myReq.title,
                            category = myReq.category,
                            status = myReq.status,
                            description = myReq.description,
                            quantity = myReq.quantity,
                            onClose = { viewModel.closeMine(myReq.id) },
                            onDelete = { viewModel.deleteMine(myReq.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }
    }

    selectedVendor?.let { vendor ->
        ModalBottomSheet(
            onDismissRequest = { selectedVendor = null },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            VendorRequirementDetailSheet(
                vendor = vendor,
                category = selectedGroupCategory.orEmpty(),
                onChat = {
                    selectedVendor = null
                    onChatWithVendor(vendor.id, vendor.shopName)
                },
                onViewProducts = {
                    selectedVendor = null
                    onOpenVendorProducts(vendor.id, vendor.shopName)
                },
                onClose = { selectedVendor = null }
            )
        }
    }
}

@Composable
private fun VendorRequirementDetailSheet(
    vendor: RequirementVendor,
    category: String,
    onChat: () -> Unit,
    onViewProducts: () -> Unit,
    onClose: () -> Unit
) {
    val title = vendor.canonicalTitle?.takeIf { it.isNotBlank() }
        ?: vendor.title?.takeIf { it.isNotBlank() }
        ?: "Requirement"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RequirementVendorLogo(logoUrl = vendor.logoUrl, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    vendor.shopName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text("Vendor requirement", color = TextMuted, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (category.isNotBlank()) {
                AssistChip(onClick = {}, label = { Text(category) }, enabled = false)
            }
            if (vendor.quantity != null) {
                AssistChip(
                    onClick = {},
                    label = { Text("Qty ${vendor.quantity}") },
                    enabled = false
                )
            }
        }

        if (!vendor.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Details", fontWeight = FontWeight.SemiBold, color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(vendor.description!!, color = TextSecondary, fontSize = 14.sp)
        } else {
            Spacer(modifier = Modifier.height(12.dp))
            Text("No extra description provided.", color = TextMuted, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onChat,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat with vendor")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onViewProducts,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Storefront, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View their products")
        }
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun MyRequirementCard(
    title: String,
    category: String,
    status: String,
    description: String? = null,
    quantity: Int? = null,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(4.dp))
            val meta = buildString {
                append(category)
                if (quantity != null) append(" · Qty $quantity")
                append(" · ${status.replaceFirstChar { it.uppercase() }}")
            }
            Text(meta, color = TextSecondary, fontSize = 12.sp)
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(description, color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status == "active") {
                    OutlinedButton(
                        onClick = onClose,
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Close") }
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = ErrorRed)
                }
            }
        }
    }
}

@Composable
private fun RequirementGroupCard(
    group: RequirementGroup,
    onCloseMine: (Int) -> Unit,
    onVendorClick: (RequirementVendor) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                group.title,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 16.sp,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text(group.category) }, enabled = false)
                if (group.quantity != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Qty ${group.quantity}") },
                        enabled = false
                    )
                }
                if (group.count > 1) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${group.count} vendors") },
                        enabled = false,
                        colors = AssistChipDefaults.assistChipColors(labelColor = WarningOrange)
                    )
                }
            }
            if (!group.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    group.description!!,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 8
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Vendors looking for this",
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            group.vendors.forEach { vendor ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onVendorClick(vendor) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    RequirementVendorLogo(logoUrl = vendor.logoUrl, size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            vendor.shopName,
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val subtitle = buildString {
                            vendor.quantity?.let { append("Qty $it") }
                            if (!vendor.description.isNullOrBlank()) {
                                if (isNotEmpty()) append(" · ")
                                append("Has details")
                            }
                        }
                        if (subtitle.isNotBlank()) {
                            Text(subtitle, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            val myId = group.myRequirementId
            if (myId != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onCloseMine(myId) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close my post")
                }
            }
        }
    }
}

@Composable
private fun RequirementVendorLogo(
    logoUrl: String?,
    size: androidx.compose.ui.unit.Dp
) {
    var logoFailed by remember(logoUrl) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(PrimaryLight),
        contentAlignment = Alignment.Center
    ) {
        if (!logoUrl.isNullOrBlank() && !logoFailed) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { logoFailed = true }
            )
        } else {
            Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}
