package com.kidshield.agent.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getDeviceName(): String = Build.MODEL
    fun getManufacturer(): String = Build.MANUFACTURER
    fun getModel(): String = Build.MODEL
    fun getAndroidVersion(): String = Build.VERSION.RELEASE
    fun getApiLevel(): Int = Build.VERSION.SDK_INT
    fun getDeviceId(): String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    fun getScreenResolution(): String {
        val metrics = context.resources.displayMetrics
        return "${metrics.widthPixels}x${metrics.heightPixels}"
    }
    fun getRefreshRate(): Float = context.display?.refreshRate ?: 60f
    fun getLanguage(): String = context.resources.configuration.locales[0].language
    fun getTimeZone(): String = java.util.TimeZone.getDefault().id

    fun getRamUsage(): Pair<Long, Long> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val total = memoryInfo.totalMem
        val available = memoryInfo.availMem
        return Pair(total, available)
    }

    fun getStorageUsage(): Pair<Long, Long> {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        return Pair(totalBlocks * blockSize, availableBlocks * blockSize)
    }

    fun getCpuInfo(): String {
        return try {
            File("/proc/cpuinfo").readText().lines().firstOrNull { it.contains("model name") }?.split(":")?.getOrNull(1)?.trim() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
