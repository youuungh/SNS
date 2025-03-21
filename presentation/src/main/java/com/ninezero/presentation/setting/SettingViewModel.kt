package com.ninezero.presentation.setting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.AuthUseCase
import com.ninezero.domain.usecase.FCMUseCase
import com.ninezero.domain.usecase.NotificationType
import com.ninezero.domain.usecase.ThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val fcmUseCase: FCMUseCase,
    private val themeUseCase: ThemeUseCase
) : ViewModel(), ContainerHost<SettingState, SettingSideEffect> {
    override val container: Container<SettingState, SettingSideEffect> = container(initialState = SettingState())

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _followNotifications = MutableStateFlow(true)
    val followNotifications = _followNotifications.asStateFlow()

    private val _likeNotifications = MutableStateFlow(true)
    val likeNotifications = _likeNotifications.asStateFlow()

    private val _commentNotifications = MutableStateFlow(true)
    val commentNotifications = _commentNotifications.asStateFlow()

    private val _replyNotifications = MutableStateFlow(true)
    val replyNotifications = _replyNotifications.asStateFlow()

    init {
        viewModelScope.launch {
            _isDarkTheme.value = themeUseCase.getTheme().first()

            themeUseCase.getTheme().collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }

        viewModelScope.launch {
            fcmUseCase.getNotificationsEnabled().collect { enabled ->
                _notificationsEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            fcmUseCase.getNotificationType(NotificationType.FOLLOW).collect { enabled ->
                _followNotifications.value = enabled
            }
        }

        viewModelScope.launch {
            fcmUseCase.getNotificationType(NotificationType.LIKE).collect { enabled ->
                _likeNotifications.value = enabled
            }
        }

        viewModelScope.launch {
            fcmUseCase.getNotificationType(NotificationType.COMMENT).collect { enabled ->
                _commentNotifications.value = enabled
            }
        }

        viewModelScope.launch {
            fcmUseCase.getNotificationType(NotificationType.REPLY).collect { enabled ->
                _replyNotifications.value = enabled
            }
        }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeUseCase.updateTheme(isDark)
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            fcmUseCase.setNotificationsEnabled(!_notificationsEnabled.value)
        }
    }

    fun toggleFollowNotifications() {
        viewModelScope.launch {
            fcmUseCase.setNotificationType(
                NotificationType.FOLLOW,
                !_followNotifications.value
            )
        }
    }

    fun toggleLikeNotifications() {
        viewModelScope.launch {
            fcmUseCase.setNotificationType(
                NotificationType.LIKE,
                !_likeNotifications.value
            )
        }
    }

    fun toggleCommentNotifications() {
        viewModelScope.launch {
            fcmUseCase.setNotificationType(
                NotificationType.COMMENT,
                !_commentNotifications.value
            )
        }
    }

    fun toggleReplyNotifications() {
        viewModelScope.launch {
            fcmUseCase.setNotificationType(
                NotificationType.REPLY,
                !_replyNotifications.value
            )
        }
    }

    fun onSignOut() = intent {
        reduce { state.copy(isLoading = true) }

        when (val result = authUseCase.clearToken()) {
            is ApiResult.Success -> postSideEffect(SettingSideEffect.NavigateToLogin)
            is ApiResult.Error -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(SettingSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun showSignOutDialog() = intent {
        reduce { state.copy(dialog = SettingDialog.SignOut) }
    }

    fun hideDialog() = intent {
        reduce { state.copy(dialog = SettingDialog.Hidden) }
    }
}

@Immutable
data class SettingState(
    val isLoading: Boolean = false,
    val dialog: SettingDialog = SettingDialog.Hidden
)

sealed interface SettingDialog {
    data object Hidden : SettingDialog
    data object SignOut : SettingDialog
}

sealed interface SettingSideEffect {
    data class ShowSnackbar(val message: String) : SettingSideEffect
    object NavigateToLogin : SettingSideEffect
}