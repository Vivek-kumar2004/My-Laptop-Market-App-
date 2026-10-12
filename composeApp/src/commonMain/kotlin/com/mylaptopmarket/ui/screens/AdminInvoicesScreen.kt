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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.util.openUrl
import com.mylaptopmarket.viewmodel.AdminInvoicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInvoicesScreen(
    viewModel: AdminInvoicesViewModel,
    onInvoiceClick: (Int) -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Invoices", fontWeight = FontWeight.Bold) },
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
            if (invoices.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "No invoices found", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        item { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                    }
                    items(invoices, key = { it.id }) { invoice ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInvoiceClick(invoice.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(invoice.invoiceNumber ?: "Invoice #${invoice.id}", fontWeight = FontWeight.Bold)
                                    Text(invoice.vendorName ?: "Vendor", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "Rs ${invoice.amount}",
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(invoice.createdAt ?: "", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "View invoice",
                                    tint = TextMuted
                                )
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
fun AdminInvoiceDetailScreen(
    invoiceId: Int,
    viewModel: AdminInvoicesViewModel,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val detail by viewModel.detail.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(invoiceId) { viewModel.load(invoiceId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        val invoice = detail
        if (invoice == null) {
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
                Text(invoice.invoiceNumber ?: "Invoice #${invoice.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text("Rs ${invoice.amount}", color = PrimaryBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                InvoiceRow("Vendor", invoice.vendorName ?: "—")
                InvoiceRow("Date", invoice.createdAt ?: "—")
                InvoiceRow("Status", invoice.status ?: "paid")
                InvoiceRow("Mobile", invoice.vendorMobile ?: "—")
                InvoiceRow("GSTIN", invoice.vendorGstin ?: "—")
                InvoiceRow("Payment ID", invoice.razorpayPaymentId ?: "—")
                if (invoice.subtotal != null) InvoiceRow("Subtotal", "Rs ${invoice.subtotal}")
                if (invoice.cgst != null && invoice.cgst > 0) InvoiceRow("CGST", "Rs ${invoice.cgst}")
                if (invoice.sgst != null && invoice.sgst > 0) InvoiceRow("SGST", "Rs ${invoice.sgst}")
                if (invoice.igst != null && invoice.igst > 0) InvoiceRow("IGST", "Rs ${invoice.igst}")
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { openUrl(viewModel.htmlUrl(invoice.id)) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View tax invoice", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(label: String, value: String) {
    Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    Text(value, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
}
