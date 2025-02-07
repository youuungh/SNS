package com.ninezero.domain.model

data class Post(
    val userId: Long,
    val id: Long,
    val title: String,
    val content: String,
    val images: List<String>,
    val userName: String,
    val profileImageUrl: String,
    val comments: List<Comment>,
    val likesCount: Int,
    val isLiked: Boolean,
    val isFollowing: Boolean,
    val createdAt: String
)