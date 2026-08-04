package com.kidshield.agent.data.remote.api

data class HeartbeatResponse(
    val success: Boolean,
    val commands: List<DeviceCommand>?,
    val settings: Map<String, String>?
)
