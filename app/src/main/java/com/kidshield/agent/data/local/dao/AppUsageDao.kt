package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usage: AppUsageEntity)

    @Query("SELECT * FROM app_usage WHERE synced = 0")
    suspend fun getUnsynced(): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE date = :date AND packageName = :packageName")
    suspend fun getByPackageAndDate(packageName: String, date: String): AppUsageEntity?

    @Query("SELECT SUM(usageTimeMs) FROM app_usage WHERE date = :date")
    fun getTotalUsageForDate(date: String): Flow<Long?>

    @Query("UPDATE app_usage SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
}
