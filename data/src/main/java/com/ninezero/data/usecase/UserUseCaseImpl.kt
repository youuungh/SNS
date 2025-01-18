package com.ninezero.data.usecase

import com.ninezero.data.UserDataStore
import com.ninezero.data.model.param.LoginParam
import com.ninezero.data.model.param.SignUpParam
import com.ninezero.data.model.param.UpdateMyInfoParam
import com.ninezero.data.model.dto.toDomain
import com.ninezero.data.ktor.UserService
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.User
import com.ninezero.domain.usecase.FileUseCase
import com.ninezero.domain.usecase.UserUseCase
import javax.inject.Inject

/** retrofit
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
        userName: String,
        password: String
    ): ApiResult<Boolean> = try {
        val requestBody = SignUpParam(
            loginId = id,
            userName = userName,
            password = password,
            extraUserInfo = "",
            profileImagePath = ""
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
        userName: String?,
        profileImagePath: String?
    ): ApiResult<Unit> {
        return try {
            val currentUser = when (val result = getMyUser()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> return ApiResult.Error.ServerError("현재 사용자 정보를 가져올 수 없습니다")
            }

            val requestBody = UpdateMyInfoParam(
                userName = userName ?: currentUser.userName,
                profileImagePath = profileImagePath ?: currentUser.profileImagePath.orEmpty(),
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

            setMyUser(profileImagePath = imagePath)
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
*/

class UserUseCaseImpl @Inject constructor(
    private val userService: UserService,
    private val fileUseCase: FileUseCase,
    private val userDataStore: UserDataStore
) : UserUseCase {
    override suspend fun login(
        id: String,
        password: String
    ): ApiResult<String> = try {
        val loginParam = LoginParam(loginId = id, password = password)
        val response = userService.login(loginParam)

        if (response.result == "SUCCESS") {
            val token = response.data
            if (token != null) {
                setToken(token)
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

    override suspend fun signUp(
        id: String,
        userName: String,
        password: String
    ): ApiResult<Boolean> = try {
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
        userDataStore.clear()
        ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error.ServerError(message = "토큰 삭제 중 오류가 발생했습니다")
    }

    override suspend fun getMyUser(): ApiResult<User> = try {
        userService.getMyPage().let { response ->
            when (response.result) {
                "SUCCESS" -> response.data?.let {
                    ApiResult.Success(it.toDomain())
                }
                else -> when (response.errorCode) {
                    "AUTH_001" -> {
                        clearToken()
                        ApiResult.Error.Unauthorized
                    }
                    "USR_003" -> {
                        clearToken()
                        ApiResult.Error.NotFound
                    }
                    else -> null
                }
            } ?: ApiResult.Error.ServerError(
                response.errorMessage ?: "사용자 정보를 가져오는데 실패했습니다"
            )
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun setMyUser(
        userName: String?,
        profileImagePath: String?
    ): ApiResult<Unit> {
        return try {
            val currentUser = when (val result = getMyUser()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> return result
            }

            val param = UpdateMyInfoParam(
                userName = userName ?: currentUser.userName,
                profileImagePath = profileImagePath ?: currentUser.profileImagePath.orEmpty(),
                extraUserInfo = ""
            )

            val response = userService.patchMyPage(param)
            if (response.result == "SUCCESS") {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "프로필 업데이트에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun setProfileImage(uri: String): ApiResult<Unit> {
        return try {
            val image = fileUseCase.getImage(uri) ?: return ApiResult.Error.InvalidRequest("이미지를 찾을 수 없습니다")

            val imagePath = when (val result = fileUseCase.uploadImage(image)) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> return result
            }

            setMyUser(profileImagePath = imagePath)
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun updateOnboardingStatus(isCompleted: Boolean) {
        userDataStore.updateOnboardingStatus(isCompleted)
    }

    override suspend fun hasCompletedOnboarding(): Boolean = userDataStore.hasCompletedOnboarding()
}
