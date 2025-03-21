package com.ninezero.data.model.param

import kotlinx.serialization.Serializable

@Serializable
data class SocialLoginParam(
    val token: String,
    val provider: String
)