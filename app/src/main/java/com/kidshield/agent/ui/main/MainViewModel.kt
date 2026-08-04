package com.kidshield.agent.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidshield.agent.data.local.entity.*
import com.kidshield.agent.data.remote.api.DeviceCommand
import com.kidshield.agent.data.repository.KidShieldRepository
import com.kidshield.agent.service.DeviceCommandHandler
import com.kidshield.agent.service.LocationTracker
import com.kidshield.agent.utils.BatteryInfo
import com.kidshield.agent.utils.BatteryUtils
import com.kidshield.agent.utils.NetworkUtils
import com.kidshield.agent.worker.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: KidShieldRepository,
    private val batteryUtils: BatteryUtils,
    private val networkUtils: NetworkUtils,
    private val deviceCommandHandler: DeviceCommandHandler,
    private val locationTracker: LocationTracker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isPaired: Boolean = repository.isPaired()
    val pairing = repository.getPairing()
    val latestLocation = repository.getLatestLocation()
    val blockedApps = repository.getBlockedApps()
    val connectionState = repository.connectionState
    val commands = repository.commands

    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo

    private val _isOnline = MutableStateFlow(networkUtils.isOnline())
    val isOnline: StateFlow<Boolean> = _isOnline

    private val _lastSync = MutableStateFlow<Long>(0)
    val lastSync: StateFlow<Long> = _lastSync

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _batteryInfo.value = batteryUtils.getBatteryInfo()
        }
        if (isPaired) {
            repository.connectWebSocket()
            startCommandListener()
            startLocationTracking()
            scheduleWorkers()
        }
    }

    private fun startCommandListener() {
        viewModelScope.launch {
            commands.collect { command ->
                deviceCommandHandler.handleCommand(command)
            }
        }
    }

    private fun startLocationTracking() {
        locationTracker.startTracking()
    }

    private fun scheduleWorkers() {
        DataSyncWorker.schedule(context)
        HeartbeatWorker.schedule(context)
        LocationSyncWorker.schedule(context)
        AppUsageSyncWorker.schedule(context)
    }

    fun refreshBattery() {
        viewModelScope.launch {
            _batteryInfo.value = batteryUtils.getBatteryInfo()
        }
    }

    fun updateOnlineStatus() {
        _isOnline.value = networkUtils.isOnline()
    }

    fun syncNow() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncLocations()
            repository.syncAppUsage()
            repository.syncScreenTime()
            repository.syncNotifications()
            repository.syncContacts()
            repository.syncSms()
            repository.syncCallLogs()
            _lastSync.value = System.currentTimeMillis()
            _isLoading.value = false
        }
    }

    fun disconnect() {
        locationTracker.stopTracking()
        repository.disconnectWebSocket()
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
