package com.example.data.repository

import android.content.Context
import com.example.data.api.OliApiClient
import com.example.data.database.OliDatabase
import com.example.data.database.entities.MemoryEntity
import com.example.data.models.OliMemoryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class MemoryRepository(
    private val context: Context,
    private val database: OliDatabase,
    private val apiClient: OliApiClient
) {
    private val _memory = MutableStateFlow(OliMemoryData())
    val memory: StateFlow<OliMemoryData> = _memory.asStateFlow()

    suspend fun loadMemory() = withContext(Dispatchers.IO) {
        // Try remote first
        try {
            val response = apiClient.getService().getMemory(null)
            if (response.isSuccessful && response.body() != null) {
                _memory.value = response.body()!!
                cacheToLocalDb(_memory.value)
                return@withContext
            }
        } catch (e: Exception) {
            // Read from local Room DB cache
        }

        val cached = database.memoryDao().getAllMemories()
        // If empty, keep default
    }

    suspend fun saveMemory(data: OliMemoryData): Boolean = withContext(Dispatchers.IO) {
        _memory.value = data
        cacheToLocalDb(data)
        try {
            val response = apiClient.getService().updateMemory(null, data)
            response.isSuccessful
        } catch (e: Exception) {
            true // Saved locally
        }
    }

    private suspend fun cacheToLocalDb(data: OliMemoryData) {
        val list = listOf(
            MemoryEntity("user_name", "user", data.userName),
            MemoryEntity("user_age", "user", data.userAge),
            MemoryEntity("user_language", "user", data.userLanguage),
            MemoryEntity("interests", "interests", data.interests.joinToString("; ")),
            MemoryEntity("projects", "projects", data.projects.joinToString("; ")),
            MemoryEntity("preferences", "preferences", data.preferences.joinToString("; "))
        )
        database.memoryDao().insertAll(list)
    }
}
