package com.ninezero.presentation.auth.signup

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
class SignUpViewModel @Inject constructor(
    private val userUseCase: UserUseCase
): ViewModel(), ContainerHost<SignUpState, SignUpSideEffect> {
    override val container: Container<SignUpState, SignUpSideEffect> = container(initialState = SignUpState())

    fun onIdChange(id: String) = blockingIntent {
        reduce {
            state.copy(
                id = id,
                isSignUpEnabled = checkSignUpEnabled(
                    id = id,
                    username = state.username,
                    password = state.password,
                    passwordConfirm = state.passwordConfirm
                )
            )
        }
    }

    fun onUsernameChange(username: String) = blockingIntent {
        reduce {
            state.copy(
                username = username,
                isSignUpEnabled = checkSignUpEnabled(
                    id = state.id,
                    username = username,
                    password = state.password,
                    passwordConfirm = state.passwordConfirm
                )
            )
        }
    }

    fun onPasswordChange(password: String) = blockingIntent {
        reduce {
            state.copy(
                password = password,
                isPasswordConfirmError = state.passwordConfirm.isNotEmpty() && password != state.passwordConfirm,
                isSignUpEnabled = checkSignUpEnabled(
                    id = state.id,
                    username = state.username,
                    password = password,
                    passwordConfirm = state.passwordConfirm
                )
            )
        }
    }

    fun onPasswordConfirmChange(confirmPassword: String) = blockingIntent {
        reduce {
            state.copy(
                passwordConfirm = confirmPassword,
                isPasswordConfirmError = confirmPassword.isNotEmpty() && state.password != confirmPassword,
                isSignUpEnabled = checkSignUpEnabled(
                    id = state.id,
                    username = state.username,
                    password = state.password,
                    passwordConfirm = confirmPassword
                )
            )
        }
    }

    fun onSignUpClick() = intent {
        if (state.password != state.passwordConfirm) {
            reduce { state.copy(isPasswordConfirmError = true) }
            return@intent
        }

        reduce { state.copy(isLoading = true) }

        when (val result = userUseCase.signUp(
            id = state.id,
            username = state.username,
            password = state.password
        )) {
            is ApiResult.Success -> {
                Timber.d("SignUp Success: ${result.data}")
                reduce { state.copy(isLoading = false) }
                postSideEffect(SignUpSideEffect.NavigateToLogin)
                postSideEffect(SignUpSideEffect.ShowSnackbar(message = "회원가입에 성공했습니다"))
            }
            is ApiResult.Error.InvalidRequest -> {
                Timber.e("SignUp Invalid Request: ${result.message}")
                reduce { state.copy(isLoading = false) }
                postSideEffect(SignUpSideEffect.ShowSnackbar(result.message))
            }
            is ApiResult.Error -> {
                Timber.e("SignUp Other Error: ${result.message}")
                reduce { state.copy(isLoading = false) }
                postSideEffect(SignUpSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    private fun checkSignUpEnabled(
        id: String,
        username: String,
        password: String,
        passwordConfirm: String
    ): Boolean {
        return id.isNotBlank() &&
                username.isNotBlank() &&
                password.isNotBlank() &&
                passwordConfirm.isNotBlank() &&
                password == passwordConfirm
    }
}

@Immutable
data class SignUpState(
    val id: String = "",
    val username: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isPasswordConfirmError: Boolean = false,
    val isSignUpEnabled: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface SignUpSideEffect {
    data class ShowSnackbar(val message: String) : SignUpSideEffect
    object NavigateToLogin: SignUpSideEffect
}