package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.PairingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PairingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pairing: PairingEntity)

    @Query("SELECT * FROM pairing WHERE id = 1")
    fun getPairing(): Flow<PairingEntity?>

    @Query("SELECT * FROM pairing WHERE id = 1")
    suspend fun getPairingSync(): PairingEntity?

    @Query("DELETE FROM pairing")
    suspend fun clear()
}
