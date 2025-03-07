package com.ninezero.data.model.dto

import com.ninezero.domain.model.Notification
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Long,
    val type: String,
    val body: String,
    val senderId: Long?,
    val senderLoginId: String?,
    val senderName: String?,
    val senderProfileImagePath: String?,
    val boardId: Long?,
    val commentId: Long?,
    val roomId: String?,
    val isRead: Boolean,
    val createdAt: String
)

fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        type = type,
        body = body,
        senderId = senderId,
        senderLoginId = senderLoginId,
        senderName = senderName,
        senderProfileImagePath = senderProfileImagePath,
        boardId = boardId,
        commentId = commentId,
        roomId = roomId,
        isRead = isRead,
        createdAt = createdAt
    )
}