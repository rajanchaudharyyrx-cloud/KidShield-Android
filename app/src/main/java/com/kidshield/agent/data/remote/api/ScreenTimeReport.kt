package com.kidshield.agent.data.remote.api

data class ScreenTimeReport(
    val deviceId: String,
    val date: String,
    val totalScreenTimeMs: Long,
    val unlockCount: Int,
    val screenOnTimeMs: Long,
    val screenOffTimeMs: Long
)
