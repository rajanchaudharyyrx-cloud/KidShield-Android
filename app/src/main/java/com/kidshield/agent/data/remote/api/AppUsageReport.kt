package com.kidshield.agent.data.remote.api

data class AppUsageReport(
    val deviceId: String,
    val packageName: String,
    val appName: String,
    val date: String,
    val usageTimeMs: Long,
    val openCount: Int
)
