package com.kidshield.agent.data.remote.api

data class ScreenshotResponse(
    val success: Boolean,
    val imageUrl: String?,
    val message: String?
)
