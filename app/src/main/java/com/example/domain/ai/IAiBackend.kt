package com.example.domain.ai

import kotlinx.coroutines.flow.Flow

interface IAiBackend {
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun healthCheck(): Boolean
    suspend fun generate(prompt: String): String
    fun streamGenerate(prompt: String): Flow<String>
}

class LocalAiBackend : IAiBackend {
    override suspend fun connect(): Boolean = true
    override suspend fun disconnect() {}
    override suspend fun healthCheck(): Boolean = true
    override suspend fun generate(prompt: String): String = "Inference via local llama.cpp / GGUF model"
    override fun streamGenerate(prompt: String): Flow<String> = kotlinx.coroutines.flow.flow {
        emit("Inference via local llama.cpp / GGUF model")
    }
}

class RemoteAiBackend(private val serverUrl: String) : IAiBackend {
    override suspend fun connect(): Boolean = true
    override suspend fun disconnect() {}
    override suspend fun healthCheck(): Boolean = true
    override suspend fun generate(prompt: String): String = "Inference via remote Neural Server at $serverUrl"
    override fun streamGenerate(prompt: String): Flow<String> = kotlinx.coroutines.flow.flow {
        emit("Inference via remote Neural Server")
    }
}
