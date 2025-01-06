package com.ninezero.presentation.auth.login

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userUseCase: UserUseCase
): ViewModel(), ContainerHost<LoginState, LoginSideEffect> {
    override val container: Container<LoginState, LoginSideEffect> = container(initialState = LoginState())

    fun onIdChange(id: String) = blockingIntent {
        reduce {
            state.copy(
                id = id,
                isLoginEnabled = id.isNotBlank() && state.password.isNotBlank()
            )
        }
    }

    fun onPasswordChange(password: String) = blockingIntent {
        reduce {
            state.copy(
                password = password,
                isPasswordError = false,
                isLoginEnabled = state.id.isNotBlank() && password.isNotBlank()
            )
        }
    }

    fun onLogin() = intent {
        reduce { state.copy(isLoading = true) }

        when (val result = userUseCase.login(id = state.id, password = state.password)) {
            is ApiResult.Success -> {
                Timber.d("Login Success: ${result.data}")
                reduce { state.copy(isLoading = false) }
                postSideEffect(LoginSideEffect.NavigateToFeed)
            }
            is ApiResult.Error.InvalidRequest -> {
                Timber.e("Login Invalid Request: ${result.message}")
                reduce {
                    state.copy(
                        isLoading = false,
                        isPasswordError = true
                    )
                }
            }
            is ApiResult.Error -> {
                Timber.e("Login Other Error: ${result.message}")
                reduce { state.copy(isLoading = false) }
                postSideEffect(LoginSideEffect.ShowSnackbar(result.message))
            }
        }
    }
}

@Immutable
data class LoginState(
    val id: String = "",
    val password: String = "",
    val isPasswordError: Boolean = false,
    val isLoginEnabled: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface LoginSideEffect {
    data class ShowSnackbar(val message: String) : LoginSideEffect
    object NavigateToFeed: LoginSideEffect
}