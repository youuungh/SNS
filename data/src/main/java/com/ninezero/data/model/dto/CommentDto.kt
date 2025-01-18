package com.ninezero.data.model.dto

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
        userName = createUserName,
        profileImageUrl = profileImageUrl
    )
}