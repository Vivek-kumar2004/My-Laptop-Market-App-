package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylaptopmarket.network.AdminSessionManager
import com.mylaptopmarket.ui.theme.BackgroundLight
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SuccessGreen
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.TextSecondary
import com.mylaptopmarket.ui.theme.WarningOrange
import com.mylaptopmarket.viewmodel.AdminDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onActiveVendorsClick: () -> Unit,
    onInactiveVendorsClick: () -> Unit,
    onInquiriesClick: () -> Unit,
    onInvoicesClick: () -> Unit,
    onProductsClick: () -> Unit,
    onRequirementsClick: () -> Unit,
    onChatsClick: () -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val data by viewModel.data.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val session by AdminSessionManager.currentSession.collectAsState()
    val greetingName = session?.username?.takeIf { it.isNotBlank() } ?: "Admin"
    val currentNotice by viewModel.announcement.collectAsState()
    val announceStatus by viewModel.announceStatus.collectAsState()
    var showNoticeDialog by remember { mutableStateOf(false) }
    var noticeText by remember { mutableStateOf("") }
    var noticeReady by remember { mutableStateOf(false) }
    var noticeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(showNoticeDialog) {
        if (showNoticeDialog) {
            noticeReady = false
            noticeError = null
            viewModel.clearAnnounceStatus()
            viewModel.loadAnnouncement()
            kotlinx.coroutines.delay(350)
            noticeReady = true
        }
    }
    LaunchedEffect(currentNotice, showNoticeDialog) {
        if (showNoticeDialog) noticeText = currentNotice
    }

    Scaffold(
        containerColor = BackgroundLight,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Panel", fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            "Hi, $greetingName",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
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
            if (data == null && isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val stats = data
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    item {
                        AdminStatCard("Active vendors", stats?.activeVendors ?: 0, Icons.Default.Store, SuccessGreen, onActiveVendorsClick)
                    }
                    item {
                        AdminStatCard("Inactive vendors", stats?.inactiveVendors ?: 0, Icons.Default.PersonOff, ErrorRed, onInactiveVendorsClick)
                    }
                    item {
                        AdminStatCard("New inquiries", stats?.newInquiries ?: 0, Icons.Default.Mail, WarningOrange, onInquiriesClick)
                    }
                    item {
                        AdminStatCard("Invoices", stats?.totalInvoices ?: 0, Icons.Default.Description, PrimaryBlue, onInvoicesClick)
                    }
                    item {
                        AdminStatCard("Products", stats?.totalProducts ?: 0, Icons.Default.Inventory2, Color(0xFF7C3AED), onProductsClick)
                    }
                    item {
                        AdminStatCard(
                            "Requirements",
                            stats?.activeRequirements ?: 0,
                            Icons.Default.Assignment,
                            Color(0xFFDB2777),
                            onRequirementsClick
                        )
                    }
                    item(span = { GridItemSpan(2) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChatsClick() },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF0369A1))
                                Text("Vendor chats", fontWeight = FontWeight.Bold, color = TextSecondary)
                                Text(
                                    "Read messages between shops",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        AdminStatCard("All inquiries", stats?.totalInquiries ?: 0, Icons.Default.Mail, Color(0xFF0D9488), onInquiriesClick)
                    }
                    item(span = { GridItemSpan(2) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNoticeDialog = true },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFEA580C))
                                Text("Notify all vendors", fontWeight = FontWeight.Bold, color = TextSecondary)
                                Text(
                                    if (currentNotice.isBlank()) "Send a popup to every vendor home screen" else "A notification is live. Tap to change it.",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                if (showNoticeDialog) {
                    AlertDialog(
                        onDismissRequest = { showNoticeDialog = false },
                        title = { Text("Message for every vendor", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "This pops up on every vendor's home screen.",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                                OutlinedTextField(
                                    value = noticeText,
                                    onValueChange = {
                                        noticeText = it.take(500)
                                        noticeError = null
                                    },
                                    label = { Text("Notification") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                                val status = noticeError ?: announceStatus
                                if (!status.isNullOrBlank()) {
                                    Text(status, color = if (noticeError != null) ErrorRed else SuccessGreen)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                enabled = noticeReady,
                                onClick = {
                                    if (noticeText.isBlank()) {
                                        noticeError = "Write the notification first."
                                    } else {
                                        viewModel.sendAnnouncement(noticeText)
                                    }
                                }
                            ) { Text("Send") }
                        },
                        dismissButton = {
                            Column {
                                if (currentNotice.isNotBlank()) {
                                    TextButton(
                                        enabled = noticeReady,
                                        onClick = {
                                            noticeText = ""
                                            viewModel.sendAnnouncement("")
                                        }
                                    ) { Text("Clear", color = ErrorRed) }
                                }
                                TextButton(onClick = { showNoticeDialog = false }) { Text("Close") }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: Int,
    icon: ImageVector,
    accent: Color,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 148.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Text(
                "$value",
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1
            )
            Text(
                title,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
