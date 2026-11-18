package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.SurfaceWhite
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.TextPrimary
import com.mylaptopmarket.viewmodel.AdminChatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatsScreen(
    viewModel: AdminChatsViewModel,
    onChatClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val threads by viewModel.threads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Vendor chats", fontWeight = FontWeight.Bold) },
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
            if (threads.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "No vendor chats yet", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        item { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
                    }
                    items(threads, key = { "${it.leftId}-${it.rightId}" }) { thread ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChatClick(thread.leftId, thread.rightId) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "${thread.leftName}  ·  ${thread.rightName}",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (thread.preview.isNotBlank()) {
                                    Text(
                                        thread.preview,
                                        color = TextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    "${thread.count} messages · ${thread.stamp}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
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
fun AdminChatDetailScreen(
    leftId: Int,
    rightId: Int,
    viewModel: AdminChatsViewModel,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val threads by viewModel.threads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val thread = threads.find { it.leftId == leftId && it.rightId == rightId }
    val title = thread?.let { "${it.leftName} and ${it.rightName}" } ?: "Vendor chat"

    LaunchedEffect(leftId, rightId) { viewModel.load(leftId, rightId) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
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
        when {
            messages.isEmpty() && isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Loading chat…", color = TextMuted)
                }
            }
            messages.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "No messages in this chat", color = TextMuted)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { "${it.senderId}-${it.createdAt}-${it.message.hashCode()}" }) { row ->
                        val mine = row.senderId == leftId
                        Column(Modifier.fillMaxWidth()) {
                            Text(
                                row.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (mine) PrimaryBlue else TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(row.message, color = TextPrimary)
                                    if (row.createdAt.isNotBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(row.createdAt, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
