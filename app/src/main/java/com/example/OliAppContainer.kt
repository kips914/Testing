package com.example

import android.content.Context
import com.example.data.api.OliApiClient
import com.example.data.database.OliDatabase
import com.example.data.repository.ChatRepository
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.MemoryRepository
import com.example.data.repository.OliServerManager
import com.example.data.repository.SessionLogger
import com.example.data.repository.SettingsRepository
import com.example.data.tunnel.DirectHttpsTransport
import com.example.data.tunnel.TailscaleTransport
import com.example.data.tunnel.TunnelClient
import com.example.security.KeystoreManager

class OliAppContainer(context: Context) {
    val sessionLogger: SessionLogger = SessionLogger()
    val database: OliDatabase = OliDatabase.getInstance(context)
    val keystoreManager: KeystoreManager = KeystoreManager(context)
    val tailscaleTransport: TailscaleTransport = TailscaleTransport()
    val tunnelClient: TunnelClient = TunnelClient(context, DirectHttpsTransport())
    val apiClient: OliApiClient = OliApiClient(context, tunnelClient)

    val connectionRepository: ConnectionRepository = ConnectionRepository(
        context = context,
        database = database,
        apiClient = apiClient,
        tunnelClient = tunnelClient,
        keystoreManager = keystoreManager,
        sessionLogger = sessionLogger
    )

    val chatRepository: ChatRepository = ChatRepository(
        context = context,
        database = database,
        apiClient = apiClient,
        connectionRepository = connectionRepository,
        keystoreManager = keystoreManager
    )

    val settingsRepository: SettingsRepository = SettingsRepository(
        context = context,
        apiClient = apiClient,
        keystoreManager = keystoreManager
    )

    val memoryRepository: MemoryRepository = MemoryRepository(
        context = context,
        database = database,
        apiClient = apiClient
    )

    val serverManager: OliServerManager = OliServerManager(
        context = context,
        apiClient = apiClient,
        connectionRepository = connectionRepository,
        sessionLogger = sessionLogger
    )
}
