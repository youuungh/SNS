package com.ninezero.domain.usecase

import androidx.paging.PagingData
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserUseCase {
    suspend fun login(id: String, password: String): ApiResult<String>
    suspend fun signUp(id: String, userName: String, password: String): ApiResult<Boolean>
    suspend fun getToken(): String?
    suspend fun setToken(token: String)
    suspend fun clearToken(): ApiResult<Unit>
    suspend fun getMyUser(): ApiResult<User>
    suspend fun setMyUser(userName: String? = null, profileImagePath: String? = null): ApiResult<Unit>
    suspend fun setProfileImage(uri: String): ApiResult<Unit>
    suspend fun getAllUsers(): ApiResult<Flow<PagingData<User>>>
    suspend fun followUser(userId: Long): ApiResult<Long>
    suspend fun unfollowUser(userId: Long): ApiResult<Long>

    suspend fun updateOnboardingStatus(isCompleted: Boolean)
    suspend fun hasCompletedOnboarding(): Boolean
}