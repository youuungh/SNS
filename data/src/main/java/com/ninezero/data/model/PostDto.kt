package com.ninezero.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ninezero.domain.model.Post
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(tableName = "posts")
@Serializable
data class PostDto(
    @PrimaryKey val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val createUserId: Long,
    val createUserName: String,
    val createUserProfileFilePath: String,
    val commentList: List<CommentDto>
)

fun PostDto.toDomain(): Post {
    val contentParam = Json.decodeFromString<ContentParam>(content)
    return Post(
        userId = this.createUserId,
        id = this.id,
        title = this.title,
        content = contentParam.text,
        images = contentParam.images,
        username = this.createUserName,
        profileImageUrl = this.createUserProfileFilePath,
        comments = this.commentList.map { it.toDomain() }
    )
}