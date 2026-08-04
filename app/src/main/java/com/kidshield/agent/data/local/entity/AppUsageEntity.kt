package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val date: String,
    val usageTimeMs: Long,
    val openCount: Int,
    val synced: Boolean = false
)
