package com.ninezero.domain.model

import androidx.compose.runtime.Stable

@Stable
data class Post(
    val userId: Long,
    val id: Long,
    val title: String,
    val content: String,
    val images: List<String>,
    val userName: String,
    val profileImageUrl: String,
    val comments: List<Comment>,
    val commentCount: Int,
    val likesCount: Int,
    val isLiked: Boolean,
    val isFollowing: Boolean,
    val isSaved: Boolean,
    val createdAt: String
)