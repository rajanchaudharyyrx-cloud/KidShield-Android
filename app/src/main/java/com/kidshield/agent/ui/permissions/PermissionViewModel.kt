package com.kidshield.agent.ui.permissions

import androidx.lifecycle.ViewModel
import com.kidshield.agent.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionUtils: PermissionUtils
) : ViewModel() {

    fun isUsageAccessGranted() = permissionUtils.isUsageAccessGranted()
    fun isOverlayGranted() = permissionUtils.isOverlayPermissionGranted()

    fun requestUsageAccess() = permissionUtils.requestUsageAccess()
    fun requestOverlay() = permissionUtils.requestOverlayPermission()
    fun requestAccessibility() = permissionUtils.requestAccessibilityService()
    fun requestNotificationAccess() = permissionUtils.requestNotificationAccess()
    fun requestIgnoreBattery() = permissionUtils.requestIgnoreBatteryOptimization()
}
