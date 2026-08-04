package com.kidshield.agent.data.local.dao

import androidx.room.*
import com.kidshield.agent.data.local.entity.ContactEntity

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Query("SELECT * FROM contacts WHERE synced = 0")
    suspend fun getUnsynced(): List<ContactEntity>

    @Query("UPDATE contacts SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM contacts")
    suspend fun clear()
}
