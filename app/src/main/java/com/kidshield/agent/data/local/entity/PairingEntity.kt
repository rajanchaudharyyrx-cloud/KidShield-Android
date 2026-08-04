package com.kidshield.agent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pairing")
data class PairingEntity(
    @PrimaryKey val id: Int = 1,
    val pairingCode: String,
    val authToken: String,
    val deviceId: String,
    val parentId: String,
    val pairedAt: Long,
    val isActive: Boolean = true
)
