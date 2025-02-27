package com.ninezero.domain.usecase

import com.ninezero.domain.model.ApiResult

interface FCMTokenUseCase {
    suspend fun registerToken(token: String): ApiResult<Long>
    suspend fun unregisterToken(token: String): ApiResult<Boolean>
    suspend fun getCurrentToken(): String?
}