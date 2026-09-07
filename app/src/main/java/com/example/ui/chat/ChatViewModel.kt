package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entities.ChatEntity
import com.example.data.database.entities.MessageEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.ConnectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    val allChats: StateFlow<List<ChatEntity>> = chatRepository.allChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isServerOnline: StateFlow<Boolean> = connectionRepository.isServerOnline

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private var activeStreamJob: Job? = null
    private var messageCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            allChats.collect { chats ->
                if (_currentChatId.value == null && chats.isNotEmpty()) {
                    selectChat(chats.first().id)
                } else if (_currentChatId.value == null && chats.isEmpty()) {
                    val newChat = chatRepository.createNewChat("New Chat")
                    selectChat(newChat.id)
                }
            }
        }
    }

    fun selectChat(chatId: String) {
        _currentChatId.value = chatId
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            chatRepository.getMessagesForChat(chatId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun startNewChat() {
        viewModelScope.launch {
            val chat = chatRepository.createNewChat("New Chat")
            selectChat(chat.id)
        }
    }

    fun renameChat(chatId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.renameChat(chatId, newTitle)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
            if (_currentChatId.value == chatId) {
                _currentChatId.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun sendMessage(content: String, attachments: String = "") {
        if (content.isBlank() && attachments.isBlank()) return
        val chatId = _currentChatId.value ?: return

        viewModelScope.launch {
            chatRepository.saveUserMessage(chatId, content, attachments)
            _isGenerating.value = true
            _streamingText.value = ""

            activeStreamJob = launch {
                try {
                    chatRepository.streamChatResponse(chatId, content).collect { chunk ->
                        _streamingText.value += chunk
                    }
                } catch (e: Exception) {
                    // Handled inside repo
                } finally {
                    _isGenerating.value = false
                    _streamingText.value = ""
                }
            }
        }
    }

    fun stopGeneration() {
        activeStreamJob?.cancel()
        _isGenerating.value = false
        val chatId = _currentChatId.value
        val partial = _streamingText.value
        if (chatId != null && partial.isNotBlank()) {
            viewModelScope.launch {
                chatRepository.saveAssistantMessage(chatId, "$partial\n[Stopped by user]")
                _streamingText.value = ""
            }
        } else {
            _streamingText.value = ""
        }
    }
}
