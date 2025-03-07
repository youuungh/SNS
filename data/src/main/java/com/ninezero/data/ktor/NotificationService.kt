package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.dto.DeviceTokenRequest
import com.ninezero.data.model.dto.NotificationDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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

    suspend fun getNotifications(page: Int, size: Int): CommonResponse<List<NotificationDto>> {
        return client.get("notifications") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun hasUnreadNotifications(): CommonResponse<Boolean> {
        return client.get("notifications/unread").body()
    }

    suspend fun markAsRead(id: Long): CommonResponse<Boolean> {
        return client.post("notifications/$id/read").body()
    }

    suspend fun deleteNotification(id: Long): CommonResponse<Boolean> {
        return client.delete("notifications/$id").body()
    }

    suspend fun deleteAllNotification(): CommonResponse<Boolean> {
        return client.delete("notifications/all").body()
    }
}