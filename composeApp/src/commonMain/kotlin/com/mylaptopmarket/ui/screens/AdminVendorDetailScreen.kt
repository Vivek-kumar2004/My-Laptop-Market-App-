package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import coil3.compose.AsyncImage
import com.mylaptopmarket.repository.AdminVendorData
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.PrimaryLight
import com.mylaptopmarket.ui.theme.SuccessGreen
import com.mylaptopmarket.ui.theme.SuccessGreenLight
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.TextPrimary
import com.mylaptopmarket.util.makeCall
import com.mylaptopmarket.viewmodel.AdminVendorDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVendorDetailScreen(
    vendorId: Int,
    viewModel: AdminVendorDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val vendor by viewModel.vendor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(vendorId) {
        viewModel.load(vendorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (vendor != null) {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit vendor")
                        }
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        when {
            vendor == null && isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            vendor == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "Vendor not found", color = TextMuted)
                }
            }
            else -> {
                VendorProfileBody(
                    vendor = vendor!!,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword },
                    onEditClick = onEditClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
private fun VendorProfileBody(
    vendor: AdminVendorData,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = vendor.status == 1
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!vendor.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = vendor.logoUrl,
                    contentDescription = vendor.shopName,
                    modifier = Modifier.size(72.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    vendor.shopName?.ifBlank { null } ?: vendor.username,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text("@${vendor.username}", color = TextMuted)
                Spacer(Modifier.height(6.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(if (active) "Active" else "Inactive") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (active) SuccessGreenLight else ErrorRed.copy(alpha = 0.12f),
                        labelColor = if (active) SuccessGreen else ErrorRed
                    )
                )
            }
        }

        ProfileRow("Mobile", vendor.mobile)
        ProfileRow("Email", vendor.email)
        ProfileRow("Contact person", vendor.contactPerson)
        ProfileRow("Location", vendor.location)
        ProfileRow("State", listOfNotNull(vendor.state, vendor.stateCode?.let { "($it)" }).joinToString(" ").ifBlank { null })
        ProfileRow("Billing address", vendor.billingAddress)
        ProfileRow("GSTIN", vendor.gstin)
        ProfileRow("Subscription start", vendor.subscriptionStart)
        ProfileRow("Subscription end", vendor.subscriptionEnd)
        if (!vendor.adminWarning.isNullOrBlank()) {
            ProfileRow("Admin warning", vendor.adminWarning)
        }
        if (!vendor.passwordPlain.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    ProfileRow("Password", if (showPassword) vendor.passwordPlain else "••••••••")
                }
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password"
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        if (!vendor.mobile.isNullOrBlank()) {
            OutlinedButton(
                onClick = { makeCall(vendor.mobile) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Call vendor")
            }
        }
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Edit profile", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileRow(label: String, value: String?) {
    val shown = value?.trim().orEmpty().ifBlank { "—" }
    Text(label, fontWeight = FontWeight.Bold, color = TextPrimary)
    Text(shown, color = TextMuted)
}
