package com.ninezero.presentation.notification

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Notification
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.NotificationUseCase
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    val notificationUseCase: NotificationUseCase,
    private val userUseCase: UserUseCase,
    private val networkRepository: NetworkRepository
) : ViewModel(), ContainerHost<NotificationState, NotificationSideEffect> {
    override val container: Container<NotificationState, NotificationSideEffect> = container(NotificationState())

    val hasUnreadNotifications = notificationUseCase.unreadNotificationsState

    private var isInitialLoad = true

    init {
        viewModelScope.launch {
            networkRepository.observeNetworkConnection()
                .collect { isOnline ->
                    if (isOnline && !isInitialLoad) {
                        load()
                    }
                }
        }

        viewModelScope.launch {
            load()
            isInitialLoad = false
        }
    }

    fun load() = intent {
        try {
            val isOnline = networkRepository.isNetworkAvailable()
            val myUserId = userUseCase.getMyUserId()

            reduce { state.copy(myUserId = myUserId) }

            if (isOnline) {
                val notifications = notificationUseCase.getNotifications()
                    .cachedIn(viewModelScope)

                reduce {
                    state.copy(
                        notifications = notifications,
                        isLoading = false,
                        isRefreshing = false
                    )
                }

                checkUnreadNotifications()
            } else {
                reduce {
                    state.copy(
                        isLoading = false,
                        isError = true
                    )
                }
                postSideEffect(NotificationSideEffect.ShowSnackbar("네트워크 연결 오류"))
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, isRefreshing = false, isError = true) }
            Timber.e(e)
        }
    }

    private fun checkUnreadNotifications() {
        viewModelScope.launch {
            try {
                notificationUseCase.hasUnreadNotifications()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        delay(1000)
        load()
    }

    fun markAsRead(id: Long) = intent {
        try {
            reduce { state.copy(readNotificationIds = state.readNotificationIds + id) }

            when (val result = notificationUseCase.markAsRead(id)) {
                is ApiResult.Success -> {
                    checkUnreadNotifications()
                }
                is ApiResult.Error -> {
                    Timber.e(result.message)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun deleteNotification(id: Long) = intent {
        hideDeleteDialog()

        when (val result = notificationUseCase.deleteNotification(id)) {
            is ApiResult.Success -> {
                reduce {
                    state.copy(deletedNotificationIds = state.deletedNotificationIds + id)
                }
                postSideEffect(NotificationSideEffect.ShowSnackbar("알림이 삭제되었습니다"))
                checkUnreadNotifications()
            }
            is ApiResult.Error -> {
                postSideEffect(NotificationSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun deleteAllNotification() = intent {
        hideDeleteAllDialog()

        when (val result = notificationUseCase.deleteAllNotification()) {
            is ApiResult.Success -> {
                reduce {
                    state.copy(deletedNotificationIds = emptySet())
                }
                postSideEffect(NotificationSideEffect.ShowSnackbar("알림이 전체 삭제되었습니다"))
                load()
                checkUnreadNotifications()
            }
            is ApiResult.Error -> {
                postSideEffect(NotificationSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun handleNotificationClick(notification: Notification) = intent {
        markAsRead(notification.id)

        when (notification.type) {
            "like" -> {
                notification.boardId?.let { postId ->
                    postSideEffect(NotificationSideEffect.NavigateToPost(state.myUserId, postId))
                }
            }
            "comment", "reply" -> {
                notification.boardId?.let { postId ->
                    postSideEffect(NotificationSideEffect.NavigateToPost(
                        state.myUserId,
                        postId,
                        showComments = true,
                        commentId = notification.commentId
                    ))
                }
            }
            "follow" -> {
                notification.senderId?.let { userId ->
                    postSideEffect(NotificationSideEffect.NavigateToUser(userId))
                }
            }
            "chat" -> {
                postSideEffect(
                    NotificationSideEffect.NavigateToChat(
                        otherUserId = notification.senderId!!,
                        roomId = notification.roomId!!,
                        otherUserLoginId = notification.senderLoginId!!,
                        otherUserName = notification.senderName!!,
                        otherUserProfilePath = notification.senderProfileImagePath,
                        myUserId = state.myUserId
                    )
                )
            }
        }
    }

    fun showDeleteDialog(notificationId: Long) = intent {
        reduce { state.copy(dialog = NotificationDialog.DeleteNotification(notificationId)) }
    }

    fun hideDeleteDialog() = intent {
        reduce { state.copy(dialog = NotificationDialog.Hidden) }
    }

    fun showDeleteAllDialog() = intent {
        reduce { state.copy(dialog = NotificationDialog.DeleteAllNotifications) }
    }

    fun hideDeleteAllDialog() = intent {
        reduce { state.copy(dialog = NotificationDialog.Hidden) }
    }

    fun showDeleteBottomSheet(notificationId: Long) = intent {
        reduce { state.copy(deleteBottomSheetNotificationId = notificationId) }
    }

    fun hideDeleteBottomSheet() = intent {
        reduce { state.copy(deleteBottomSheetNotificationId = null) }
    }

    fun onResume() = intent {
        checkUnreadNotifications()
    }
}

@Immutable
data class NotificationState(
    val myUserId: Long = -1L,
    val notifications: Flow<PagingData<Notification>> = emptyFlow(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val readNotificationIds: Set<Long> = emptySet(),
    val deletedNotificationIds: Set<Long> = emptySet(),
    val deleteBottomSheetNotificationId: Long? = null,
    val dialog: NotificationDialog = NotificationDialog.Hidden
)

sealed interface NotificationDialog {
    data object Hidden : NotificationDialog
    data class DeleteNotification(val notificationId: Long) : NotificationDialog
    data object DeleteAllNotifications : NotificationDialog
}

sealed interface NotificationSideEffect {
    data class ShowSnackbar(val message: String) : NotificationSideEffect
    data class NavigateToUser(val userId: Long) : NotificationSideEffect
    data class NavigateToChat(
        val otherUserId: Long,
        val roomId: String,
        val otherUserLoginId: String,
        val otherUserName: String,
        val otherUserProfilePath: String?,
        val myUserId: Long
    ) : NotificationSideEffect
    data class NavigateToPost(
        val userId: Long,
        val postId: Long,
        val showComments: Boolean = false,
        val commentId: Long? = null
    ) : NotificationSideEffect
}