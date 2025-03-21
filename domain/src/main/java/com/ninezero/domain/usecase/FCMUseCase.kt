package com.ninezero.domain.usecase

import com.ninezero.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

sealed class NotificationType(val key: String) {
    object FOLLOW : NotificationType("follow")
    object LIKE : NotificationType("like")
    object COMMENT : NotificationType("comment")
    object REPLY : NotificationType("reply")

    companion object {
        fun fromString(type: String): NotificationType? = when(type) {
            "follow" -> FOLLOW
            "like" -> LIKE
            "comment" -> COMMENT
            "reply" -> REPLY
            else -> null
        }
    }
}

interface FCMUseCase {
    suspend fun registerToken(token: String): ApiResult<Long>
    suspend fun unregisterToken(token: String): ApiResult<Boolean>
    suspend fun getCurrentToken(): String?

    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun getNotificationsEnabled(): Flow<Boolean>

    suspend fun setNotificationType(type: NotificationType, enabled: Boolean)
    fun getNotificationType(type: NotificationType): Flow<Boolean>
}