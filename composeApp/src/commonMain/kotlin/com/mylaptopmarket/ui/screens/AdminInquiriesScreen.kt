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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.repository.AdminInquiryData
import com.mylaptopmarket.ui.theme.ErrorRed
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.WarningOrange
import com.mylaptopmarket.viewmodel.AdminInquiriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInquiriesScreen(
    viewModel: AdminInquiriesViewModel,
    onInquiryClick: (Int) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val inquiries by viewModel.inquiries.collectAsState()
    val newCount by viewModel.newCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (newCount > 0) "Inquiries ($newCount new)" else "Inquiries",
                        fontWeight = FontWeight.Bold
                    )
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
            if (inquiries.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "No business inquiries yet", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        item { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                    }
                    items(inquiries, key = { it.id }) { inquiry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInquiryClick(inquiry.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(inquiry.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (inquiry.status == "new") {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("New") },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = WarningOrange.copy(alpha = 0.15f),
                                                labelColor = WarningOrange
                                            )
                                        )
                                    }
                                }
                                Text(inquiry.subject, style = MaterialTheme.typography.bodyMedium)
                                Text(inquiry.email, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(inquiry.createdAt ?: "", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInquiryDetailScreen(
    inquiryId: Int,
    viewModel: AdminInquiriesViewModel,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val detail by viewModel.detail.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    var confirmDelete by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(inquiryId) { viewModel.load(inquiryId) }
    LaunchedEffect(deleted) { if (deleted) onBackClick() }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(inquiryId)
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
            title = { Text("Delete inquiry?") },
            text = { Text("This cannot be undone.") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inquiry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        val inquiry = detail
        if (inquiry == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "Loading...", color = TextMuted)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                DetailRow("Name / Company", inquiry.name)
                DetailRow("Email", inquiry.email)
                DetailRow("Subject", inquiry.subject)
                DetailRow("Submitted", inquiry.createdAt ?: "")
                Text("Message", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Text(inquiry.message, modifier = Modifier.padding(16.dp))
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        val subject = inquiry.subject.replace(" ", "%20")
                        uriHandler.openUri("mailto:${inquiry.email}?subject=Re:%20$subject")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reply via Email")
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
