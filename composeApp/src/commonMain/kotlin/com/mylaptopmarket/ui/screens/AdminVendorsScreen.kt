package com.mylaptopmarket.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.repository.AdminVendorData
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SuccessGreen
import com.mylaptopmarket.ui.theme.SuccessGreenLight
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.viewmodel.AdminVendorFilter
import com.mylaptopmarket.viewmodel.AdminVendorsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVendorsScreen(
    viewModel: AdminVendorsViewModel,
    onAddClick: () -> Unit,
    onVendorClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val allVendors by viewModel.vendors.collectAsState()
    val filter by viewModel.filter.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val vendors = when (filter) {
        AdminVendorFilter.ALL -> allVendors
        AdminVendorFilter.ACTIVE -> allVendors.filter { it.status == 1 }
        AdminVendorFilter.INACTIVE -> allVendors.filter { it.status != 1 }
    }.filter { vendor ->
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            true
        } else {
            listOf(vendor.shopName, vendor.username, vendor.mobile, vendor.location)
                .any { it?.contains(query, ignoreCase = true) == true }
        }
    }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    var pendingDelete by remember { mutableStateOf<AdminVendorData?>(null) }
    var pendingWarn by remember { mutableStateOf<AdminVendorData?>(null) }
    var warnText by remember { mutableStateOf("") }
    var warnError by remember { mutableStateOf<String?>(null) }
    var warnReady by remember { mutableStateOf(false) }
    var showPasswordId by remember { mutableStateOf<Int?>(null) }
    var searchOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(pendingWarn?.id) {
        warnReady = false
        if (pendingWarn != null) {
            delay(350)
            warnReady = true
        }
    }

    statusMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearStatus() },
            confirmButton = { TextButton(onClick = { viewModel.clearStatus() }) { Text("OK") } },
            title = { Text("Done") },
            text = { Text(msg) }
        )
    }

    pendingDelete?.let { vendor ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(vendor.id)
                    pendingDelete = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
            title = { Text("Delete vendor?") },
            text = { Text("This will also delete ${vendor.shopName ?: vendor.username}'s products, invoices and messages.") }
        )
    }

    pendingWarn?.let { vendor ->
        AlertDialog(
            onDismissRequest = { pendingWarn = null },
            confirmButton = {
                TextButton(
                    enabled = warnReady,
                    onClick = {
                        val message = warnText.trim()
                        if (message.isEmpty()) {
                            warnError = "Write the warning first."
                            return@TextButton
                        }
                        viewModel.warn(vendor.id, message)
                        pendingWarn = null
                    }
                ) { Text("Send") }
            },
            dismissButton = {
                Row {
                    if (warnReady && !vendor.adminWarning.isNullOrBlank()) {
                        TextButton(onClick = {
                            viewModel.warn(vendor.id, "")
                            pendingWarn = null
                        }) { Text("Clear") }
                    }
                    TextButton(onClick = { pendingWarn = null }) { Text("Cancel") }
                }
            },
            title = { Text("Warn ${vendor.shopName ?: vendor.username}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = warnText,
                        onValueChange = {
                            warnText = it.take(500)
                            warnError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Warning") },
                        minLines = 3
                    )
                    if (!warnError.isNullOrBlank()) {
                        Text(warnError!!, color = ErrorRed)
                    }
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Shop, username, mobile") },
                            singleLine = true
                        )
                    } else {
                        Text(filter.title, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (filter != AdminVendorFilter.ALL) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (searchOpen) {
                            searchQuery = ""
                        }
                        searchOpen = !searchOpen
                    }) {
                        Icon(
                            if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search vendors"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add vendor")
            }
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
            if (vendors.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        errorMessage ?: if (searchQuery.isNotBlank()) "No matching vendors" else filter.emptyMessage,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        item { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                    }
                    items(vendors, key = { it.id }) { vendor ->
                        AdminVendorCard(
                            vendor = vendor,
                            showPassword = showPasswordId == vendor.id,
                            onOpen = { onVendorClick(vendor.id) },
                            onTogglePassword = {
                                showPasswordId = if (showPasswordId == vendor.id) null else vendor.id
                            },
                            onActivate = { viewModel.activate(vendor.id) },
                            onDeactivate = { viewModel.deactivate(vendor.id) },
                            onEdit = { onEditClick(vendor.id) },
                            onWarn = {
                                warnText = vendor.adminWarning.orEmpty()
                                warnError = null
                                pendingWarn = vendor
                            },
                            onDelete = { pendingDelete = vendor }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AdminVendorCard(
    vendor: AdminVendorData,
    showPassword: Boolean,
    onOpen: () -> Unit,
    onTogglePassword: () -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onEdit: () -> Unit,
    onWarn: () -> Unit,
    onDelete: () -> Unit
) {
    val active = vendor.status == 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(vendor.shopName ?: vendor.username, fontWeight = FontWeight.Bold)
                    Text("@${vendor.username}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Text("Tap to view profile", style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(if (active) "Active" else "Inactive") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (active) SuccessGreenLight else ErrorRed.copy(alpha = 0.12f),
                        labelColor = if (active) SuccessGreen else ErrorRed
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Mobile: ${vendor.mobile ?: "-"}", style = MaterialTheme.typography.bodySmall)
            Text("Sub: ${vendor.subscriptionEnd ?: "—"}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!vendor.passwordPlain.isNullOrBlank()) {
                    Text(
                        "Password: ${if (showPassword) vendor.passwordPlain else "••••••••"}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                } else {
                    Text(
                        "Password: not saved — tap Edit to set",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                if (active) {
                    OutlinedButton(onClick = onDeactivate, modifier = Modifier.weight(1f)) { Text("Deactivate") }
                } else {
                    OutlinedButton(onClick = onActivate, modifier = Modifier.weight(1f)) { Text("Activate") }
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onWarn) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warn",
                        tint = if (vendor.adminWarning.isNullOrBlank()) TextMuted else ErrorRed
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed) }
            }
        }
    }
}
