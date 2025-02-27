package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.dto.DeviceTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class NotificationService @Inject constructor(
    private val client: HttpClient
) {
    suspend fun registerDeviceToken(request: DeviceTokenRequest): CommonResponse<Long> {
        return client.post("notifications/tokens"){
            setBody(request)
        }.body()
    }

    suspend fun removeDeviceToken(token: String): CommonResponse<Boolean> {
        return client.delete("notifications/tokens/$token").body()
    }
}