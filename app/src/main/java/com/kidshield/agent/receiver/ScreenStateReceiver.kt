package com.kidshield.agent.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kidshield.agent.service.ScreenTimeTracker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenStateReceiver : BroadcastReceiver() {

    @Inject lateinit var screenTimeTracker: ScreenTimeTracker

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> screenTimeTracker.onScreenOn()
            Intent.ACTION_SCREEN_OFF -> screenTimeTracker.onScreenOff()
        }
    }
}
