package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.ScreenTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(screenTime: ScreenTimeEntity)

    @Query("SELECT * FROM screen_time WHERE date = :date")
    suspend fun getByDate(date: String): ScreenTimeEntity?

    @Query("SELECT * FROM screen_time WHERE synced = 0")
    suspend fun getUnsynced(): List<ScreenTimeEntity>

    @Query("SELECT * FROM screen_time ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<ScreenTimeEntity?>

    @Query("UPDATE screen_time SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
}
