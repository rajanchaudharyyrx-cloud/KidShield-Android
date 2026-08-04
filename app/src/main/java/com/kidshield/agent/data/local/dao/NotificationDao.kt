package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE synced = 0 ORDER BY timestamp DESC LIMIT 200")
    suspend fun getUnsynced(): List<NotificationEntity>

    @Query("UPDATE notifications SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM notifications WHERE synced = 1 AND timestamp < :before")
    suspend fun deleteOldSynced(before: Long)
}
