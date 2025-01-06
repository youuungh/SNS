package com.ninezero.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FileDto(
    val id: Long,
    val fileName: String,
    val createdAt: String,
    val filePath: String
)
