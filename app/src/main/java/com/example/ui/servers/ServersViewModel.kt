package com.example.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entities.ServerEntity
import com.example.data.models.DeviceItem
import com.example.data.models.DiscoveryServerItem
import com.example.data.models.InvitationItem
import com.example.data.models.ServerInfoResponse
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.OliServerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServersViewModel(
    private val connectionRepository: ConnectionRepository,
    private val serverManager: OliServerManager
) : ViewModel() {

    val myServers: StateFlow<List<ServerEntity>> = connectionRepository.allServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serverInfo: StateFlow<ServerInfoResponse> = serverManager.serverInfo
    val devices: StateFlow<List<DeviceItem>> = serverManager.devices
    val invitations: StateFlow<List<InvitationItem>> = serverManager.invitations

    private val _discoveredServers = MutableStateFlow<List<DiscoveryServerItem>>(emptyList())
    val discoveredServers: StateFlow<List<DiscoveryServerItem>> = _discoveredServers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            serverManager.refreshStatus()
            _discoveredServers.value = connectionRepository.getDiscoveredServers()
            _isLoading.value = false
        }
    }

    fun startServer() {
        viewModelScope.launch {
            serverManager.startServer()
        }
    }

    fun stopServer() {
        viewModelScope.launch {
            serverManager.stopServer()
        }
    }

    fun restartEngine() {
        viewModelScope.launch {
            serverManager.restartEngine()
        }
    }

    fun connectServer(server: ServerEntity) {
        viewModelScope.launch {
            connectionRepository.connectToServer(server)
        }
    }

    fun deleteServer(id: String) {
        viewModelScope.launch {
            connectionRepository.deleteServer(id)
        }
    }

    fun createInvitation(maxUses: Int, validityHours: Int, onCreated: (InvitationItem) -> Unit) {
        viewModelScope.launch {
            val inv = serverManager.createInvitation(maxUses, validityHours)
            onCreated(inv)
        }
    }

    fun revokeInvitation(id: String) {
        viewModelScope.launch {
            serverManager.revokeInvitation(id)
        }
    }

    fun revokeDevice(id: String) {
        viewModelScope.launch {
            serverManager.revokeDevice(id)
        }
    }

    fun updateServerConfig(
        name: String,
        description: String,
        port: Int,
        accessMode: String,
        passwordProtected: Boolean,
        password: String? = null
    ) {
        viewModelScope.launch {
            serverManager.updateServerSettings(name, description, port, accessMode, passwordProtected, password)
        }
    }
}
