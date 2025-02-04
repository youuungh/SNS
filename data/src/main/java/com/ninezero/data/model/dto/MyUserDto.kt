package com.ninezero.data.model.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ninezero.domain.model.User

@Entity
data class MyUserDto(
    @PrimaryKey val id: Long,
    val userName: String,
    val profileImagePath: String?,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

fun MyUserDto.toDomain(): User {
    return User(
        id = id,
        loginId = "",
        userName = userName,
        profileImagePath = profileImagePath,
        postCount = postCount,
        followerCount = followerCount,
        followingCount = followingCount,
        isFollowing = false
    )
}

fun User.toDto(): MyUserDto {
    return MyUserDto(
        id = id,
        userName = userName,
        profileImagePath = profileImagePath,
        postCount = postCount,
        followerCount = followerCount,
        followingCount = followingCount
    )
}