package com.kidshield.agent.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kidshield.agent.service.KidShieldService
import com.kidshield.agent.utils.SecurePrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var securePrefs: SecurePrefs

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (securePrefs.getBoolean(com.kidshield.agent.utils.Constants.KEY_IS_PAIRED, false)) {
                KidShieldService.start(context)
            }
        }
    }
}
