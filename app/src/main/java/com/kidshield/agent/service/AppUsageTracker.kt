package com.kidshield.agent.service

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.kidshield.agent.data.repository.KidShieldRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: KidShieldRepository
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null

    fun startTracking() {
        trackingJob?.cancel()
        trackingJob = scope.launch {
            while (isActive) {
                collectUsageStats()
                delay(60000) // Every minute
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
    }

    private suspend fun collectUsageStats() {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        stats?.forEach { usageStat ->
            try {
                val appInfo = packageManager.getApplicationInfo(usageStat.packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                repository.saveAppUsage(
                    packageName = usageStat.packageName,
                    appName = appName,
                    date = date,
                    timeMs = usageStat.totalTimeInForeground,
                    openCount = 1
                )
            } catch (e: Exception) {
                // App not found
            }
        }
    }

    fun getForegroundApp(): String? {
        val endTime = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            endTime - 1000,
            endTime
        )
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
