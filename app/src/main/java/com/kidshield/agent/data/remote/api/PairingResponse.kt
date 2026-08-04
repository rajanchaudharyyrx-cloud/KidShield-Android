package com.kidshield.agent.data.remote.api

data class PairingResponse(
    val success: Boolean,
    val authToken: String,
    val parentId: String,
    val message: String
)
