package com.example.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.OliSettings
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.SettingsRepository
import com.example.data.tunnel.TailscaleTransport
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val connectionRepository: ConnectionRepository,
    private val tailscaleTransport: TailscaleTransport
) : ViewModel() {

    val settings: StateFlow<OliSettings> = settingsRepository.settings

    fun updateSettings(newSettings: OliSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }

    fun openTailscale(context: Context): Boolean {
        return tailscaleTransport.openTailscaleApp(context)
    }

    fun clearCache() {
        settingsRepository.clearCache()
    }

    fun resetConnection() {
        viewModelScope.launch {
            connectionRepository.disconnectCurrentServer()
        }
    }
}
