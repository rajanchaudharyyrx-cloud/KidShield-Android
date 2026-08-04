package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_time")
data class ScreenTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val totalScreenTimeMs: Long,
    val unlockCount: Int,
    val screenOnTimeMs: Long,
    val screenOffTimeMs: Long,
    val synced: Boolean = false
)
