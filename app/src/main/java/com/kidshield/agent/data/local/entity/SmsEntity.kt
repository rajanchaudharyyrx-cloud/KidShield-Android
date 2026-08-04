package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms")
data class SmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long,
    val type: Int,
    val synced: Boolean = false
)
