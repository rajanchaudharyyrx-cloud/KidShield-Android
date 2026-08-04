package com.kidshield.agent.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.kidshield.agent.data.repository.KidShieldRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject lateinit var repository: KidShieldRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val packageName = it.packageName
            val title = it.notification.extras.getString("android.title") ?: ""
            val text = it.notification.extras.getCharSequence("android.text")?.toString() ?: ""

            if (!isSystemNotification(packageName) && !it.isOngoing) {
                scope.launch {
                    repository.saveNotification(packageName, packageName, title, text)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    private fun isSystemNotification(packageName: String): Boolean {
        return packageName.contains("android") ||
                packageName.contains("systemui") ||
                packageName.contains("settings") ||
                packageName == packageName
    }
}
