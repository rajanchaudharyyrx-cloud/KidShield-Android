package com.kidshield.agent.data.remote.api

data class DeviceCommand(
    val id: String,
    val type: String,
    val payload: Map<String, Any>?,
    val timestamp: Long
)
