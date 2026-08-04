package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val name: String?,
    val type: Int,
    val duration: Long,
    val timestamp: Long,
    val synced: Boolean = false
)
