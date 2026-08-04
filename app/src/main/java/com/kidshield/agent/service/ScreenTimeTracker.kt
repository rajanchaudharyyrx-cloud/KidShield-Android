package com.kidshield.agent.service

import android.content.Context
import com.kidshield.agent.data.repository.KidShieldRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: KidShieldRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var screenOnTime: Long = 0
    private var screenOffTime: Long = 0
    private var unlockCount: Int = 0
    private var isScreenOn: Boolean = false
    private var screenOnStart: Long = 0

    fun onScreenOn() {
        if (!isScreenOn) {
            isScreenOn = true
            screenOnStart = System.currentTimeMillis()
            unlockCount++
        }
    }

    fun onScreenOff() {
        if (isScreenOn) {
            isScreenOn = false
            val duration = System.currentTimeMillis() - screenOnStart
            screenOnTime += duration
        }
    }

    fun saveDailyStats() {
        scope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val totalScreenTime = screenOnTime + screenOffTime
            repository.saveScreenTime(
                date = date,
                totalMs = totalScreenTime,
                unlocks = unlockCount,
                onMs = screenOnTime,
                offMs = screenOffTime
            )
        }
    }

    fun resetDailyStats() {
        screenOnTime = 0
        screenOffTime = 0
        unlockCount = 0
    }
}
