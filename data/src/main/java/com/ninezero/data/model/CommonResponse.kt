package com.ninezero.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CommonResponse<T>(
    val result: String,
    val data: T? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)