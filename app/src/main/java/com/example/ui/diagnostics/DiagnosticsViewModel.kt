package com.example.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DiagnosticsStatus
import com.example.data.repository.OliServerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnosticsViewModel(
    private val serverManager: OliServerManager
) : ViewModel() {

    val diagnostics: StateFlow<DiagnosticsStatus> = serverManager.diagnostics

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    init {
        runDiagnostics()
    }

    fun runDiagnostics() {
        viewModelScope.launch {
            _isChecking.value = true
            serverManager.runDiagnostics()
            _isChecking.value = false
        }
    }
}
