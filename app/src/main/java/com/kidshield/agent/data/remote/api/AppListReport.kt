package com.kidshield.agent.data.remote.api

data class AppListReport(
    val deviceId: String,
    val apps: List<AppInfo>
)

data class AppInfo(
    val packageName: String,
    val appName: String,
    val version: String,
    val versionCode: Long,
    val installTime: Long,
    val updateTime: Long,
    val isSystemApp: Boolean
)
