package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String, // "user" or "assistant" or "system"
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attachments: String = "" // serialized URI or comma-separated list
)

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val address: String, // e.g. http://192.168.1.50:8080 or tailscale IP
    val accessMode: String, // "Private", "Invite Only", "Public"
    val isConnected: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val isLocalCompanion: Boolean = false
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val key: String,
    val category: String, // "user", "interests", "projects", "preferences"
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
