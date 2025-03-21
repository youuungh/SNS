package com.ninezero.domain.model.chat

import androidx.compose.runtime.Stable

@Stable
data class ChatRoom(
    val id: String,
    val name: String,
    val participants: List<ChatRoomParticipant>,
    val lastMessage: ChatMessage?,
    val messageCount: Int,
    val createdAt: String
)