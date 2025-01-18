package com.ninezero.presentation.model.feed

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.richeditor.model.RichTextState
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post

@Immutable
data class PostModel(
    val userId: Long,
    val postId: Long,
    val username: String,
    val profileImageUrl: String?,
    val images: List<String>,
    val richTextState: RichTextState,
    val comments: List<Comment>
)

fun Post.toModel(): PostModel {
    return PostModel(
        userId = this.userId,
        postId = this.id,
        username = this.userName,
        profileImageUrl = this.profileImageUrl,
        images = this.images,
        richTextState = RichTextState().apply { setHtml(this@toModel.content) },
        comments = this.comments
    )
}