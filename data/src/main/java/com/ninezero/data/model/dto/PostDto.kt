package com.ninezero.data.model.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ninezero.data.model.param.ContentParam
import com.ninezero.domain.model.Post
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity
@Serializable
data class PostDto(
    @PrimaryKey val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val createUserId: Long,
    val createUserName: String,
    val createUserProfileImagePath: String,
    val comments: List<CommentDto>,
    val commentCount: Int,
    val likesCount: Int,
    val isLiked: Boolean,
    val isFollowing: Boolean,
    val isSaved: Boolean,
    @kotlinx.serialization.Transient
    val isMyPost: Boolean = false
)

fun PostDto.toDomain(): Post {
    val contentParam = Json.decodeFromString<ContentParam>(content)
    return Post(
        userId = this.createUserId,
        id = this.id,
        title = this.title,
        content = contentParam.text,
        images = contentParam.images,
        userName = this.createUserName,
        profileImageUrl = this.createUserProfileImagePath,
        comments = this.comments.map { it.toDomain() },
        commentCount = this.commentCount,
        likesCount = this.likesCount,
        isLiked = this.isLiked,
        isFollowing = this.isFollowing,
        isSaved = this.isSaved,
        createdAt = this.createdAt
    )
}