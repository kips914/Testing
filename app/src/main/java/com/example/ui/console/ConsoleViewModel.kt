package com.example.ui.console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.EngineStatusResponse
import com.example.data.models.ServerInfoResponse
import com.example.data.repository.ConsoleLog
import com.example.data.repository.OliServerManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConsoleViewModel(
    private val serverManager: OliServerManager
) : ViewModel() {

    val serverInfo: StateFlow<ServerInfoResponse> = serverManager.serverInfo
    val engineStatus: StateFlow<EngineStatusResponse> = serverManager.engineStatus
    val logs: StateFlow<List<ConsoleLog>> = serverManager.consoleLogs

    fun refresh() {
        viewModelScope.launch {
            serverManager.refreshStatus()
        }
    }

    fun restartEngine() {
        viewModelScope.launch {
            serverManager.restartEngine()
        }
    }
}
