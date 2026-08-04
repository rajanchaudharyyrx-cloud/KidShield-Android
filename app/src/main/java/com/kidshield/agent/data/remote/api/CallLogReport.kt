package com.kidshield.agent.data.remote.api

data class CallLogReport(
    val deviceId: String,
    val number: String,
    val name: String?,
    val type: Int,
    val duration: Long,
    val timestamp: Long
)
