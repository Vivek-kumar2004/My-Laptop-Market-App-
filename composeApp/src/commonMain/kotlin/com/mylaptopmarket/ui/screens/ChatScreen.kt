package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.ChatMessage
import com.mylaptopmarket.repository.ProductRepository
import com.mylaptopmarket.util.ProductMessageContext
import com.mylaptopmarket.viewmodel.MessagesViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactId: Int,
    contactName: String,
    viewModel: MessagesViewModel,
    onBackClick: () -> Unit,
    productId: String? = null,
    productRepository: ProductRepository? = null
) {
    val messages by viewModel.chatMessages.collectAsState()
    var inputText by remember(contactId, productId) { mutableStateOf("") }
    var productContext by remember(contactId, productId) { mutableStateOf<Product?>(null) }
    val listState = rememberLazyListState()
    val myVendorId = SessionManager.currentSession.value?.id

    LaunchedEffect(contactId, productId) {
        viewModel.fetchChat(contactId)
        val id = productId
        if (!id.isNullOrBlank() && productRepository != null) {
            val product = productRepository.getProductById(id).first()
            productContext = product
            if (product != null && inputText.isBlank()) {
                inputText = ProductMessageContext.draft(product) + "\n"
            }
        }
    }

    DisposableEffect(contactId) {
        viewModel.startChatPolling(contactId)
        onDispose { viewModel.stopChatPolling() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                productContext?.let { product ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "PRODUCT ENQUIRY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        minLines = if (productContext != null) 4 else 1,
                        maxLines = 6
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val typed = inputText.trim()
                            if (typed.isBlank()) return@IconButton
                            val product = productContext
                            val toSend = if (
                                product != null &&
                                !ProductMessageContext.containsProduct(typed, product.id)
                            ) {
                                ProductMessageContext.draft(product) + "\n\n" + typed
                            } else {
                                typed
                            }
                            viewModel.sendMessage(contactId, toSend)
                            inputText = ""
                            productContext = null
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.sender_id == myVendorId
                ChatBubble(msg = msg, isMe = isMe)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage, isMe: Boolean) {
    val isContext = msg.message.contains("Regarding product:")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = when {
                isMe && isContext -> MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                isMe -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = msg.message,
                modifier = Modifier.padding(12.dp),
                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
