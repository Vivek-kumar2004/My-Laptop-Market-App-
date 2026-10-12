package com.mylaptopmarket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylaptopmarket.network.SessionManager
import com.mylaptopmarket.repository.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MessagesViewModel(private val repository: ApiMessageRepository) : ViewModel() {
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var chatPollJob: Job? = null
    private var conversationPollJob: Job? = null

    fun fetchConversations() {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            refreshConversations(vendorId)
            _isLoading.value = false
        }
    }

    fun refreshUnread() {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        viewModelScope.launch {
            refreshConversations(vendorId)
        }
    }

    fun startConversationPolling() {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        conversationPollJob?.cancel()
        conversationPollJob = viewModelScope.launch {
            while (isActive) {
                refreshConversations(vendorId)
                delay(3000)
            }
        }
    }

    fun stopConversationPolling() {
        conversationPollJob?.cancel()
        conversationPollJob = null
    }

    fun fetchChat(contactId: Int) {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        viewModelScope.launch {
            refreshChat(vendorId, contactId)
        }
    }

    fun startChatPolling(contactId: Int) {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        chatPollJob?.cancel()
        chatPollJob = viewModelScope.launch {
            while (isActive) {
                refreshChat(vendorId, contactId)
                delay(2000)
            }
        }
    }

    fun stopChatPolling() {
        chatPollJob?.cancel()
        chatPollJob = null
    }

    fun sendMessage(contactId: Int, text: String) {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val request = SendMessageRequest(vendorId, contactId, text)
            val result = repository.sendMessage(request)
            result.onSuccess { newMsg ->
                if (_chatMessages.value.none { it.id == newMsg.id }) {
                    _chatMessages.value = _chatMessages.value + newMsg
                }
                refreshConversations(vendorId)
            }
        }
    }

    fun fetchContacts() {
        val vendorId = SessionManager.currentSession.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getContacts(vendorId)
            result.onSuccess { _contacts.value = it }
            _isLoading.value = false
        }
    }

    private suspend fun refreshChat(vendorId: Int, contactId: Int) {
        repository.getChat(vendorId, contactId).onSuccess { incoming ->
            if (incoming != _chatMessages.value) {
                _chatMessages.value = incoming
            }
        }
    }

    private suspend fun refreshConversations(vendorId: Int) {
        repository.getConversations(vendorId).onSuccess { incoming ->
            if (incoming != _conversations.value) {
                _conversations.value = incoming
            }
        }
    }
}
