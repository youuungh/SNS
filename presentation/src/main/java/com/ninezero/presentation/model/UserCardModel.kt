package com.ninezero.presentation.model

import androidx.compose.runtime.Immutable
import com.ninezero.domain.model.User

@Immutable
data class UserCardModel(
    val userId: Long,
    val userLoginId: String,
    val userName: String,
    val profileImagePath: String?,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)

fun User.toModel(): UserCardModel {
    return UserCardModel(
        userId = this.id,
        userLoginId = this.loginId,
        userName = this.userName,
        profileImagePath = this.profileImagePath,
        postCount = this.postCount,
        followerCount = this.followerCount,
        followingCount = this.followingCount,
        isFollowing = this.isFollowing
    )
}