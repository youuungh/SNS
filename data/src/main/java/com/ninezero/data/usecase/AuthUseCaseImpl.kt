package com.ninezero.data.usecase

import com.google.firebase.messaging.FirebaseMessaging
import com.ninezero.data.UserDataStore
import com.ninezero.data.ktor.SocialAuthManager
import com.ninezero.data.ktor.UserService
import com.ninezero.data.model.param.LoginParam
import com.ninezero.data.model.param.SignUpParam
import com.ninezero.data.model.param.SocialLoginParam
import com.ninezero.data.util.Constants.DEFAULT
import com.ninezero.data.util.Constants.GOOGLE
import com.ninezero.data.util.Constants.KAKAO
import com.ninezero.data.util.Constants.NAVER
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.AuthUseCase
import com.ninezero.domain.usecase.FCMUseCase
import com.ninezero.domain.usecase.UserUseCase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthUseCaseImpl @Inject constructor(
    private val userService: UserService,
    private val userUseCase: UserUseCase,
    private val fcmUseCase: FCMUseCase,
    private val socialAuthManager: SocialAuthManager,
    private val userDataStore: UserDataStore
) : AuthUseCase {
    override suspend fun login(id: String, password: String): ApiResult<String> = try {
        val loginParam = LoginParam(loginId = id, password = password)
        val response = userService.login(loginParam)

        if (response.result == "SUCCESS") {
            val token = response.data
            if (token != null) {
                setToken(token)
                userDataStore.setLoginType(DEFAULT)

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
                    fcmUseCase.registerToken(fcmToken)
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

    override suspend fun socialLogin(token: String, provider: String): ApiResult<String> = try {
        val socialLoginParam = SocialLoginParam(token = token, provider = provider)
        val response = userService.socialLogin(socialLoginParam)

        if (response.result == "SUCCESS") {
            val serverToken = response.data
            if (serverToken != null) {
                setToken(serverToken)

                val loginType = when (provider) {
                    "google" -> GOOGLE
                    "naver" -> NAVER
                    "kakao" -> KAKAO
                    else -> provider
                }
                userDataStore.setLoginType(loginType)

                when (val userResult = userUseCase.getMyUser()) {
                    is ApiResult.Success -> {
                        userUseCase.setMyUserId(userResult.data.id)
                    }
                    else -> {
                        Timber.e("소셜 로그인 후 사용자 정보 가져오기 실패")
                    }
                }

                try {
                    val fcmToken = FirebaseMessaging.getInstance().token.await()
                    fcmUseCase.registerToken(fcmToken)
                } catch (e: Exception) {
                    Timber.e(e, "FCM 토큰 등록 실패")
                }

                ApiResult.Success(serverToken)
            } else {
                ApiResult.Error.ServerError("서버에서 토큰을 반환하지 않았습니다")
            }
        } else {
            ApiResult.Error.ServerError(response.errorMessage ?: "소셜 로그인에 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun getGoogleIdToken(): ApiResult<String> = try {
        val googleIdToken = socialAuthManager.getGoogleIdToken()
        val firebaseIdToken = socialAuthManager.firebaseAuthWithGoogle(googleIdToken)

        ApiResult.Success(firebaseIdToken)
    } catch (e: Exception) {
        Timber.e(e)
        ApiResult.Error.ServerError("${e.message}")
    }

    override suspend fun getKakaoIdToken(): ApiResult<String> = try {
        val kakaoIdToken = socialAuthManager.getKakaoIdToken()
        ApiResult.Success(kakaoIdToken)
    } catch (e: Exception) {
        Timber.e(e)
        ApiResult.Error.ServerError("${e.message}")
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
            val token = fcmUseCase.getCurrentToken()
            token?.let {
                fcmUseCase.unregisterToken(token)
            }
        } catch (e: Exception) {
            Timber.e(e, "FCM 토큰 등록 해제 실패")
        }

        val loginType = userDataStore.getLoginType()
        loginType?.let {
            socialAuthManager.signOut(it)
        }

        userDataStore.clear()
        ApiResult.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e)
        ApiResult.Error.ServerError(message = "토큰 삭제 중 오류가 발생했습니다")
    }

    override suspend fun updateOnboardingStatus(isCompleted: Boolean) {
        userDataStore.updateOnboardingStatus(isCompleted)
    }

    override suspend fun hasCompletedOnboarding(): Boolean = userDataStore.hasCompletedOnboarding()
}