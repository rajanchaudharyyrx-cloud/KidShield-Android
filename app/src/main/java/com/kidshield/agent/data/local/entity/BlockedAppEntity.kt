package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val blockedAt: Long,
    val blockedBy: String,
    val isBlocked: Boolean = true
)
