package com.ninezero.domain.model

data class RecentSearch(
    val id: Long,
    val userId: Long,
    val searchedUserId: Long,
    val searchedUserName: String,
    val searchedUserProfileImagePath: String?,
    val searchedAt: String
)