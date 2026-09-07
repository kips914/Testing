package com.example.data.repository

import android.content.Context
import com.example.data.api.OliApiClient
import com.example.data.models.OliSettings
import com.example.security.KeystoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val context: Context,
    private val apiClient: OliApiClient,
    private val keystoreManager: KeystoreManager
) {
    private val prefs = context.getSharedPreferences("oli_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadLocalSettings())
    val settings: StateFlow<OliSettings> = _settings.asStateFlow()

    private fun loadLocalSettings(): OliSettings {
        return OliSettings(
            quickMode = prefs.getBoolean("quick_mode", false),
            performance = prefs.getString("performance", "Balanced") ?: "Balanced",
            model = prefs.getString("model", "llama-3-8b-instruct.Q4_K_M") ?: "llama-3-8b-instruct.Q4_K_M",
            temperature = prefs.getFloat("temperature", 0.7f),
            contextWindow = prefs.getInt("context_window", 4096),
            theme = prefs.getString("theme", "Dark") ?: "Dark",
            language = prefs.getString("language", "System") ?: "System"
        )
    }

    suspend fun updateSettings(newSettings: OliSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putBoolean("quick_mode", newSettings.quickMode)
            .putString("performance", newSettings.performance)
            .putString("model", newSettings.model)
            .putFloat("temperature", newSettings.temperature)
            .putInt("context_window", newSettings.contextWindow)
            .putString("theme", newSettings.theme)
            .putString("language", newSettings.language)
            .apply()

        // Also try to push to remote server if connected
        try {
            apiClient.getService().updateSettings(null, newSettings)
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    suspend fun fetchRemoteSettings() {
        try {
            val response = apiClient.getService().getSettings(null)
            if (response.isSuccessful && response.body() != null) {
                updateSettings(response.body()!!)
            }
        } catch (e: Exception) {
            // Local fallback
        }
    }

    fun clearCache() {
        context.cacheDir.deleteRecursively()
    }
}
