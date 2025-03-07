package com.ninezero.data.usecase

import com.ninezero.data.UserDataStore
import com.ninezero.data.ktor.NotificationService
import com.ninezero.data.model.dto.DeviceTokenRequest
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.FCMTokenUseCase
import timber.log.Timber
import javax.inject.Inject

class FCMTokenUseCaseImpl @Inject constructor(
    private val notificationService: NotificationService,
    private val userDataStore: UserDataStore,
    private val networkRepository: NetworkRepository
) : FCMTokenUseCase {

    override suspend fun registerToken(token: String): ApiResult<Long> {
        return try {
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val request = DeviceTokenRequest(token = token, deviceInfo = "Android")
            val response = notificationService.registerDeviceToken(request)

            if (response.result == "SUCCESS") {
                Timber.d("FCM 토큰 등록 성공: $token")
                ApiResult.Success(response.data!!)
            } else {
                Timber.e("FCM 토큰 등록 실패: ${response.errorMessage}")
                ApiResult.Error.ServerError(response.errorMessage ?: "FCM 토큰 등록에 실패했습니다")
            }
        } catch (e: Exception) {
            Timber.e(e, "FCM 토큰 등록 오류")
            e.handleNetworkException()
        }
    }

    override suspend fun unregisterToken(token: String): ApiResult<Boolean> {
        return try {
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val response = notificationService.removeDeviceToken(token)

            if (response.result == "SUCCESS") {
                Timber.d("FCM 토큰 등록 해제 성공: $token")
                ApiResult.Success(response.data!!)
            } else {
                Timber.e("FCM 토큰 등록 해제 실패: ${response.errorMessage}")
                ApiResult.Error.ServerError(response.errorMessage ?: "FCM 토큰 해제에 실패했습니다")
            }
        } catch (e: Exception) {
            Timber.e(e, "FCM 토큰 등록 해제 오류")
            e.handleNetworkException()
        }
    }

    override suspend fun getCurrentToken(): String? {
        return userDataStore.getFcmToken()
    }
}