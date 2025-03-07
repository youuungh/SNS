package com.ninezero.data.usecase

import com.google.firebase.messaging.FirebaseMessaging
import com.ninezero.data.UserDataStore
import com.ninezero.data.ktor.UserService
import com.ninezero.data.model.param.LoginParam
import com.ninezero.data.model.param.SignUpParam
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.AuthUseCase
import com.ninezero.domain.usecase.FCMTokenUseCase
import com.ninezero.domain.usecase.UserUseCase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthUseCaseImpl @Inject constructor(
    private val userService: UserService,
    private val userUseCase: UserUseCase,
    private val fcmTokenUseCase: FCMTokenUseCase,
    private val userDataStore: UserDataStore
) : AuthUseCase {
    override suspend fun login(id: String, password: String): ApiResult<String> = try {
        val loginParam = LoginParam(loginId = id, password = password)
        val response = userService.login(loginParam)

        if (response.result == "SUCCESS") {
            val token = response.data
            if (token != null) {
                setToken(token)

                when (val userResult = userUseCase.getMyUser()) {
                    is ApiResult.Success -> {
                        userUseCase.setMyUserId(userResult.data.id)
                    }
                    else -> {
                        Timber.e("사용자 정보 가져오기 실패")
                    }
                }

                try {
                    val fcmToken = FirebaseMessaging.getInstance().token.await()
                    fcmTokenUseCase.registerToken(fcmToken)
                } catch (e: Exception) {
                    Timber.e(e, "FCM 토큰 등록 실패")
                }

                ApiResult.Success(token)
            } else {
                ApiResult.Error.InvalidRequest("토큰이 없습니다")
            }
        } else {
            ApiResult.Error.InvalidRequest(response.errorMessage ?: "로그인에 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun signUp(id: String, userName: String, password: String): ApiResult<Boolean> = try {
        val signUpParam = SignUpParam(
            loginId = id,
            userName = userName,
            password = password,
            extraUserInfo = "",
            profileImagePath = ""
        )
        val response = userService.signUp(signUpParam)

        if (response.result == "SUCCESS") {
            ApiResult.Success(true)
        } else {
            ApiResult.Error.InvalidRequest(response.errorMessage ?: "회원가입에 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun getToken(): String? {
        return userDataStore.getToken()
    }

    override suspend fun setToken(token: String) {
        userDataStore.setToken(token = token)
    }

    override suspend fun clearToken(): ApiResult<Unit> = try {
        try {
            val token = fcmTokenUseCase.getCurrentToken()
            token?.let {
                fcmTokenUseCase.unregisterToken(token)
            }
        } catch (e: Exception) {
            Timber.e(e, "FCM 토큰 등록 해제 실패")
        }

        userDataStore.clear()
        ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error.ServerError(message = "토큰 삭제 중 오류가 발생했습니다")
    }

    override suspend fun updateOnboardingStatus(isCompleted: Boolean) {
        userDataStore.updateOnboardingStatus(isCompleted)
    }

    override suspend fun hasCompletedOnboarding(): Boolean = userDataStore.hasCompletedOnboarding()
}