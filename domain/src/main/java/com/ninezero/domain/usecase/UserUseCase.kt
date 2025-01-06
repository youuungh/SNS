package com.ninezero.domain.usecase

import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.User

interface UserUseCase {
    suspend fun login(id: String, password: String): ApiResult<String>
    suspend fun signUp(id: String, username: String, password: String): ApiResult<Boolean>
    suspend fun getToken(): String?
    suspend fun setToken(token: String)
    suspend fun clearToken(): ApiResult<Unit>
    suspend fun getMyUser(): ApiResult<User>
    suspend fun setMyUser(username: String? = null, profileImageUrl: String? = null): ApiResult<Unit>
    suspend fun setProfileImage(uri: String): ApiResult<Unit>
    suspend fun updateOnboardingStatus(isCompleted: Boolean)
    suspend fun hasCompletedOnboarding(): Boolean
}