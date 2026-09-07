package com.example.data.api

import com.example.data.models.ActionResponse
import com.example.data.models.CapabilitiesResponse
import com.example.data.models.ChatRequest
import com.example.data.models.ChatResponse
import com.example.data.models.CreateInvitationRequest
import com.example.data.models.DeviceItem
import com.example.data.models.DiscoveryServerItem
import com.example.data.models.EngineStatusResponse
import com.example.data.models.HealthResponse
import com.example.data.models.InvitationItem
import com.example.data.models.OliMemoryData
import com.example.data.models.OliSettings
import com.example.data.models.PairRequest
import com.example.data.models.PairResponse
import com.example.data.models.ServerInfoResponse
import com.example.data.models.ServerSettingsRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Streaming

interface OliApiService {

    @POST("api/pair")
    suspend fun pairDevice(
        @Body request: PairRequest
    ): Response<PairResponse>

    @POST("api/chat")
    suspend fun sendChat(
        @Header("Authorization") token: String?,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @Streaming
    @POST("api/chat")
    suspend fun streamChat(
        @Header("Authorization") token: String?,
        @Body request: ChatRequest
    ): Response<ResponseBody>

    @GET("api/engine-status")
    suspend fun getEngineStatus(
        @Header("Authorization") token: String?
    ): Response<EngineStatusResponse>

    @POST("api/ensure-running")
    suspend fun ensureRunning(
        @Header("Authorization") token: String?
    ): Response<ActionResponse>

    @POST("api/restart-engine")
    suspend fun restartEngine(
        @Header("Authorization") token: String?
    ): Response<ActionResponse>

    @GET("api/settings")
    suspend fun getSettings(
        @Header("Authorization") token: String?
    ): Response<OliSettings>

    @PUT("api/settings")
    suspend fun updateSettings(
        @Header("Authorization") token: String?,
        @Body settings: OliSettings
    ): Response<ActionResponse>

    @GET("api/server/info")
    suspend fun getServerInfo(
        @Header("Authorization") token: String?
    ): Response<ServerInfoResponse>

    @GET("api/server/status")
    suspend fun getServerStatus(
        @Header("Authorization") token: String?
    ): Response<ServerInfoResponse>

    @PUT("api/server/settings")
    suspend fun updateServerSettings(
        @Header("Authorization") token: String?,
        @Body request: ServerSettingsRequest
    ): Response<ActionResponse>

    @POST("api/server/start")
    suspend fun startServer(
        @Header("Authorization") token: String?
    ): Response<ActionResponse>

    @POST("api/server/stop")
    suspend fun stopServer(
        @Header("Authorization") token: String?
    ): Response<ActionResponse>

    @GET("api/server/devices")
    suspend fun getDevices(
        @Header("Authorization") token: String?
    ): Response<List<DeviceItem>>

    @POST("api/server/devices/{id}/revoke")
    suspend fun revokeDevice(
        @Header("Authorization") token: String?,
        @Path("id") deviceId: String
    ): Response<ActionResponse>

    @GET("api/server/invitations")
    suspend fun getInvitations(
        @Header("Authorization") token: String?
    ): Response<List<InvitationItem>>

    @POST("api/server/invitations")
    suspend fun createInvitation(
        @Header("Authorization") token: String?,
        @Body request: CreateInvitationRequest
    ): Response<InvitationItem>

    @POST("api/server/invitations/{id}/revoke")
    suspend fun revokeInvitation(
        @Header("Authorization") token: String?,
        @Path("id") invitationId: String
    ): Response<ActionResponse>

    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/capabilities")
    suspend fun getCapabilities(): Response<CapabilitiesResponse>

    @GET("api/discovery/servers")
    suspend fun getDiscoveredServers(): Response<List<DiscoveryServerItem>>

    @GET("api/memory")
    suspend fun getMemory(
        @Header("Authorization") token: String?
    ): Response<OliMemoryData>

    @PUT("api/memory")
    suspend fun updateMemory(
        @Header("Authorization") token: String?,
        @Body memory: OliMemoryData
    ): Response<ActionResponse>
}
