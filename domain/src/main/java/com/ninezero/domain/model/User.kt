package com.ninezero.domain.model

data class User(
    val id: Long,
    val loginId: String,
    val userName: String,
    val profileImagePath: String?,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)