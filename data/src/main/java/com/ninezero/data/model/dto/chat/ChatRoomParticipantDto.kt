package com.ninezero.data.model.dto.chat

import com.ninezero.domain.model.chat.ChatRoomParticipant
import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomParticipantDto(
    val userId: Long,
    val userLoginId: String,
    val userName: String,
    val profileImagePath: String?,
    val unreadCount: Int,
    val lastReadMessageId: String?,
    val leaveTimestamp: String?
)

fun ChatRoomParticipantDto.toDomain(): ChatRoomParticipant {
    return ChatRoomParticipant(
        userId = userId,
        userLoginId = userLoginId,
        userName = userName,
        profileImagePath = profileImagePath,
        unreadCount = unreadCount,
        lastReadMessageId = lastReadMessageId,
        leaveTimestamp = leaveTimestamp
    )
}