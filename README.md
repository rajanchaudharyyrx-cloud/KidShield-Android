# Kid Shield - Kids Device Agent

## Overview
Kid Shield is an Android parental control agent app that connects to a Parent App via an 8-digit pairing code. No login, signup, OTP, or email required.

## Features
- 8-digit pairing code system
- Real-time location tracking
- App usage monitoring
- Screen time limits
- App blocking
- Notification reading
- Screenshot capture
- Live screen streaming
- Camera access (front/rear)
- Audio streaming
- File browsing
- Contacts, SMS, Call logs
- Device commands (lock, alarm, lost mode)
- Foreground service with auto-restart
- WebSocket real-time connection
- Offline queue & auto-sync
- Encrypted local storage

## Tech Stack
- Kotlin
- Jetpack Compose
- MVVM Architecture
- Hilt DI
- Room Database
- Retrofit
- WebSocket
- WorkManager
- Foreground Service
- Accessibility Service

## Setup Instructions

### 1. Open in Android Studio
- File → Open → Select the `KidShield` folder
- Wait for Gradle sync

### 2. Update API Base URL
Open `app/src/main/java/com/kidshield/agent/utils/Constants.kt` and update:
```kotlin
const val BASE_URL = "https://your-api-domain.com/v1/"
const val WS_URL = "wss://your-ws-domain.com/agent"
```

### 3. Build & Run
- Connect an Android device (API 26+)
- Click Run ▶️

### 4. Pairing
- Launch app on child device
- Enter 8-digit pairing code from Parent App
- Grant all requested permissions

## Required Permissions
- Accessibility Service
- Usage Access
- Notification Access
- Overlay Permission
- Ignore Battery Optimization
- Location
- Camera
- Microphone
- Storage
- Phone State
- Contacts
- SMS
- Call Log

## Project Structure
```
app/src/main/java/com/kidshield/agent/
├── data/
│   ├── local/          # Room Database, Entities, DAOs
│   ├── remote/         # API, WebSocket
│   └── repository/     # Repository Pattern
├── di/                 # Hilt Modules
├── service/            # Foreground, Accessibility, Notification
├── receiver/           # Boot, Network, Device Admin
├── ui/                 # Activities, ViewModels, Screens
└── utils/              # Utilities, Constants
```

## Security
- EncryptedSharedPreferences
- HTTPS with certificate pinning
- Token-based authentication
- Root detection
- Anti-debug
- Obfuscation (ProGuard)

## License
Proprietary - For authorized parental control use only.
