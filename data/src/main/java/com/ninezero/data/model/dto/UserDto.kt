package com.ninezero.data.model.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ninezero.domain.model.User
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UserDto(
    @PrimaryKey val id: Long,
    val loginId: String,
    val userName: String,
    val extraUserInfo: String,
    val profileImagePath: String,
    val boardCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)

fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        loginId = this.loginId,
        userName = this.userName,
        profileImagePath = this.profileImagePath,
        postCount = this.boardCount,
        followerCount = this.followerCount,
        followingCount = this.followingCount,
        isFollowing = this.isFollowing
    )
}