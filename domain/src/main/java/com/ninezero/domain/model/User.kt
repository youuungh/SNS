package com.ninezero.domain.model

data class User(
    val id: Long,
    val loginId: String,
    val userName: String,
    val profileImagePath: String? = null,
)