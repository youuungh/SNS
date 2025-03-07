package com.ninezero.data.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ninezero.data.UserDataStore
import com.ninezero.data.db.user.paging.SearchPagingSource
import com.ninezero.data.model.param.UpdateMyInfoParam
import com.ninezero.data.model.dto.toDomain
import com.ninezero.data.ktor.UserService
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.RecentSearch
import com.ninezero.domain.model.User
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.UserRepository
import com.ninezero.domain.usecase.AuthUseCase
import com.ninezero.domain.usecase.FileUseCase
import com.ninezero.domain.usecase.UserUseCase
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

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
    private val authUseCaseProvider: Provider<AuthUseCase>,
    private val fileUseCase: FileUseCase,
    private val userDataStore: UserDataStore,
    private val networkRepository: NetworkRepository
) : UserUseCase {

    override suspend fun getMyUserId(): Long {
        return userDataStore.getUserId()
    }

    override suspend fun setMyUserId(userId: Long) {
        userDataStore.setUserId(userId)
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
                        authUseCaseProvider.get().clearToken()
                        ApiResult.Error.Unauthorized
                    }
                    "USR_003" -> {
                        authUseCaseProvider.get().clearToken()
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

    override suspend fun getUserInfo(userId: Long): ApiResult<User> = try {
        if (networkRepository.isNetworkAvailable()) {
            val response = userService.getUserInfo(userId)
            if (response.result == "SUCCESS") {
                response.data?.let {
                    ApiResult.Success(it.toDomain())
                } ?: ApiResult.Error.ServerError("데이터가 없습니다")
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "사용자 정보를 가져오는데 실패했습니다")
            }
        } else {
            ApiResult.Error.NetworkError("네트워크 연결을 확인해주세요")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
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

    override fun searchUsers(query: String): ApiResult<Flow<PagingData<User>>> = try {
        ApiResult.Success(
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    prefetchDistance = 1
                )
            ) {
                SearchPagingSource(userService, query)
            }.flow
        )
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("검색에 실패했습니다")
    }

    override suspend fun getRecentSearches(): ApiResult<List<RecentSearch>> {
        return try {
            checkNetwork()?.let { return it }

            val response = userService.getRecentSearches()
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data?.map { it.toDomain() } ?: emptyList())
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "최근 검색 목록을 불러오는데 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun saveRecentSearch(userId: Long): ApiResult<Unit> {
        return try {
            checkNetwork()?.let { return it }

            val response = userService.saveRecentSearch(userId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "최근 검색 저장에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun deleteRecentSearch(userId: Long): ApiResult<Unit> {
        return try {
            checkNetwork()?.let { return it }

            val response = userService.deleteRecentSearch(userId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "최근 검색 삭제에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun clearRecentSearches(): ApiResult<Unit> {
        return try {
            checkNetwork()?.let { return it }

            val response = userService.clearRecentSearches()
            if (response.result == "SUCCESS") {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "최근 검색 전체 삭제에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    private suspend fun checkNetwork(): ApiResult.Error.NetworkError? {
        return if (!networkRepository.isNetworkAvailable()) {
            ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
        } else {
            null
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}