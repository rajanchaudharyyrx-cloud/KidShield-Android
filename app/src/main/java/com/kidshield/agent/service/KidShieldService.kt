package com.kidshield.agent.service

import android.app.*
import android.content.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kidshield.agent.KidShieldApp
import com.kidshield.agent.R
import com.kidshield.agent.data.repository.KidShieldRepository
import com.kidshield.agent.utils.Constants
import com.kidshield.agent.utils.NetworkUtils
import com.kidshield.agent.worker.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class KidShieldService : Service() {

    @Inject lateinit var repository: KidShieldRepository
    @Inject lateinit var networkUtils: NetworkUtils
    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var appUsageTracker: AppUsageTracker
    @Inject lateinit var screenTimeTracker: ScreenTimeTracker

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var heartbeatJob: Job? = null
    private var syncJob: Job? = null
    private var appUsageJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("KidShieldService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(KidShieldApp.NOTIFICATION_ID, createNotification())
        startMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        heartbeatJob?.cancel()
        syncJob?.cancel()
        appUsageJob?.cancel()
        locationTracker.stopTracking()
        appUsageTracker.stopTracking()
        serviceScope.cancel()
        // Restart service
        val restartIntent = Intent(applicationContext, KidShieldService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, com.kidshield.agent.ui.main.MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, KidShieldApp.CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startMonitoring() {
        if (repository.isPaired()) {
            repository.connectWebSocket()
            locationTracker.startTracking()
            appUsageTracker.startTracking()
            startHeartbeat()
            startSync()
            startAppUsageCollection()
            scheduleWorkers()
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (isActive) {
                if (networkUtils.isOnline()) {
                    repository.sendHeartbeat()
                }
                delay(Constants.HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    private fun startSync() {
        syncJob?.cancel()
        syncJob = serviceScope.launch {
            while (isActive) {
                if (networkUtils.isOnline()) {
                    launch { repository.syncLocations() }
                    launch { repository.syncAppUsage() }
                    launch { repository.syncScreenTime() }
                    launch { repository.syncNotifications() }
                    launch { repository.syncContacts() }
                    launch { repository.syncSms() }
                    launch { repository.syncCallLogs() }
                    launch { repository.processSyncQueue() }
                }
                delay(Constants.SYNC_INTERVAL_MS)
            }
        }
    }

    private fun startAppUsageCollection() {
        appUsageJob = serviceScope.launch {
            while (isActive) {
                screenTimeTracker.saveDailyStats()
                delay(300000) // Every 5 minutes
            }
        }
    }

    private fun scheduleWorkers() {
        DataSyncWorker.schedule(applicationContext)
        HeartbeatWorker.schedule(applicationContext)
        LocationSyncWorker.schedule(applicationContext)
        AppUsageSyncWorker.schedule(applicationContext)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, KidShieldService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, KidShieldService::class.java))
        }
    }
}
