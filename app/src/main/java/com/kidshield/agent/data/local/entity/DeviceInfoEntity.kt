package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_info")
data class DeviceInfoEntity(
    @PrimaryKey val id: Int = 1,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val screenResolution: String,
    val refreshRate: Float,
    val language: String,
    val timeZone: String,
    val cpuInfo: String,
    val totalRam: Long,
    val totalStorage: Long,
    val updatedAt: Long
)
