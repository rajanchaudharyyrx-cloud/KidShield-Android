package com.kidshield.agent.data.remote.api

data class SmsReport(
    val deviceId: String,
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long,
    val type: Int
)
