package com.example.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entities.ServerEntity
import com.example.data.models.DiscoveryServerItem
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.FoundServerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    val currentServer: StateFlow<ServerEntity?> = connectionRepository.currentServer

    private val _discoveredServers = MutableStateFlow<List<DiscoveryServerItem>>(emptyList())
    val discoveredServers: StateFlow<List<DiscoveryServerItem>> = _discoveredServers.asStateFlow()

    private val _foundServer = MutableStateFlow<FoundServerInfo?>(null)
    val foundServer: StateFlow<FoundServerInfo?> = _foundServer.asStateFlow()

    private val _isPairing = MutableStateFlow(false)
    val isPairing: StateFlow<Boolean> = _isPairing.asStateFlow()

    private val _pairingMessage = MutableStateFlow<String?>(null)
    val pairingMessage: StateFlow<String?> = _pairingMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun handleScannedQr(rawQr: String) {
        _errorMessage.value = null
        val parsed = connectionRepository.parseInviteQr(rawQr)
        if (!parsed.isValid) {
            _errorMessage.value = parsed.error ?: "Invalid QR code format"
            return
        }

        // Show confirmation dialog with found server info
        _foundServer.value = FoundServerInfo(
            id = "scanned_${System.currentTimeMillis()}",
            name = "My OLI Server",
            description = "Personal AI Server on PC",
            address = parsed.address ?: "http://100.84.12.9:8080",
            accessMode = "Private",
            requiresPassword = false,
            invitationToken = parsed.token
        )
    }

    fun handleManualAddress(address: String) {
        val trimmed = address.trim()
        if (trimmed.isBlank()) {
            _errorMessage.value = "Please enter server address"
            return
        }

        val fullAddress = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "http://$trimmed"
        } else {
            trimmed
        }

        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val found = connectionRepository.pingServer(fullAddress)
                _foundServer.value = found
            } catch (e: Exception) {
                _errorMessage.value = "Failed to connect: ${e.message}"
            }
        }
    }

    fun confirmConnect(password: String? = null, onConnected: () -> Unit) {
        val target = _foundServer.value ?: return
        _isPairing.value = true
        _pairingMessage.value = "Pairing with ${target.name}..."

        viewModelScope.launch {
            val result = connectionRepository.pairWithServer(
                address = target.address,
                invitationToken = target.invitationToken ?: "token_default",
                password = password,
                serverName = target.name,
                description = target.description,
                accessMode = target.accessMode
            )

            _isPairing.value = false
            if (result.isSuccess) {
                _pairingMessage.value = "Connected successfully!"
                _foundServer.value = null
                onConnected()
            } else {
                _errorMessage.value = "Failed to connect to server: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun cancelFoundServer() {
        _foundServer.value = null
        _errorMessage.value = null
    }

    fun disconnect() {
        viewModelScope.launch {
            connectionRepository.disconnectCurrentServer()
        }
    }

    fun discoverServers() {
        viewModelScope.launch {
            _discoveredServers.value = connectionRepository.getDiscoveredServers()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
