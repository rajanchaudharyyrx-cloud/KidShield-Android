package com.kidshield.agent.data.remote.api

data class ContactReport(
    val deviceId: String,
    val name: String,
    val phoneNumber: String,
    val email: String?
)
