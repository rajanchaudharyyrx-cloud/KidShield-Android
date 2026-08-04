package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val data: String,
    val timestamp: Long,
    val retryCount: Int = 0,
    val createdAt: Long
)
