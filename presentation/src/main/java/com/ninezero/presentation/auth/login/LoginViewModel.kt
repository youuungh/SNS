package com.ninezero.presentation.auth.login

import android.app.Activity
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.suspendCancellableCoroutine
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
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

        when (val result = authUseCase.login(id = state.id, password = state.password)) {
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
                Timber.e("Login Error: ${result.message}")
                reduce { state.copy(isLoading = false) }
                postSideEffect(LoginSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun onGoogleLogin() = intent {
        reduce { state.copy(isLoading = true) }

        when (val tokenResult = authUseCase.getGoogleIdToken()) {
            is ApiResult.Success -> {
                when (val loginResult = authUseCase.socialLogin(token = tokenResult.data, provider = "google")) {
                    is ApiResult.Success -> {
                        Timber.d("Google 로그인 성공")
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(LoginSideEffect.NavigateToFeed)
                    }
                    is ApiResult.Error -> {
                        Timber.e("로그인 에러: ${loginResult.message}")
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(LoginSideEffect.ShowSnackbar(loginResult.message))
                    }
                }
            }
            is ApiResult.Error -> {
                Timber.e("Firebase 토큰 획득 실패: ${tokenResult.message}")
                reduce { state.copy(isLoading = false) }
                postSideEffect(LoginSideEffect.ShowSnackbar(tokenResult.message))
            }
        }
    }

    fun onNaverLogin(activity: Activity) = intent {
        reduce { state.copy(isLoading = true) }

        try {
            val token = startNaverLogin(activity)

            when (val loginResult = authUseCase.socialLogin(token = token, provider = "naver")) {
                is ApiResult.Success -> {
                    Timber.d("네이버 로그인 성공")
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(LoginSideEffect.NavigateToFeed)
                }
                is ApiResult.Error -> {
                    Timber.e("로그인 에러: ${loginResult.message}")
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(LoginSideEffect.ShowSnackbar(loginResult.message))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "네이버 로그인 처리 중 오류")
            reduce { state.copy(isLoading = false) }
            postSideEffect(LoginSideEffect.ShowSnackbar("네이버 로그인 실패: ${e.message}"))
        }
    }

    fun onKakaoLogin() = intent {
        reduce { state.copy(isLoading = true) }

        try {
            when (val tokenResult = authUseCase.getKakaoIdToken()) {
                is ApiResult.Success -> {
                    when (val loginResult = authUseCase.socialLogin(token = tokenResult.data, provider = "kakao")) {
                        is ApiResult.Success -> {
                            Timber.d("카카오 로그인 성공")
                            reduce { state.copy(isLoading = false) }
                            postSideEffect(LoginSideEffect.NavigateToFeed)
                        }
                        is ApiResult.Error -> {
                            Timber.e("로그인 에러: ${loginResult.message}")
                            reduce { state.copy(isLoading = false) }
                            postSideEffect(LoginSideEffect.ShowSnackbar(loginResult.message))
                        }
                    }
                }
                is ApiResult.Error -> {
                    Timber.e("카카오 토큰 획득 실패: ${tokenResult.message}")
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(LoginSideEffect.ShowSnackbar(tokenResult.message))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "카카오 로그인 처리 중 오류")
            reduce { state.copy(isLoading = false) }
            postSideEffect(LoginSideEffect.ShowSnackbar("${e.message}"))
        }
    }

    private suspend fun startNaverLogin(activity: Activity): String = suspendCancellableCoroutine { continuation ->
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                val accessToken = NaverIdLoginSDK.getAccessToken()
                if (accessToken != null) {
                    continuation.resumeWith(Result.success(accessToken))
                } else {
                    continuation.resumeWithException(IllegalStateException("네이버 토큰이 null입니다"))
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                continuation.resumeWithException(IllegalStateException(message))
            }

            override fun onError(errorCode: Int, message: String) {
                continuation.resumeWithException(IllegalStateException(message))
            }
        }

        NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
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