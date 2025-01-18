package com.ninezero.data.util

import com.ninezero.data.model.CommonResponse
import com.ninezero.domain.model.ApiResult
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import timber.log.Timber

suspend fun Exception.handleNetworkException(): ApiResult.Error {
    return when (this) {
        is ClientRequestException -> {
            Timber.e("HTTP Error: ${response.status.value}")
            try {
                val errorResponse = response.body<CommonResponse<Unit>>()
                if (response.status.value == 400) {
                    ApiResult.Error.InvalidRequest(errorResponse.errorMessage ?: "요청이 올바르지 않습니다")
                } else {
                    ApiResult.Error.ServerError(errorResponse.errorMessage ?: "서버 오류가 발생했습니다")
                }
            } catch (e: Exception) {
                ApiResult.Error.ServerError("서버 오류가 발생했습니다")
            }
        }
        else -> {
            Timber.e("Network Error: ${message}")
            ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
        }
    }
}