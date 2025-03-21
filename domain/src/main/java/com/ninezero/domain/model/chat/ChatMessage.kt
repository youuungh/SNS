package com.ninezero.domain.model.chat

import androidx.compose.runtime.Stable

@Stable
data class ChatMessage(
    val id: String,
    val content: String,
    val senderId: Long,
    val senderName: String,
    val roomId: String,
    val createdAt: String,
    val leaveTimestamp: String?
)