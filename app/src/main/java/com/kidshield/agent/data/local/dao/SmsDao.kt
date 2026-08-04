package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.SmsEntity

@Dao
interface SmsDao {
    @Insert
    suspend fun insertAll(smsList: List<SmsEntity>)

    @Query("SELECT * FROM sms WHERE synced = 0 ORDER BY timestamp DESC LIMIT 500")
    suspend fun getUnsynced(): List<SmsEntity>

    @Query("UPDATE sms SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM sms WHERE synced = 1 AND timestamp < :before")
    suspend fun deleteOldSynced(before: Long)
}
