package com.ninezero.data.usecase

import com.ninezero.data.UserDataStore
import com.ninezero.data.model.LoginParam
import com.ninezero.data.model.SignUpParam
import com.ninezero.data.model.UpdateMyInfoParam
import com.ninezero.data.model.toDomain
import com.ninezero.data.retrofit.UserService
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.User
import com.ninezero.domain.usecase.FileUseCase
import com.ninezero.domain.usecase.UserUseCase
import timber.log.Timber
import javax.inject.Inject

class UserUseCaseImpl @Inject constructor(
    private val userService: UserService,
    private val fileUseCase: FileUseCase,
    private val userDataStore: UserDataStore
) : UserUseCase {
    override suspend fun login(
        id: String,
        password: String
    ): ApiResult<String> = try {
        val requestBody = LoginParam(loginId = id, password = password).toRequestBody()
        val response = userService.login(requestBody)

        val token = response.data
        setToken(token)
        ApiResult.Success(token)
    } catch (e: Exception) {
        when (e) {
            is retrofit2.HttpException -> {
                Timber.e("HTTP Error: ${e.code()}")
                if (e.code() == 400) {
                    ApiResult.Error.InvalidRequest("잘못된 아이디 또는 비밀번호입니다")
                } else {
                    ApiResult.Error.ServerError("서버 오류가 발생했습니다")
                }
            }
            else -> {
                Timber.e("Network Error: ${e.message}")
                ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
            }
        }
    }

    override suspend fun signUp(
        id: String,
        username: String,
        password: String
    ): ApiResult<Boolean> = try {
        val requestBody = SignUpParam(
            loginId = id,
            name = username,
            password = password,
            extraUserInfo = "",
            profileFilePath = ""
        ).toRequestBody()
        val response = userService.signUp(requestBody)

        if (response.result == "90000") {
            ApiResult.Success(true)
        } else {
            ApiResult.Error.ServerError(response.errorMessage)
        }
    } catch (e: Exception) {
        when (e) {
            is retrofit2.HttpException -> {
                Timber.e("HTTP Error: ${e.code()}")
                if (e.code() == 400) {
                    ApiResult.Error.InvalidRequest("이미 존재하는 계정입니다")
                } else {
                    ApiResult.Error.ServerError("서버 오류가 발생했습니다")
                }
            }
            else -> {
                Timber.e("Network Error: ${e.message}")
                ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
            }
        }
    }

    override suspend fun getToken(): String? {
        return userDataStore.getToken()
    }

    override suspend fun setToken(token: String) {
        userDataStore.setToken(token = token)
    }

    override suspend fun clearToken(): ApiResult<Unit> = try {
        userDataStore.clear()
        ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error.ServerError(message = "토큰 삭제 중 오류가 발생했습니다")
    }

    override suspend fun getMyUser(): ApiResult<User> = try {
        val response = userService.getMyPage()
        ApiResult.Success(response.data.toDomain())
    } catch (e: Exception) {
        when (e) {
            is retrofit2.HttpException -> {
                Timber.e("HTTP Error: ${e.code()}")
                ApiResult.Error.ServerError("서버에서 사용자 정보를 가져오는데 실패했습니다")
            }
            else -> {
                Timber.e("Network Error: ${e.message}")
                ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
            }
        }
    }

    override suspend fun setMyUser(
        username: String?,
        profileImageUrl: String?
    ): ApiResult<Unit> {
        return try {
            val currentUser = when (val result = getMyUser()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> return ApiResult.Error.ServerError("현재 사용자 정보를 가져올 수 없습니다")
            }

            val requestBody = UpdateMyInfoParam(
                userName = username ?: currentUser.username,
                profileFilePath = profileImageUrl ?: currentUser.profileImageUrl.orEmpty(),
                extraUserInfo = ""
            ).toRequestBody()

            userService.patchMyPage(requestBody)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            when (e) {
                is retrofit2.HttpException -> {
                    Timber.e("HTTP Error: ${e.code()}")
                    ApiResult.Error.ServerError("프로필 업데이트에 실패했습니다")
                }
                else -> {
                    Timber.e("Network Error: ${e.message}")
                    ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
                }
            }
        }
    }

    override suspend fun setProfileImage(uri: String): ApiResult<Unit> {
        return try {
            val image = fileUseCase.getImage(uri) ?: return ApiResult.Error.InvalidRequest("이미지를 찾을 수 없습니다")

            val imagePath = when (val result = fileUseCase.uploadImage(image)) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> return result
            }

            setMyUser(profileImageUrl = imagePath)
        } catch (e: Exception) {
            Timber.e("Network Error: ${e.message}")
            ApiResult.Error.ServerError("프로필 이미지 설정에 실패했습니다")
        }
    }

    override suspend fun updateOnboardingStatus(isCompleted: Boolean) {
        userDataStore.updateOnboardingStatus(isCompleted)
    }

    override suspend fun hasCompletedOnboarding(): Boolean = userDataStore.hasCompletedOnboarding()
}