package com.ninezero.domain.model

data class Notification(
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