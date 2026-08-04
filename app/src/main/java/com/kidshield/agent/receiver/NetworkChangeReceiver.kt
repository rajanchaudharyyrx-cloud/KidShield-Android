package com.kidshield.agent.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kidshield.agent.service.KidShieldService
import com.kidshield.agent.utils.NetworkUtils
import com.kidshield.agent.utils.SecurePrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NetworkChangeReceiver : BroadcastReceiver() {

    @Inject lateinit var networkUtils: NetworkUtils
    @Inject lateinit var securePrefs: SecurePrefs

    override fun onReceive(context: Context, intent: Intent?) {
        if (networkUtils.isOnline() && securePrefs.getBoolean(com.kidshield.agent.utils.Constants.KEY_IS_PAIRED, false)) {
            CoroutineScope(Dispatchers.IO).launch {
                KidShieldService.start(context)
            }
        }
    }
}
