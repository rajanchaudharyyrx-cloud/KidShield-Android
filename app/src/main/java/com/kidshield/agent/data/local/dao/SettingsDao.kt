package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingsEntity)

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun get(key: String): SettingsEntity?

    @Query("SELECT * FROM settings WHERE key = :key")
    fun getFlow(key: String): Flow<SettingsEntity?>

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun delete(key: String)
}
