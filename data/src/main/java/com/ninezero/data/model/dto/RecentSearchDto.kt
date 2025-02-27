package com.ninezero.data.model.dto

import com.ninezero.domain.model.RecentSearch
import kotlinx.serialization.Serializable

@Serializable
data class RecentSearchDto(
    val id: Long,
    val userId: Long,
    val searchedUserId: Long,
    val searchedUserName: String,
    val searchedUserProfileImagePath: String?,
    val searchedAt: String
)

fun RecentSearchDto.toDomain(): RecentSearch {
    return RecentSearch(
        id = id,
        userId = userId,
        searchedUserId = searchedUserId,
        searchedUserName = searchedUserName,
        searchedUserProfileImagePath = searchedUserProfileImagePath,
        searchedAt = searchedAt
    )
}