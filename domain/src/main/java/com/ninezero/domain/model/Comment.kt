package com.ninezero.domain.model

data class Comment(
    val id: Long,
    val userId: Long,
    val text: String,
    val userName: String,
    val profileImageUrl: String?,
    val parentId: Long?,
    val parentUserName: String?,
    val depth: Int,
    val replyCount: Int,
    val isExpanded: Boolean = false,
    val replies: List<Comment> = emptyList()
)