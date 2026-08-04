package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationEntity): Long

    @Query("SELECT * FROM locations WHERE synced = 0 ORDER BY timestamp DESC LIMIT 100")
    suspend fun getUnsynced(): List<LocationEntity>

    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<LocationEntity?>

    @Query("UPDATE locations SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM locations WHERE synced = 1 AND timestamp < :before")
    suspend fun deleteOldSynced(before: Long)
}
