package com.example.data.repository

import android.content.Context
import com.example.data.api.OliApiClient
import com.example.data.database.OliDatabase
import com.example.data.database.entities.ChatEntity
import com.example.data.database.entities.MessageEntity
import com.example.data.models.ChatMessage
import com.example.data.models.ChatRequest
import com.example.security.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val database: OliDatabase,
    private val apiClient: OliApiClient,
    private val connectionRepository: ConnectionRepository,
    private val keystoreManager: KeystoreManager
) {
    val allChats: Flow<List<ChatEntity>> = database.chatDao().getAllChats()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> =
        database.messageDao().getMessagesForChat(chatId)

    suspend fun createNewChat(initialTitle: String = "New Chat"): ChatEntity {
        val chat = ChatEntity(
            id = UUID.randomUUID().toString(),
            title = initialTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        database.chatDao().insertChat(chat)
        return chat
    }

    suspend fun renameChat(chatId: String, newTitle: String) {
        val chat = database.chatDao().getChatById(chatId)
        if (chat != null) {
            database.chatDao().updateChat(chat.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteChat(chatId: String) {
        database.chatDao().deleteChatById(chatId)
    }

    suspend fun saveUserMessage(chatId: String, content: String, attachments: String = ""): MessageEntity {
        val userMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            role = "user",
            content = content,
            createdAt = System.currentTimeMillis(),
            attachments = attachments
        )
        database.messageDao().insertMessage(userMsg)

        // Update chat updatedAt
        val chat = database.chatDao().getChatById(chatId)
        if (chat != null) {
            val title = if (chat.title == "New Chat" && content.isNotBlank()) {
                content.take(30).trim()
            } else {
                chat.title
            }
            database.chatDao().updateChat(chat.copy(title = title, updatedAt = System.currentTimeMillis()))
        }

        return userMsg
    }

    suspend fun saveAssistantMessage(chatId: String, content: String): MessageEntity {
        val assistantMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            role = "assistant",
            content = content,
            createdAt = System.currentTimeMillis()
        )
        database.messageDao().insertMessage(assistantMsg)

        val chat = database.chatDao().getChatById(chatId)
        if (chat != null) {
            database.chatDao().updateChat(chat.copy(updatedAt = System.currentTimeMillis()))
        }

        return assistantMsg
    }

    fun streamChatResponse(chatId: String, userMessage: String): Flow<String> = flow {
        val currentServer = connectionRepository.currentServer.value
        val token = currentServer?.let { keystoreManager.getDecryptedToken("session_${it.id}") }
        val authHeader = if (token != null) "Bearer $token" else null

        var receivedText = ""
        try {
            val history = database.messageDao().getMessagesForChatList(chatId).map {
                ChatMessage(role = it.role, content = it.content)
            }

            val request = ChatRequest(
                messages = history + ChatMessage(role = "user", content = userMessage),
                stream = true
            )

            val response = apiClient.getService().streamChat(authHeader, request)
            if (response.isSuccessful && response.body() != null) {
                val source = response.body()!!.source()
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.isNotBlank()) {
                        // Extract delta content
                        val chunk = if (line.startsWith("data:")) line.removePrefix("data:").trim() else line
                        if (chunk != "[DONE]") {
                            receivedText += chunk
                            emit(chunk)
                        }
                    }
                }
            } else {
                // Regular fallback response
                val regular = apiClient.getService().sendChat(authHeader, request.copy(stream = false))
                if (regular.isSuccessful && regular.body() != null) {
                    val reply = regular.body()!!.response
                    receivedText = reply
                    emit(reply)
                } else {
                    throw IllegalStateException("Server returned ${regular.code()}")
                }
            }
        } catch (e: Exception) {
            val errorMessage = "Network Error: ${e.message ?: "Unable to connect to OLI Server."}"
            receivedText = errorMessage
            emit(errorMessage)
        }

        // Persist final assistant response to Room database
        if (receivedText.isNotBlank()) {
            saveAssistantMessage(chatId, receivedText.trim())
        }
    }.flowOn(Dispatchers.IO)
}
