package com.kidshield.agent.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface KidShieldApi {

    @POST("agent/pair")
    suspend fun pairDevice(@Body request: PairingRequest): Response<PairingResponse>

    @POST("agent/heartbeat")
    suspend fun sendHeartbeat(@Header("Authorization") token: String, @Body request: HeartbeatRequest): Response<HeartbeatResponse>

    @POST("agent/location")
    suspend fun sendLocation(@Header("Authorization") token: String, @Body request: LocationUpdateRequest): Response<GenericResponse>

    @POST("agent/location/batch")
    suspend fun sendLocationsBatch(@Header("Authorization") token: String, @Body requests: List<LocationUpdateRequest>): Response<GenericResponse>

    @POST("agent/usage")
    suspend fun sendAppUsage(@Header("Authorization") token: String, @Body reports: List<AppUsageReport>): Response<GenericResponse>

    @POST("agent/screentime")
    suspend fun sendScreenTime(@Header("Authorization") token: String, @Body reports: List<ScreenTimeReport>): Response<GenericResponse>

    @POST("agent/notifications")
    suspend fun sendNotifications(@Header("Authorization") token: String, @Body reports: List<NotificationReport>): Response<GenericResponse>

    @POST("agent/contacts")
    suspend fun sendContacts(@Header("Authorization") token: String, @Body reports: List<ContactReport>): Response<GenericResponse>

    @POST("agent/sms")
    suspend fun sendSms(@Header("Authorization") token: String, @Body reports: List<SmsReport>): Response<GenericResponse>

    @POST("agent/calls")
    suspend fun sendCallLogs(@Header("Authorization") token: String, @Body reports: List<CallLogReport>): Response<GenericResponse>

    @POST("agent/apps")
    suspend fun sendAppList(@Header("Authorization") token: String, @Body report: AppListReport): Response<GenericResponse>

    @Multipart
    @POST("agent/screenshot")
    suspend fun uploadScreenshot(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<ScreenshotResponse>

    @POST("agent/command/ack")
    suspend fun acknowledgeCommand(@Header("Authorization") token: String, @Query("commandId") commandId: String): Response<GenericResponse>

    @GET("agent/blocked-apps")
    suspend fun getBlockedApps(@Header("Authorization") token: String): Response<List<AppInfo>>

    @GET("agent/settings")
    suspend fun getSettings(@Header("Authorization") token: String): Response<Map<String, String>>
}
