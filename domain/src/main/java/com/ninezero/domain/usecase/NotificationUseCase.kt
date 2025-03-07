package com.ninezero.domain.usecase

import androidx.paging.PagingData
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NotificationUseCase {
    fun getNotifications(): Flow<PagingData<Notification>>
    suspend fun hasUnreadNotifications(): ApiResult<Boolean>
    suspend fun markAsRead(id: Long): ApiResult<Boolean>
    suspend fun deleteNotification(id: Long): ApiResult<Boolean>
    suspend fun deleteAllNotification(): ApiResult<Boolean>

    val unreadNotificationsState: StateFlow<Boolean>
    fun updateUnreadNotificationsState(hasUnread: Boolean)
}