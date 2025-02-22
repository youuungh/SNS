package com.ninezero.domain.model.chat

data class ChatMessageContent(
    val content: String,
    val roomId: String?,
    val otherUserId: Long?
)