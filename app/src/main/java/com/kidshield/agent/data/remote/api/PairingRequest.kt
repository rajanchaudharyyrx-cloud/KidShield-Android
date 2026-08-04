package com.kidshield.agent.data.remote.api

data class PairingRequest(
    val pairingCode: String,
    val deviceId: String,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int
)
