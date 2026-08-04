package com.kidshield.agent.data.remote.api

data class HeartbeatRequest(
    val deviceId: String,
    val timestamp: Long,
    val batteryPercentage: Int,
    val isCharging: Boolean,
    val connectionType: String,
    val ramUsagePercent: Float,
    val storageUsagePercent: Float
)
