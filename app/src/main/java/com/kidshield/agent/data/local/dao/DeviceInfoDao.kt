package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.DeviceInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: DeviceInfoEntity)

    @Query("SELECT * FROM device_info WHERE id = 1")
    fun getDeviceInfo(): Flow<DeviceInfoEntity?>

    @Query("DELETE FROM device_info")
    suspend fun clear()
}
