package com.ninezero.data.model

import com.ninezero.domain.model.Comment
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: Long,
    val comment: String,
    val createdAt: String,
    val createUserId: Long,
    val createUserName: String,
    val profileImageUrl: String
)

fun CommentDto.toDomain() : Comment {
    return Comment(
        id = id,
        text = comment,
        username = createUserName,
        profileImageUrl = profileImageUrl
    )
}