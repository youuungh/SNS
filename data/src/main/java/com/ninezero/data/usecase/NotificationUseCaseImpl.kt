package com.ninezero.data.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ninezero.data.db.notification.paging.NotificationPagingSource
import com.ninezero.data.ktor.NotificationService
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Notification
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.NotificationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUseCaseImpl @Inject constructor(
    private val notificationService: NotificationService,
    private val networkRepository: NetworkRepository
) : NotificationUseCase {

    private val _unreadNotificationsState = MutableStateFlow(false)
    override val unreadNotificationsState: StateFlow<Boolean> = _unreadNotificationsState.asStateFlow()

    override fun updateUnreadNotificationsState(hasUnread: Boolean) {
        _unreadNotificationsState.value = hasUnread
    }

    override fun getNotifications(): Flow<PagingData<Notification>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = 2
            )
        ) {
            NotificationPagingSource(notificationService)
        }.flow
    }

    override suspend fun hasUnreadNotifications(): ApiResult<Boolean> {
        return try {
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val response = notificationService.hasUnreadNotifications()
            if (response.result == "SUCCESS") {
                updateUnreadNotificationsState(response.data == true)
                ApiResult.Success(response.data == true)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "알림 상태 확인 실패")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun markAsRead(id: Long): ApiResult<Boolean> {
        return try {
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val response = notificationService.markAsRead(id)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "알림 읽음 처리 실패")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun deleteNotification(id: Long): ApiResult<Boolean> {
        return try {
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val response = notificationService.deleteNotification(id)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "알림 삭제 실패")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun deleteAllNotification(): ApiResult<Boolean> {
        return try {
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val response = notificationService.deleteAllNotification()
            if (response.result == "SUCCESS") {
                updateUnreadNotificationsState(false)
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "알림 전체 삭제 실패")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}