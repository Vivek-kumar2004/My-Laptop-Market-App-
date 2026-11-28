package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylaptopmarket.model.StockSheet
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ProductRepository
import com.mylaptopmarket.util.NetworkStatus
import com.mylaptopmarket.util.OfflineSync
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSheetScreen(
    productId: String,
    repository: ProductRepository,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val session = SessionManager.currentSession.value
    val online by NetworkStatus.online.collectAsState()
    val catalogRevision by OfflineSync.revision.collectAsState()
    var sheet by remember { mutableStateOf<StockSheet?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(session != null) }

    LaunchedEffect(productId, session?.id, catalogRevision) {
        val vendorId = session?.id
        if (vendorId == null) {
            loading = false
            return@LaunchedEffect
        }
        loading = true
        repository.getStockSheet(vendorId, productId)
            .onSuccess {
                sheet = it
                error = null
            }
            .onFailure {
                error = it.message ?: "Could not open stock sheet"
            }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock sheet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            session == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue)
                    Spacer(Modifier.height(12.dp))
                    Text("Login required", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Stock sheet rows are visible only to logged-in vendors.",
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onLoginClick) { Text("Login as Vendor") }
                }
            }
            loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(error ?: "", color = TextMuted, textAlign = TextAlign.Center)
                }
            }
            sheet != null -> {
                val data = sheet!!
                Box(Modifier.fillMaxSize().padding(padding)) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
                        Text(
                            "My Laptop Market",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = PrimaryBlue
                        )
                        Spacer(Modifier.height(6.dp))
                        if (!online) {
                            Text(
                                "Saved stock sheet. It updates when the internet is back.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        if (data.originalName.isNotBlank()) {
                            Text(data.originalName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (data.truncated) {
                            Text(
                                "Showing the first rows and columns only.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        StockSheetTable(data)
                        Spacer(Modifier.height(48.dp))
                    }
                    Text(
                        "My Laptop Market",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer { rotationZ = -28f; alpha = 0.22f },
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StockSheetTable(sheet: StockSheet) {
    val hScroll = rememberScrollState()
    Column(Modifier.horizontalScroll(hScroll).border(1.dp, Color(0xFFE2E8F0))) {
        Row(Modifier.background(Color(0xFFF1F5F9))) {
            sheet.headers.forEach { header ->
                Text(
                    header,
                    modifier = Modifier.width(140.dp).padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        sheet.rows.forEachIndexed { index, row ->
            Row(Modifier.background(if (index % 2 == 0) Color.White else Color(0xFFF8FAFC))) {
                row.forEach { cell ->
                    Text(
                        cell,
                        modifier = Modifier.width(140.dp).padding(8.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
        if (sheet.rows.isEmpty()) {
            Text("This sheet has no data rows.", modifier = Modifier.padding(12.dp), color = TextMuted)
        }
    }
}
