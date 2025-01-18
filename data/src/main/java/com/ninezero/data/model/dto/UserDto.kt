package com.ninezero.data.model.dto

import com.ninezero.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val loginId: String,
    val userName: String,
    val extraUserInfo: String,
    val profileImagePath: String
)

fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        loginId = this.loginId,
        userName = this.userName,
        profileImagePath = this.profileImagePath
    )
}