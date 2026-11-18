package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mylaptopmarket.repository.Contact
import com.mylaptopmarket.repository.Conversation
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.ui.theme.PrimaryBlue
import com.mylaptopmarket.ui.theme.PrimaryLight
import com.mylaptopmarket.ui.theme.TextMuted
import com.mylaptopmarket.ui.theme.TextPrimary
import com.mylaptopmarket.ui.theme.TextSecondary
import com.mylaptopmarket.viewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    viewModel: MessagesViewModel,
    onChatClick: (Int, String) -> Unit,
    onBackClick: () -> Unit,
    onMainTabSelected: ((com.mylaptopmarket.ui.components.MainTab) -> Unit)? = null
) {
    val conversations by viewModel.conversations.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showNewChatSheet by remember { mutableStateOf(false) }
    var contactQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchConversations()
    }

    DisposableEffect(Unit) {
        viewModel.startConversationPolling()
        onDispose { viewModel.stopConversationPolling() }
    }

    LaunchedEffect(showNewChatSheet) {
        if (showNewChatSheet) {
            viewModel.fetchContacts()
        }
    }

    val filteredContacts = remember(contacts, contactQuery) {
        val q = contactQuery.trim()
        if (q.isEmpty()) contacts
        else contacts.filter {
            it.shop_name.orEmpty().contains(q, ignoreCase = true) ||
                it.username.contains(q, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", modifier = Modifier.tourTarget("page-messages"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (onMainTabSelected != null) {
                com.mylaptopmarket.ui.components.MainBottomBar(
                    selected = com.mylaptopmarket.ui.components.MainTab.Messages,
                    onTabSelected = onMainTabSelected
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    contactQuery = ""
                    showNewChatSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        }
    ) { padding ->
        when {
            isLoading && conversations.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            conversations.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No messages yet", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap + to start a chat with another vendor",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(conversations, key = { it.contact_id }) { conv ->
                        ConversationItem(
                            conv = conv,
                            onClick = { onChatClick(conv.contact_id, conv.contact_name) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                    }
                }
            }
        }
    }

    if (showNewChatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewChatSheet = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    "New Chat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Choose a vendor to message",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = contactQuery,
                    onValueChange = { contactQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search vendors") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading && contacts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    filteredContacts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (contacts.isEmpty()) "No vendors available"
                                else "No match for \"$contactQuery\"",
                                color = TextMuted
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                        ) {
                            items(filteredContacts, key = { it.id }) { contact ->
                                NewChatContactItem(
                                    contact = contact,
                                    onClick = {
                                        showNewChatSheet = false
                                        val name = contact.shop_name?.takeIf { it.isNotBlank() }
                                            ?: contact.username
                                        onChatClick(contact.id, name)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewChatContactItem(contact: Contact, onClick: () -> Unit) {
    val displayName = contact.shop_name?.takeIf { it.isNotBlank() } ?: contact.username
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "@${contact.username}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conv.contact_name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conv.last_message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (conv.unread_count > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = conv.unread_count.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
