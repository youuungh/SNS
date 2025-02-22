package com.ninezero.data.model.dto.chat

import com.ninezero.domain.model.chat.ChatRoom
import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomDto(
    val id: String,
    val name: String,
    val participants: List<ChatRoomParticipantDto>,
    val lastMessage: ChatMessageDto?,
    val messageCount: Int,
    val createdAt: String
)

fun ChatRoomDto.toDomain(): ChatRoom {
    return ChatRoom(
        id = id,
        name = name,
        participants = participants.map { it.toDomain() },
        lastMessage = lastMessage?.toDomain(),
        messageCount = messageCount,
        createdAt = createdAt
    )
}