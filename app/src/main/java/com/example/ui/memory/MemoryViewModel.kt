package com.example.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.OliMemoryData
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MemoryViewModel(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    val memory: StateFlow<OliMemoryData> = memoryRepository.memory

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        viewModelScope.launch {
            memoryRepository.loadMemory()
        }
    }

    fun saveMemory(data: OliMemoryData) {
        viewModelScope.launch {
            val success = memoryRepository.saveMemory(data)
            if (success) {
                _isSaved.value = true
                kotlinx.coroutines.delay(2000)
                _isSaved.value = false
            }
        }
    }
}
