package com.ninezero.presentation.profile

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.withTimeout
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel(), ContainerHost<ProfileState, ProfileSideEffect> {
    override val container: Container<ProfileState, ProfileSideEffect> = container(initialState = ProfileState())

    init {
        load()
    }

    private fun load() = intent {
        reduce { state.copy(isLoading = true, hasError = false) }

        when (val result = userUseCase.getMyUser()) {
            is ApiResult.Success -> {
                reduce {
                    state.copy(
                        profileImageUrl = result.data.profileImagePath,
                        username = result.data.userName,
                        isLoading = false
                    )
                }
            }
            is ApiResult.Error -> {
                reduce { state.copy(isLoading = false, hasError = true) }
                postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun refresh() = load()

    fun onImageChange(uri: Uri?, onSuccess: () -> Unit) = intent {
        uri?.toString()?.let {
            when (val result = userUseCase.setProfileImage(it)) {
                is ApiResult.Success -> {
                    onSuccess()
                    load()
                }
                is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun onUsernameChange(username: String) = intent {
        reduce { state.copy(isLoading = true) }

        when (val result = userUseCase.setMyUser(userName = username, profileImagePath = null)) {
            is ApiResult.Success -> load()
            is ApiResult.Error -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun onSignOut() = intent {
        reduce { state.copy(isLoading = true) }

        when (val result = userUseCase.clearToken()) {
            is ApiResult.Success -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(ProfileSideEffect.NavigateToLogin)
            }
            is ApiResult.Error -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
            }
        }
    }
}

@Immutable
data class ProfileState(
    val profileImageUrl: String? = null,
    val username: String = "",
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)

sealed interface ProfileDialog {
    data object Hidden : ProfileDialog
    data class EditUsername(val initialUsername: String) : ProfileDialog
    data object SignOut : ProfileDialog
}

sealed interface ProfileSideEffect {
    data class ShowSnackbar(val message: String) : ProfileSideEffect
    object NavigateToLogin : ProfileSideEffect
}