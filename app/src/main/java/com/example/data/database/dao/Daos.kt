package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entities.ChatEntity
import com.example.data.database.entities.MemoryEntity
import com.example.data.database.entities.MessageEntity
import com.example.data.database.entities.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY updatedAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    suspend fun getChatById(id: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChatById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    suspend fun getMessagesForChatList(chatId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)
}

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY lastSeen DESC")
    fun getAllServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): ServerEntity?

    @Query("SELECT * FROM servers WHERE isConnected = 1 LIMIT 1")
    suspend fun getConnectedServer(): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity)

    @Update
    suspend fun updateServer(server: ServerEntity)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServerById(id: String)

    @Query("UPDATE servers SET isConnected = 0")
    suspend fun disconnectAll()

    @Query("UPDATE servers SET isConnected = 1 WHERE id = :id")
    suspend fun setConnected(id: String)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE category = :category")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>)
}
