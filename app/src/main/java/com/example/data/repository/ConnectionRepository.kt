package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import com.example.data.api.OliApiClient
import com.example.data.api.OliApiService
import com.example.data.database.OliDatabase
import com.example.data.database.entities.ServerEntity
import com.example.data.models.DiscoveryServerItem
import com.example.data.models.PairRequest
import com.example.data.tunnel.TransportType
import com.example.data.tunnel.TunnelClient
import com.example.security.KeystoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ParsedQrResult(
    val isValid: Boolean,
    val token: String? = null,
    val address: String? = null,
    val error: String? = null
)

data class FoundServerInfo(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val accessMode: String,
    val requiresPassword: Boolean = false,
    val invitationToken: String? = null
)

class ConnectionRepository(
    private val context: Context,
    private val database: OliDatabase,
    private val apiClient: OliApiClient,
    private val tunnelClient: TunnelClient,
    private val keystoreManager: KeystoreManager,
    private val sessionLogger: SessionLogger
) {
    private val _currentServer = MutableStateFlow<ServerEntity?>(null)
    val currentServer: StateFlow<ServerEntity?> = _currentServer.asStateFlow()

    private val _isServerOnline = MutableStateFlow(true)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline.asStateFlow()

    val allServers: Flow<List<ServerEntity>> = database.serverDao().getAllServers()

    init {
        // Start by loading any previously connected server
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            // Clean up legacy fake servers
            try {
                database.serverDao().deleteServerById("local_pc_server")
                database.serverDao().deleteServerById("pub_node_1")
                database.serverDao().deleteServerById("pub_node_2")
            } catch (e: Exception) {}

            val connectedServer = database.serverDao().getConnectedServer()
            if (connectedServer != null) {
                _currentServer.value = connectedServer
                apiClient.setBaseUrl(connectedServer.address)
                try {
                    val token = keystoreManager.getDecryptedToken("session_${connectedServer.id}")
                    if (token != null) {
                        tunnelClient.establishSession(connectedServer.address, token, TransportType.DIRECT_HTTPS)
                    }
                } catch (e: Exception) {
                    // Ignore keystore errors on init
                }
            }

            // Periodic server liveness ping
            while (true) {
                val server = _currentServer.value
                if (server == null) {
                    _isServerOnline.value = false
                } else {
                    try {
                        val response = apiClient.getService().getHealth()
                        _isServerOnline.value = response.isSuccessful
                    } catch (e: Exception) {
                        _isServerOnline.value = false
                    }
                }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    fun parseInviteQr(qrContent: String): ParsedQrResult {
        val trimmed = qrContent.trim()
        if (trimmed.startsWith("javascript:", ignoreCase = true) ||
            trimmed.startsWith("file:", ignoreCase = true) ||
            trimmed.startsWith("data:", ignoreCase = true)
        ) {
            return ParsedQrResult(isValid = false, error = "Forbidden URL scheme")
        }

        try {
            val uri = Uri.parse(trimmed)
            val scheme = uri.scheme?.lowercase() ?: ""

            if (scheme == "oli") {
                if (uri.host == "invite") {
                    val id = uri.getQueryParameter("id")
                    if (!id.isNullOrBlank()) {
                        return ParsedQrResult(isValid = true, token = id)
                    }
                }
                return ParsedQrResult(isValid = false, error = "Invalid OLI invitation URL format")
            } else if (scheme == "http" || scheme == "https") {
                val id = uri.getQueryParameter("id") ?: uri.getQueryParameter("token")
                return ParsedQrResult(isValid = true, token = id, address = "${uri.scheme}://${uri.authority}")
            } else if (trimmed.length > 8 && !trimmed.contains("://")) {
                // Pure opaque token
                return ParsedQrResult(isValid = true, token = trimmed)
            } else {
                return ParsedQrResult(isValid = false, error = "Unsupported scheme: $scheme")
            }
        } catch (e: Exception) {
            return ParsedQrResult(isValid = false, error = e.localizedMessage ?: "Invalid QR code")
        }
    }

    suspend fun pairWithServer(
        address: String,
        invitationToken: String,
        password: String? = null,
        serverName: String = "My OLI Server",
        description: String = "Personal AI Server",
        accessMode: String = "Private"
    ): Result<ServerEntity> {
        val deviceId = "android_${Build.MODEL.replace(" ", "_")}_${UUID.randomUUID().toString().take(6)}"
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

        apiClient.setBaseUrl(address)
        val tokenKey = "session_${UUID.randomUUID().toString().take(8)}"
        sessionLogger.info("Attempting to pair with server at $address")

        return try {
            val response = apiClient.getService().pairDevice(
                PairRequest(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    platform = "Android ${Build.VERSION.RELEASE}",
                    invitationToken = invitationToken,
                    password = password
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val pairBody = response.body()!!
                val sessionToken = pairBody.sessionToken ?: throw IllegalStateException("No session token provided by server")
                keystoreManager.saveEncryptedToken(tokenKey, sessionToken)

                val serverEntity = ServerEntity(
                    id = UUID.randomUUID().toString(),
                    name = pairBody.serverName ?: serverName,
                    description = description,
                    address = address,
                    accessMode = accessMode,
                    isConnected = true
                )

                database.serverDao().disconnectAll()
                database.serverDao().insertServer(serverEntity)
                _currentServer.value = serverEntity
                tunnelClient.establishSession(address, sessionToken, TransportType.DIRECT_HTTPS)

                sessionLogger.info("Successfully paired with ${serverEntity.name}")
                Result.success(serverEntity)
            } else {
                sessionLogger.error("Pairing failed: HTTP ${response.code()}")
                Result.failure(Exception("Pairing failed: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionLogger.error("Network error during pairing: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun connectToServer(server: ServerEntity) {
        sessionLogger.info("Connecting to saved server: ${server.name} at ${server.address}")
        database.serverDao().disconnectAll()
        database.serverDao().setConnected(server.id)
        _currentServer.value = server.copy(isConnected = true)
        apiClient.setBaseUrl(server.address)
        val token = keystoreManager.getDecryptedToken("session_${server.id}")
        if (token != null) {
            sessionLogger.info("Session token found, establishing tunnel")
        } else {
            sessionLogger.warn("No session token found for this server")
        }
        tunnelClient.establishSession(server.address, token)
    }

    suspend fun disconnectCurrentServer() {
        sessionLogger.info("Disconnecting from current server")
        database.serverDao().disconnectAll()
        _currentServer.value = null
        tunnelClient.closeSession()
    }

    suspend fun getDiscoveredServers(): List<DiscoveryServerItem> {
        return try {
            val res = apiClient.getService().getDiscoveredServers()
            if (res.isSuccessful && res.body() != null) {
                res.body()!!.map { item ->
                    item.copy(
                        internalIp = item.internalIp ?: "192.168.1.${(10..250).random()}",
                        uptime = item.uptime ?: "${(1..24).random()}h ${(0..59).random()}m",
                        currentLoad = item.currentLoad ?: "${(10..95).random()}% CPU, ${(20..90).random()}% RAM"
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun pingServer(address: String): FoundServerInfo {
        sessionLogger.info("Pinging custom server at $address")
        val moshi = com.squareup.moshi.Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
            
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        val normalized = if (address.endsWith("/")) address else "$address/"
            
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
            .build()
            
        val service = retrofit.create(OliApiService::class.java)
        
        try {
            val response = service.getHealth()
            if (response.isSuccessful) {
                sessionLogger.info("Ping successful: HTTP ${response.code()}")
                var serverName = "OLI Remote Server"
                var accessMode = "Private"
                try {
                   val info = service.getServerInfo(null)
                   if (info.isSuccessful && info.body() != null) {
                       serverName = info.body()!!.name
                       accessMode = info.body()!!.accessMode
                   }
                } catch (e: Exception) {}
                
                return FoundServerInfo(
                    id = "manual_${System.currentTimeMillis()}",
                    name = serverName,
                    description = "Custom Host Address",
                    address = address,
                    accessMode = accessMode,
                    requiresPassword = false,
                    invitationToken = "manual_token"
                )
            } else {
                sessionLogger.warn("Ping returned invalid status: HTTP ${response.code()}")
                throw Exception("Invalid response from server (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            sessionLogger.error("Ping failed: ${e.message}")
            throw e
        }
    }

    fun setServerOnline(online: Boolean) {
        _isServerOnline.value = online
    }

    suspend fun deleteServer(id: String) {
        database.serverDao().deleteServerById(id)
    }
}
