package com.ninezero.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRequest(
    val token: String,
    val deviceInfo: String? = null
)