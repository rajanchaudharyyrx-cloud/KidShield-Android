package com.kidshield.agent.data.remote.api

data class LocationUpdateRequest(
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val speed: Float,
    val heading: Float,
    val timestamp: Long
)
