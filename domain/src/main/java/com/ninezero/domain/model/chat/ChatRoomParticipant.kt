package com.ninezero.domain.model.chat

data class ChatRoomParticipant(
    val userId: Long,
    val userLoginId: String,
    val userName: String,
    val profileImagePath: String?,
    val unreadCount: Int,
    val lastReadMessageId: String?,
    val leaveTimestamp: String?
)