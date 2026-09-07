package com.example.data.tunnel

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

enum class TunnelStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class TransportType {
    DIRECT_HTTPS,
    TAILSCALE,
    SECURE_RELAY
}

data class TunnelSession(
    val sessionId: String,
    val serverAddress: String,
    val token: String?,
    val transportType: TransportType,
    val connectedAt: Long = System.currentTimeMillis()
)

interface TunnelTransport {
    val transportType: TransportType
    fun isAvailable(context: Context): Boolean
    fun prepareHttpClient(builder: OkHttpClient.Builder): OkHttpClient.Builder
}

class DirectHttpsTransport : TunnelTransport {
    override val transportType: TransportType = TransportType.DIRECT_HTTPS

    override fun isAvailable(context: Context): Boolean = true

    override fun prepareHttpClient(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
    }
}

class TailscaleTransport : TunnelTransport {
    override val transportType: TransportType = TransportType.TAILSCALE

    override fun isAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.tailscale.ipn", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun prepareHttpClient(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
    }

    fun openTailscaleApp(context: Context): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.tailscale.ipn")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                // Fallback to store
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tailscale.ipn")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(marketIntent)
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * High-level TunnelClient managing active tunnel session and state
 */
class TunnelClient(
    private val context: Context,
    private val activeTransport: TunnelTransport = DirectHttpsTransport()
) {
    private val _status = MutableStateFlow(TunnelStatus.DISCONNECTED)
    val status: StateFlow<TunnelStatus> = _status.asStateFlow()

    private val _currentSession = MutableStateFlow<TunnelSession?>(null)
    val currentSession: StateFlow<TunnelSession?> = _currentSession.asStateFlow()

    fun establishSession(serverAddress: String, token: String?, transportType: TransportType = TransportType.DIRECT_HTTPS) {
        _status.value = TunnelStatus.CONNECTING
        val session = TunnelSession(
            sessionId = "sess_${System.currentTimeMillis()}",
            serverAddress = serverAddress,
            token = token,
            transportType = transportType
        )
        _currentSession.value = session
        _status.value = TunnelStatus.CONNECTED
    }

    fun closeSession() {
        _currentSession.value = null
        _status.value = TunnelStatus.DISCONNECTED
    }

    fun configureOkHttpClient(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return activeTransport.prepareHttpClient(builder)
    }
}
