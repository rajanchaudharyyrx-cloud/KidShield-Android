package com.kidshield.agent.data.repository

import com.kidshield.agent.data.local.dao.*
import com.kidshield.agent.data.local.entity.*
import com.kidshield.agent.data.remote.api.*
import com.kidshield.agent.data.remote.websocket.WebSocketManager
import com.kidshield.agent.utils.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KidShieldRepository @Inject constructor(
    private val api: KidShieldApi,
    private val webSocketManager: WebSocketManager,
    private val securePrefs: SecurePrefs,
    private val networkUtils: NetworkUtils,
    private val deviceInfoUtils: DeviceInfoUtils,
    private val batteryUtils: BatteryUtils,
    private val pairingDao: PairingDao,
    private val deviceInfoDao: DeviceInfoDao,
    private val locationDao: LocationDao,
    private val appUsageDao: AppUsageDao,
    private val screenTimeDao: ScreenTimeDao,
    private val blockedAppDao: BlockedAppDao,
    private val notificationDao: NotificationDao,
    private val contactDao: ContactDao,
    private val smsDao: SmsDao,
    private val callLogDao: CallLogDao,
    private val syncQueueDao: SyncQueueDao
) {
    // ===== Pairing =====
    suspend fun pairDevice(pairingCode: String): Result<PairingResponse> {
        return try {
            val request = PairingRequest(
                pairingCode = pairingCode,
                deviceId = deviceInfoUtils.getDeviceId(),
                deviceName = deviceInfoUtils.getDeviceName(),
                manufacturer = deviceInfoUtils.getManufacturer(),
                model = deviceInfoUtils.getModel(),
                androidVersion = deviceInfoUtils.getAndroidVersion(),
                apiLevel = deviceInfoUtils.getApiLevel()
            )
            val response = api.pairDevice(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                securePrefs.saveString(Constants.KEY_PAIRING_CODE, pairingCode)
                securePrefs.saveString(Constants.KEY_AUTH_TOKEN, body.authToken)
                securePrefs.saveString(Constants.KEY_PARENT_ID, body.parentId)
                securePrefs.saveString(Constants.KEY_DEVICE_ID, deviceInfoUtils.getDeviceId())
                securePrefs.saveBoolean(Constants.KEY_IS_PAIRED, true)

                pairingDao.insert(
                    PairingEntity(
                        pairingCode = pairingCode,
                        authToken = body.authToken,
                        deviceId = deviceInfoUtils.getDeviceId(),
                        parentId = body.parentId,
                        pairedAt = System.currentTimeMillis()
                    )
                )
                Result.success(body)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Pairing failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isPaired(): Boolean = securePrefs.getBoolean(Constants.KEY_IS_PAIRED, false)
    fun getAuthToken(): String = securePrefs.getString(Constants.KEY_AUTH_TOKEN)
    fun getDeviceId(): String = securePrefs.getString(Constants.KEY_DEVICE_ID)
    fun getPairing(): Flow<PairingEntity?> = pairingDao.getPairing()

    // ===== WebSocket =====
    fun connectWebSocket() {
        val token = getAuthToken()
        val deviceId = getDeviceId()
        if (token.isNotEmpty() && deviceId.isNotEmpty()) {
            webSocketManager.connect(token, deviceId)
        }
    }

    fun disconnectWebSocket() = webSocketManager.disconnect()
    val commands = webSocketManager.commands
    val connectionState = webSocketManager.connectionState

    // ===== Heartbeat =====
    suspend fun sendHeartbeat(): Result<HeartbeatResponse> {
        return try {
            val battery = batteryUtils.getBatteryInfo()
            val (totalRam, availRam) = deviceInfoUtils.getRamUsage()
            val (totalStorage, availStorage) = deviceInfoUtils.getStorageUsage()
            val request = HeartbeatRequest(
                deviceId = getDeviceId(),
                timestamp = System.currentTimeMillis(),
                batteryPercentage = battery.percentage,
                isCharging = battery.isCharging,
                connectionType = networkUtils.getConnectionType(),
                ramUsagePercent = ((totalRam - availRam).toFloat() / totalRam) * 100,
                storageUsagePercent = ((totalStorage - availStorage).toFloat() / totalStorage) * 100
            )
            val response = api.sendHeartbeat("Bearer ${getAuthToken()}", request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Heartbeat failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== Location =====
    suspend fun saveLocation(lat: Double, lon: Double, accuracy: Float, altitude: Double, speed: Float, heading: Float) {
        locationDao.insert(LocationEntity(
            latitude = lat, longitude = lon, accuracy = accuracy,
            altitude = altitude, speed = speed, heading = heading,
            timestamp = System.currentTimeMillis()
        ))
    }

    suspend fun syncLocations() {
        val unsynced = locationDao.getUnsynced()
        if (unsynced.isEmpty()) return
        val requests = unsynced.map {
            LocationUpdateRequest(
                deviceId = getDeviceId(), latitude = it.latitude, longitude = it.longitude,
                accuracy = it.accuracy, altitude = it.altitude, speed = it.speed,
                heading = it.heading, timestamp = it.timestamp
            )
        }
        val response = api.sendLocationsBatch("Bearer ${getAuthToken()}", requests)
        if (response.isSuccessful) {
            locationDao.markSynced(unsynced.map { it.id })
        }
    }

    fun getLatestLocation(): Flow<LocationEntity?> = locationDao.getLatest()

    // ===== App Usage =====
    suspend fun saveAppUsage(packageName: String, appName: String, date: String, timeMs: Long, openCount: Int) {
        val existing = appUsageDao.getByPackageAndDate(packageName, date)
        if (existing != null) {
            appUsageDao.insert(existing.copy(
                usageTimeMs = existing.usageTimeMs + timeMs,
                openCount = existing.openCount + openCount
            ))
        } else {
            appUsageDao.insert(AppUsageEntity(
                packageName = packageName, appName = appName, date = date,
                usageTimeMs = timeMs, openCount = openCount
            ))
        }
    }

    suspend fun syncAppUsage() {
        val unsynced = appUsageDao.getUnsynced()
        if (unsynced.isEmpty()) return
        val reports = unsynced.map {
            AppUsageReport(
                deviceId = getDeviceId(), packageName = it.packageName,
                appName = it.appName, date = it.date,
                usageTimeMs = it.usageTimeMs, openCount = it.openCount
            )
        }
        val response = api.sendAppUsage("Bearer ${getAuthToken()}", reports)
        if (response.isSuccessful) {
            appUsageDao.markSynced(unsynced.map { it.id })
        }
    }

    // ===== Screen Time =====
    suspend fun saveScreenTime(date: String, totalMs: Long, unlocks: Int, onMs: Long, offMs: Long) {
        screenTimeDao.insert(ScreenTimeEntity(
            date = date, totalScreenTimeMs = totalMs,
            unlockCount = unlocks, screenOnTimeMs = onMs, screenOffTimeMs = offMs
        ))
    }

    suspend fun syncScreenTime() {
        val unsynced = screenTimeDao.getUnsynced()
        if (unsynced.isEmpty()) return
        val reports = unsynced.map {
            ScreenTimeReport(
                deviceId = getDeviceId(), date = it.date,
                totalScreenTimeMs = it.totalScreenTimeMs, unlockCount = it.unlockCount,
                screenOnTimeMs = it.screenOnTimeMs, screenOffTimeMs = it.screenOffTimeMs
            )
        }
        val response = api.sendScreenTime("Bearer ${getAuthToken()}", reports)
        if (response.isSuccessful) {
            screenTimeDao.markSynced(unsynced.map { it.id })
        }
    }

    // ===== Blocked Apps =====
    fun getBlockedApps(): Flow<List<BlockedAppEntity>> = blockedAppDao.getBlockedApps()
    suspend fun isAppBlocked(packageName: String): Boolean = blockedAppDao.isBlocked(packageName) != null
    suspend fun blockApp(packageName: String, appName: String, blockedBy: String) {
        blockedAppDao.insert(BlockedAppEntity(
            packageName = packageName, appName = appName,
            blockedAt = System.currentTimeMillis(), blockedBy = blockedBy
        ))
    }
    suspend fun unblockApp(packageName: String) = blockedAppDao.unblock(packageName)

    // ===== Notifications =====
    suspend fun saveNotification(packageName: String, appName: String, title: String, text: String) {
        notificationDao.insert(NotificationEntity(
            packageName = packageName, appName = appName,
            title = title, text = text, timestamp = System.currentTimeMillis()
        ))
    }

    suspend fun syncNotifications() {
        val unsynced = notificationDao.getUnsynced()
        if (unsynced.isEmpty()) return
        val reports = unsynced.map {
            NotificationReport(
                deviceId = getDeviceId(), packageName = it.packageName,
                appName = it.appName, title = it.title, text = it.text, timestamp = it.timestamp
            )
        }
        val response = api.sendNotifications("Bearer ${getAuthToken()}", reports)
        if (response.isSuccessful) {
            notificationDao.markSynced(unsynced.map { it.id })
        }
    }

    // ===== Contacts =====
    suspend fun saveContacts(contacts: List<ContactEntity>) {
        contactDao.insertAll(contacts)
    }

    suspend fun syncContacts() {
        val unsynced = contactDao.getUnsynced()
        if (unsynced.isEmpty()) return
        val reports = unsynced.map {
            ContactReport(
                deviceId = getDeviceId(), name = it.name,
                phoneNumber = it.phoneNumber, email = it.email
            )
        }
        val response = api.sendContacts("Bearer ${getAuthToken()}", reports)
        if (response.isSuccessful) {
            contactDao.markSynced(unsynced.map { it.id })
        }
    }

    // ===== SMS =====
    suspend fun saveSms(smsList: List<SmsEntity>) {
        smsDao.insertAll(smsList)
    }

    suspend fun syncSms() {
        val unsynced = smsDao.getUnsynced()
        if (unsynced.isEmpty()) return
        val reports = unsynced.map {
            SmsReport(
                deviceId = getDeviceId(), sender = it.sender, receiver = it.receiver,
                message = it.message, timestamp = it.timestamp, type = it.type
            )
        }
        val response = api.sendSms("Bearer ${getAuthToken()}", reports)
        if (response.isSuccessful) {
            smsDao.markSynced(unsynced.map { it.id })
        }
    }

    // ===== Call Logs =====
    suspend fun saveCallLogs(callLogs: List<CallLogEntity>) {
        callLogDao.insertAll(callLogs)
    }

    suspend fun syncCallLogs() {
        val unsynced = callLogDao.getUnsynced()
        if (unsynced.isEmpty()) return
        val reports = unsynced.map {
            CallLogReport(
                deviceId = getDeviceId(), number = it.number, name = it.name,
                type = it.type, duration = it.duration, timestamp = it.timestamp
            )
        }
        val response = api.sendCallLogs("Bearer ${getAuthToken()}", reports)
        if (response.isSuccessful) {
            callLogDao.markSynced(unsynced.map { it.id })
        }
    }

    // ===== App List =====
    suspend fun sendAppList(apps: List<AppInfo>) {
        api.sendAppList("Bearer ${getAuthToken()}", AppListReport(getDeviceId(), apps))
    }

    // ===== Screenshot =====
    suspend fun uploadScreenshot(imagePart: okhttp3.MultipartBody.Part): Result<ScreenshotResponse> {
        return try {
            val response = api.uploadScreenshot("Bearer ${getAuthToken()}", imagePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== Ack Command =====
    suspend fun acknowledgeCommand(commandId: String) {
        api.acknowledgeCommand("Bearer ${getAuthToken()}", commandId)
    }

    // ===== Fetch Blocked Apps =====
    suspend fun fetchBlockedApps(): List<AppInfo> {
        val response = api.getBlockedApps("Bearer ${getAuthToken()}")
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    // ===== Sync Queue =====
    suspend fun queueSync(type: String, data: String) {
        syncQueueDao.insert(SyncQueueEntity(
            type = type, data = data,
            timestamp = System.currentTimeMillis(), createdAt = System.currentTimeMillis()
        ))
    }

    suspend fun processSyncQueue() {
        val pending = syncQueueDao.getPending()
        pending.forEach { item ->
            // Process based on type
            syncQueueDao.delete(item.id)
        }
    }

    // ===== Device Info =====
    suspend fun saveDeviceInfo() {
        val (totalRam, availRam) = deviceInfoUtils.getRamUsage()
        val (totalStorage, availStorage) = deviceInfoUtils.getStorageUsage()
        deviceInfoDao.insert(DeviceInfoEntity(
            deviceName = deviceInfoUtils.getDeviceName(),
            manufacturer = deviceInfoUtils.getManufacturer(),
            model = deviceInfoUtils.getModel(),
            androidVersion = deviceInfoUtils.getAndroidVersion(),
            apiLevel = deviceInfoUtils.getApiLevel(),
            screenResolution = deviceInfoUtils.getScreenResolution(),
            refreshRate = deviceInfoUtils.getRefreshRate(),
            language = deviceInfoUtils.getLanguage(),
            timeZone = deviceInfoUtils.getTimeZone(),
            cpuInfo = deviceInfoUtils.getCpuInfo(),
            totalRam = totalRam,
            totalStorage = totalStorage,
            updatedAt = System.currentTimeMillis()
        ))
    }
}
