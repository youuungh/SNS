package com.ninezero.data.model.dto.chat

import com.ninezero.domain.model.chat.ChatMessageContent
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageRequest(
    val content: String,
    val roomId: String?,
    val otherUserId: Long?
)

fun ChatMessageRequest.toDomain(): ChatMessageContent {
    return ChatMessageContent(
        content = content,
        roomId = roomId,
        otherUserId = otherUserId
    )
}