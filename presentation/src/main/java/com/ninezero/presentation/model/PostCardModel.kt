package com.ninezero.presentation.model

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.richeditor.model.RichTextState
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post

@Immutable
data class PostCardModel(
    val userId: Long,
    val postId: Long,
    val userName: String,
    val profileImageUrl: String?,
    val images: List<String>,
    val richTextState: RichTextState,
    val comments: List<Comment>,
    val likesCount: Int,
    val isLiked: Boolean,
    val createdAt: String
)

fun Post.toModel(): PostCardModel {
    return PostCardModel(
        userId = this.userId,
        postId = this.id,
        userName = this.userName,
        profileImageUrl = this.profileImageUrl,
        images = this.images,
        richTextState = RichTextState().apply { setHtml(this@toModel.content) },
        comments = this.comments,
        likesCount = this.likesCount,
        isLiked = this.isLiked,
        createdAt = this.createdAt
    )
}