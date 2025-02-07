package com.ninezero.data.usecase

import androidx.paging.PagingData
import com.ninezero.data.UserDataStore
import com.ninezero.data.model.param.LoginParam
import com.ninezero.data.model.param.SignUpParam
import com.ninezero.data.model.param.UpdateMyInfoParam
import com.ninezero.data.model.dto.toDomain
import com.ninezero.data.ktor.UserService
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.User
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.UserRepository
import com.ninezero.domain.usecase.FileUseCase
import com.ninezero.domain.usecase.UserUseCase
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
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
    private val userRepository: UserRepository,
    private val fileUseCase: FileUseCase,
    private val userDataStore: UserDataStore,
    private val networkRepository: NetworkRepository
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
        if (networkRepository.isNetworkAvailable()) {
            val response = userService.getMyPage()
            if (response.result == "SUCCESS") {
                response.data?.let {
                    val user = it.toDomain()
                    userRepository.updateMyUser(user)
                    ApiResult.Success(user)
                } ?: ApiResult.Error.ServerError("데이터가 없습니다")
            } else {
                when (response.errorCode) {
                    "AUTH_001" -> {
                        clearToken()
                        ApiResult.Error.Unauthorized
                    }
                    "USR_003" -> {
                        clearToken()
                        ApiResult.Error.NotFound
                    }
                    else -> userRepository.getMyUser()?.let {
                        ApiResult.Success(it)
                    } ?: ApiResult.Error.ServerError(response.errorMessage ?: "사용자 정보를 가져오는데 실패했습니다")
                }
            }
        } else {
            userRepository.getMyUser()?.let {
                ApiResult.Success(it)
            } ?: ApiResult.Error.NetworkError("네트워크 연결을 확인해주세요")
        }
    } catch (e: Exception) {
        userRepository.getMyUser()?.let {
            ApiResult.Success(it)
        } ?: e.handleNetworkException()
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

            val response = userService.updateMyPage(param)
            if (response.result == "SUCCESS") {
                val updatedUser = currentUser.copy(
                    userName = userName ?: currentUser.userName,
                    profileImagePath = profileImagePath ?: currentUser.profileImagePath
                )
                userRepository.updateMyUser(updatedUser)
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

    override suspend fun getAllUsers(): ApiResult<Flow<PagingData<User>>> = try {
        ApiResult.Success(userRepository.getAllUsers())
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("유저 목록을 불러오는데 실패했습니다")
    }

    override suspend fun followUser(userId: Long): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = userService.followUser(userId = userId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "팔로우에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun unfollowUser(userId: Long): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = userService.unfollowUser(userId = userId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "언팔로우에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun updateOnboardingStatus(isCompleted: Boolean) {
        userDataStore.updateOnboardingStatus(isCompleted)
    }

    override suspend fun hasCompletedOnboarding(): Boolean = userDataStore.hasCompletedOnboarding()

    private suspend fun checkNetwork(): ApiResult.Error.NetworkError? {
        return if (!networkRepository.isNetworkAvailable()) {
            ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
        } else {
            null
        }
    }
}