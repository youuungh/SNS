package com.ninezero.domain.model

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    sealed interface Error : ApiResult<Nothing> {
        val message: String

        data class InvalidRequest(override val message: String) : Error
        data class ServerError(override val message: String) : Error
        data class NetworkError(override val message: String) : Error
    }
}