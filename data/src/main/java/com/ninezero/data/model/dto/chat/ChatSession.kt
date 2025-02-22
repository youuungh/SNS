package com.ninezero.data.model.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatSession(
    val userId: Long,
    val userName: String,
    val sessionId: String
)