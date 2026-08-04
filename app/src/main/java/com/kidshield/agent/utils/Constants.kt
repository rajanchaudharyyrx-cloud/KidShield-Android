package com.kidshield.agent.utils

object Constants {
    const val BASE_URL = "https://api.kidshield.com/v1/"
    const val WS_URL = "wss://ws.kidshield.com/agent"

    const val PREFS_NAME = "kidshield_secure_prefs"
    const val KEY_PAIRING_CODE = "pairing_code"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_DEVICE_ID = "device_id"
    const val KEY_PARENT_ID = "parent_id"
    const val KEY_IS_PAIRED = "is_paired"
    const val KEY_LAST_SYNC = "last_sync"

    const val HEARTBEAT_INTERVAL_MS = 15000L
    const val LOCATION_UPDATE_INTERVAL_MS = 10000L
    const val SYNC_INTERVAL_MS = 30000L
    const val SCREEN_TIME_CHECK_INTERVAL_MS = 60000L

    const val PAIRING_CODE_LENGTH = 8

    const val DB_NAME = "kidshield.db"
    const val DB_VERSION = 1
}
