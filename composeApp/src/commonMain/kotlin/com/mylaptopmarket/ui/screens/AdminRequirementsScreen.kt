package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.repository.AdminRequirementGroupData
import com.mylaptopmarket.repository.AdminRequirementVendorData
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.PrimaryLight
import com.mylaptopmarket.ui.theme.SuccessGreen
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.TextPrimary
import com.mylaptopmarket.ui.theme.TextSecondary
import com.mylaptopmarket.ui.theme.WarningOrange
import com.mylaptopmarket.util.makeCall
import com.mylaptopmarket.viewmodel.AdminRequirementFilter
import com.mylaptopmarket.viewmodel.AdminRequirementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequirementsScreen(
    viewModel: AdminRequirementsViewModel,
    onRequirementClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val requirements by viewModel.requirements.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Requirements", fontWeight = FontWeight.Bold) },
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
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminRequirementFilter.entries.forEach { option ->
                        AssistChip(
                            onClick = { viewModel.setFilter(option) },
                            label = { Text(option.title) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (filter == option) PrimaryBlue.copy(alpha = 0.15f) else SurfaceWhite,
                                labelColor = if (filter == option) PrimaryBlue else TextMuted
                            )
                        )
                    }
                }
                if (requirements.isEmpty() && !isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage ?: "No vendor requirements yet", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!errorMessage.isNullOrBlank()) {
                            item { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                        }
                        item {
                            Text(
                                "Similar product needs are grouped into one card. Tap a vendor to open their requirement.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        items(
                            requirements,
                            key = { it.clusterKey?.ifBlank { it.title } ?: it.title }
                        ) { group ->
                            AdminRequirementGroupCard(
                                group = group,
                                onVendorClick = { vendor ->
                                    val id = vendor.requirementId
                                    if (id != null) onRequirementClick(id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRequirementGroupCard(
    group: AdminRequirementGroupData,
    onVendorClick: (AdminRequirementVendorData) -> Unit
) {
    val title = group.canonicalTitle?.ifBlank { group.title } ?: group.title
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!group.category.isNullOrBlank()) {
                    AssistChip(onClick = {}, label = { Text(group.category) }, enabled = false)
                }
                if (group.quantity != null) {
                    AssistChip(onClick = {}, label = { Text("Qty ${group.quantity}") }, enabled = false)
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
                Spacer(Modifier.height(8.dp))
                Text(group.description, color = TextSecondary, fontSize = 13.sp, maxLines = 6)
            }
            Spacer(Modifier.height(10.dp))
            Text("Vendors looking for this", fontWeight = FontWeight.SemiBold, color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            group.vendors.forEach { vendor ->
                val active = vendor.status.equals("active", ignoreCase = true)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onVendorClick(vendor) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    AdminRequirementVendorLogo(logoUrl = vendor.logoUrl, size = 36.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(vendor.shopName, color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        val subtitle = buildString {
                            vendor.quantity?.let { append("Qty $it") }
                            if (isNotEmpty()) append(" · ")
                            append(if (active) "Active" else "Closed")
                        }
                        Text(subtitle, color = TextMuted, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminRequirementVendorLogo(logoUrl: String?, size: androidx.compose.ui.unit.Dp) {
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
            Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(size * 0.45f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequirementDetailScreen(
    requirementId: Int,
    viewModel: AdminRequirementsViewModel,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val detail by viewModel.detail.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(requirementId) { viewModel.load(requirementId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requirement", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        val item = detail
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: if (isLoading) "Loading..." else "Requirement not found", color = TextMuted)
            }
        } else {
            val active = item.status.equals("active", ignoreCase = true)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(item.canonicalTitle?.ifBlank { item.title } ?: item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(if (active) "Active" else "Closed") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (active) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.12f),
                        labelColor = if (active) SuccessGreen else ErrorRed
                    )
                )
                Spacer(Modifier.height(16.dp))
                DetailRow("Vendor", item.vendorName)
                if (!item.vendorUsername.isNullOrBlank()) DetailRow("Username", "@${item.vendorUsername}")
                if (!item.vendorMobile.isNullOrBlank()) DetailRow("Mobile", item.vendorMobile)
                DetailRow("Category", item.category ?: "—")
                DetailRow("Quantity", item.quantity?.toString() ?: "—")
                DetailRow("Posted", item.createdAt ?: "—")
                DetailRow("Last update", item.updatedAt ?: "—")
                Text("Details", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Text(item.description?.ifBlank { "No extra notes" } ?: "No extra notes", modifier = Modifier.padding(16.dp))
                }
                if (!item.vendorMobile.isNullOrBlank()) {
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { makeCall(item.vendorMobile) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Call vendor")
                    }
                }
                if (active) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.close(item.id) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close requirement", fontWeight = FontWeight.Bold)
                    }
                }
                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorMessage!!, color = ErrorRed)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    Text(value, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
}
