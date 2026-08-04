package com.kidshield.agent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kidshield.agent.data.local.dao.*
import com.kidshield.agent.data.local.entity.*

@Database(
    entities = [
        PairingEntity::class,
        DeviceInfoEntity::class,
        LocationEntity::class,
        AppUsageEntity::class,
        ScreenTimeEntity::class,
        BlockedAppEntity::class,
        NotificationEntity::class,
        ContactEntity::class,
        SmsEntity::class,
        CallLogEntity::class,
        SyncQueueEntity::class,
        SettingsEntity::class
    ],
    version = Constants.DB_VERSION,
    exportSchema = false
)
abstract class KidShieldDatabase : RoomDatabase() {
    abstract fun pairingDao(): PairingDao
    abstract fun deviceInfoDao(): DeviceInfoDao
    abstract fun locationDao(): LocationDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun screenTimeDao(): ScreenTimeDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun notificationDao(): NotificationDao
    abstract fun contactDao(): ContactDao
    abstract fun smsDao(): SmsDao
    abstract fun callLogDao(): CallLogDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun settingsDao(): SettingsDao
}
