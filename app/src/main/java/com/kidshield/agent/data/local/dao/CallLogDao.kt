package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.CallLogEntity

@Dao
interface CallLogDao {
    @Insert
    suspend fun insertAll(callLogs: List<CallLogEntity>)

    @Query("SELECT * FROM call_logs WHERE synced = 0 ORDER BY timestamp DESC LIMIT 500")
    suspend fun getUnsynced(): List<CallLogEntity>

    @Query("UPDATE call_logs SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM call_logs WHERE synced = 1 AND timestamp < :before")
    suspend fun deleteOldSynced(before: Long)
}
