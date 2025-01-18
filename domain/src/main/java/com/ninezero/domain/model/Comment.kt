package com.ninezero.domain.model

data class Comment(
    val id: Long,
    val text: String,
    val userName: String,
    val profileImageUrl: String? = null
)