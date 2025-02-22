package com.ninezero.data.model.dto.chat

import com.ninezero.domain.model.chat.ChatMessage
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val content: String,
    val senderId: Long,
    val senderName: String,
    val roomId: String,
    val createdAt: String,
    val leaveTimestamp: String?
)

fun ChatMessageDto.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        content = content,
        senderId = senderId,
        senderName = senderName,
        roomId = roomId,
        createdAt = createdAt,
        leaveTimestamp = leaveTimestamp
    )
}