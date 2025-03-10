package com.ninezero.data.model.dto

import com.ninezero.domain.model.Comment
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: Long,
    val comment: String,
    val parentId: Long?,
    val parentUserName: String?,
    val depth: Int,
    val mentionedUserIds: List<Long>?,
    val replyCount: Int,
    val replyToCommentId: Long?,
    val replyToUserName: String?,
    val createdAt: String,
    val createUserId: Long,
    val createUserName: String,
    val profileImageUrl: String
)

fun CommentDto.toDomain() : Comment {
    return Comment(
        id = id,
        userId = createUserId,
        text = comment,
        userName = createUserName,
        profileImageUrl = profileImageUrl,
        parentId = parentId,
        parentUserName = parentUserName,
        depth = depth,
        replyCount = replyCount,
        mentionedUserIds = mentionedUserIds,
        replyToCommentId = replyToCommentId,
        replyToUserName = replyToUserName
    )
}