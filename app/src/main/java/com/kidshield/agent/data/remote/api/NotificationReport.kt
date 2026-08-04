package com.kidshield.agent.data.remote.api

data class NotificationReport(
    val deviceId: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
