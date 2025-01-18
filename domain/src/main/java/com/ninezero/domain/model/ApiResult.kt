package com.ninezero.domain.model

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    sealed interface Error : ApiResult<Nothing> {
        val message: String

        data class InvalidRequest(override val message: String) : Error
        data class ServerError(override val message: String) : Error
        data class NetworkError(override val message: String) : Error

        object Unauthorized : Error {
            override val message: String
                get() = "인증되지 않은 사용자입니다"
        }

        object NotFound : Error {
            override val message: String
                get() = "조회되지 않는 사용자입니다"
        }
    }
}