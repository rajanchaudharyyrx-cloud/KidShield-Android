package com.kidshield.agent.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import com.kidshield.agent.data.repository.KidShieldRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class KidShieldAccessibilityService : AccessibilityService() {

    @Inject lateinit var repository: KidShieldRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentPackage: String = ""
    private var appStartTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("Accessibility", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val packageName = it.packageName?.toString() ?: return
                    if (packageName != currentPackage && packageName != packageName) {
                        // Save previous app usage
                        if (currentPackage.isNotEmpty() && appStartTime > 0) {
                            val usageTime = System.currentTimeMillis() - appStartTime
                            scope.launch {
                                repository.saveAppUsage(
                                    currentPackage, currentPackage,
                                    getToday(), usageTime, 0
                                )
                            }
                        }
                        currentPackage = packageName
                        appStartTime = System.currentTimeMillis()

                        // Check if blocked
                        scope.launch {
                            if (repository.isAppBlocked(packageName)) {
                                showBlockScreen()
                            }
                        }
                    }
                }
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    val packageName = it.packageName?.toString() ?: ""
                    val text = it.text?.joinToString(" ") ?: ""
                    // Skip system notifications
                    if (!isSystemNotification(packageName)) {
                        scope.launch {
                            repository.saveNotification(packageName, packageName, "", text)
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun showBlockScreen() {
        val intent = Intent(this, com.kidshield.agent.ui.main.BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun isSystemNotification(packageName: String): Boolean {
        return packageName.contains("android") ||
                packageName.contains("systemui") ||
                packageName.contains("settings")
    }

    private fun getToday(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}
