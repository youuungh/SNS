package com.ninezero.domain.usecase

import com.ninezero.domain.model.ApiResult

interface AuthUseCase {
    suspend fun login(id: String, password: String): ApiResult<String>
    suspend fun socialLogin(token: String, provider: String): ApiResult<String>
    suspend fun getGoogleIdToken(): ApiResult<String>
    suspend fun getKakaoIdToken(): ApiResult<String>
    suspend fun signUp(id: String, userName: String, password: String): ApiResult<Boolean>
    suspend fun getToken(): String?
    suspend fun setToken(token: String)
    suspend fun clearToken(): ApiResult<Unit>
    suspend fun updateOnboardingStatus(isCompleted: Boolean)
    suspend fun hasCompletedOnboarding(): Boolean
}