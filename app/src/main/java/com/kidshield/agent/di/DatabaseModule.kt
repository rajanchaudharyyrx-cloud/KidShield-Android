package com.kidshield.agent.di

import android.content.Context
import androidx.room.Room
import com.kidshield.agent.data.local.KidShieldDatabase
import com.kidshield.agent.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KidShieldDatabase {
        return Room.databaseBuilder(
            context,
            KidShieldDatabase::class.java,
            Constants.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePairingDao(db: KidShieldDatabase) = db.pairingDao()
    @Provides
    fun provideDeviceInfoDao(db: KidShieldDatabase) = db.deviceInfoDao()
    @Provides
    fun provideLocationDao(db: KidShieldDatabase) = db.locationDao()
    @Provides
    fun provideAppUsageDao(db: KidShieldDatabase) = db.appUsageDao()
    @Provides
    fun provideScreenTimeDao(db: KidShieldDatabase) = db.screenTimeDao()
    @Provides
    fun provideBlockedAppDao(db: KidShieldDatabase) = db.blockedAppDao()
    @Provides
    fun provideNotificationDao(db: KidShieldDatabase) = db.notificationDao()
    @Provides
    fun provideContactDao(db: KidShieldDatabase) = db.contactDao()
    @Provides
    fun provideSmsDao(db: KidShieldDatabase) = db.smsDao()
    @Provides
    fun provideCallLogDao(db: KidShieldDatabase) = db.callLogDao()
    @Provides
    fun provideSyncQueueDao(db: KidShieldDatabase) = db.syncQueueDao()
    @Provides
    fun provideSettingsDao(db: KidShieldDatabase) = db.settingsDao()
}
