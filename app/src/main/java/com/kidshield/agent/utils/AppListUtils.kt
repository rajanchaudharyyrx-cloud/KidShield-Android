package com.kidshield.agent.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppListUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pm = context.packageManager

    data class AppDetail(
        val packageName: String,
        val appName: String,
        val version: String,
        val versionCode: Long,
        val icon: android.graphics.drawable.Drawable?,
        val installTime: Long,
        val updateTime: Long,
        val isSystemApp: Boolean
    )

    fun getInstalledApps(): List<AppDetail> {
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).map { app ->
            AppDetail(
                packageName = app.packageName,
                appName = pm.getApplicationLabel(app).toString(),
                version = try { pm.getPackageInfo(app.packageName, 0).versionName ?: "" } catch (e: Exception) { "" },
                versionCode = try { pm.getPackageInfo(app.packageName, 0).longVersionCode } catch (e: Exception) { 0 },
                icon = try { pm.getApplicationIcon(app.packageName) } catch (e: Exception) { null },
                installTime = try { pm.getPackageInfo(app.packageName, 0).firstInstallTime } catch (e: Exception) { 0 },
                updateTime = try { pm.getPackageInfo(app.packageName, 0).lastUpdateTime } catch (e: Exception) { 0 },
                isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }
    }

    fun detectNewApps(previousApps: Set<String>): List<String> {
        val currentApps = getInstalledApps().map { it.packageName }.toSet()
        return currentApps.subtract(previousApps).toList()
    }

    fun detectRemovedApps(previousApps: Set<String>): List<String> {
        val currentApps = getInstalledApps().map { it.packageName }.toSet()
        return previousApps.subtract(currentApps).toList()
    }
}
