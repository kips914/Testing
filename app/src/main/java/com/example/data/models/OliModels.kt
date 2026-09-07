package com.example.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String,
    @Json(name = "attachments") val attachments: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ChatRequest(
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "stream") val stream: Boolean = true,
    @Json(name = "temperature") val temperature: Float = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int = 2048,
    @Json(name = "model") val model: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "response") val response: String,
    @Json(name = "finish_reason") val finishReason: String? = null,
    @Json(name = "tokens_used") val tokensUsed: Int? = null
)

@JsonClass(generateAdapter = true)
data class StreamChunk(
    @Json(name = "delta") val delta: String,
    @Json(name = "done") val done: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PairRequest(
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "device_name") val deviceName: String,
    @Json(name = "platform") val platform: String = "Android",
    @Json(name = "invitation_token") val invitationToken: String,
    @Json(name = "password") val password: String? = null
)

@JsonClass(generateAdapter = true)
data class PairResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "session_token") val sessionToken: String? = null,
    @Json(name = "server_name") val serverName: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "permissions") val permissions: List<String> = listOf("CHAT", "MEMORY_READ")
)

@JsonClass(generateAdapter = true)
data class EngineStatusResponse(
    @Json(name = "status") val status: String, // "OFFLINE", "STARTING", "RUNNING", "ERROR"
    @Json(name = "gpu") val gpu: String? = null,
    @Json(name = "ram") val ram: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "uptime_seconds") val uptimeSeconds: Long? = null
)

@JsonClass(generateAdapter = true)
data class ServerInfoResponse(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "port") val port: Int = 8080,
    @Json(name = "status") val status: String, // "RUNNING", "STOPPED", "STARTING"
    @Json(name = "access_mode") val accessMode: String = "Private", // "Private", "Invite Only", "Public"
    @Json(name = "tunnel_status") val tunnelStatus: String = "CONNECTED",
    @Json(name = "devices_count") val devicesCount: Int = 1,
    @Json(name = "engine_status") val engineStatus: String = "RUNNING",
    @Json(name = "model_name") val modelName: String = "llama-3-8b-instruct.Q4_K_M.gguf",
    @Json(name = "password_protected") val passwordProtected: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ServerSettingsRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "port") val port: Int,
    @Json(name = "access_mode") val accessMode: String,
    @Json(name = "password_protected") val passwordProtected: Boolean,
    @Json(name = "password") val password: String? = null
)

@JsonClass(generateAdapter = true)
data class DeviceItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "platform") val platform: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "last_seen") val lastSeen: String,
    @Json(name = "status") val status: String, // "Active", "Revoked"
    @Json(name = "permissions") val permissions: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class InvitationItem(
    @Json(name = "id") val id: String,
    @Json(name = "token") val token: String,
    @Json(name = "expires_at") val expiresAt: String,
    @Json(name = "max_uses") val maxUses: Int = 1,
    @Json(name = "used_count") val usedCount: Int = 0,
    @Json(name = "status") val status: String, // "Active", "Used", "Revoked"
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateInvitationRequest(
    @Json(name = "max_uses") val maxUses: Int = 1,
    @Json(name = "validity_hours") val validityHours: Int = 24
)

@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status") val status: String, // "ok"
    @Json(name = "apiVersion") val apiVersion: String = "2.0.0",
    @Json(name = "serverVersion") val serverVersion: String = "2.0.0"
)

@JsonClass(generateAdapter = true)
data class CapabilitiesResponse(
    @Json(name = "chat") val chat: Boolean = true,
    @Json(name = "streaming") val streaming: Boolean = true,
    @Json(name = "memory") val memory: Boolean = true,
    @Json(name = "ocr") val ocr: Boolean = true,
    @Json(name = "settings") val settings: Boolean = true,
    @Json(name = "engineControl") val engineControl: Boolean = true,
    @Json(name = "files") val files: Boolean = true
)

@JsonClass(generateAdapter = true)
data class DiscoveryServerItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "address") val address: String,
    @Json(name = "status") val status: String = "ONLINE",
    @Json(name = "access_mode") val accessMode: String = "Public",
    @Json(name = "internal_ip") val internalIp: String? = null,
    @Json(name = "uptime") val uptime: String? = null,
    @Json(name = "current_load") val currentLoad: String? = null
)

@JsonClass(generateAdapter = true)
data class OliSettings(
    @Json(name = "quick_mode") val quickMode: Boolean = false,
    @Json(name = "performance") val performance: String = "Balanced",
    @Json(name = "model") val model: String = "llama-3-8b-instruct",
    @Json(name = "temperature") val temperature: Float = 0.7f,
    @Json(name = "context_window") val contextWindow: Int = 4096,
    @Json(name = "theme") val theme: String = "Dark",
    @Json(name = "language") val language: String = "System"
)

@JsonClass(generateAdapter = true)
data class OliMemoryData(
    @Json(name = "user_name") val userName: String = "User",
    @Json(name = "user_age") val userAge: String = "",
    @Json(name = "user_language") val userLanguage: String = "ru, en",
    @Json(name = "interests") val interests: List<String> = listOf("Artificial Intelligence", "Android Development", "Architecture"),
    @Json(name = "projects") val projects: List<String> = listOf("OLI Ecosystem", "Local LLM Inference"),
    @Json(name = "preferences") val preferences: List<String> = listOf("Concise technical answers", "Dark mode aesthetic", "Security first")
)

@JsonClass(generateAdapter = true)
data class ActionResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)
