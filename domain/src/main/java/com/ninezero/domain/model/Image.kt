package com.ninezero.domain.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class Image(
    val uri: String,
    val name: String,
    val size: Long,
    val mimeType: String
)