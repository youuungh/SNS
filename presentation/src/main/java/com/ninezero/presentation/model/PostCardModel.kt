package com.ninezero.presentation.model

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.richeditor.model.RichTextState
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post

@Immutable
data class PostCardModel(
    val postId: Long,
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
    val images: List<String>,
    val richTextState: RichTextState,
    val comments: List<Comment>,
    val commentCount: Int,
    val likesCount: Int,
    val isLiked: Boolean,
    val isFollowing: Boolean,
    val isSaved: Boolean,
    val createdAt: String
)

fun Post.toModel(): PostCardModel {
    return PostCardModel(
        postId = this.id,
        userId = this.userId,
        userName = this.userName,
        profileImageUrl = this.profileImageUrl,
        images = this.images,
        richTextState = RichTextState().apply { setHtml(this@toModel.content) },
        comments = this.comments,
        commentCount = this.commentCount,
        likesCount = this.likesCount,
        isLiked = this.isLiked,
        isFollowing = this.isFollowing,
        isSaved = this.isSaved,
        createdAt = this.createdAt
    )
}