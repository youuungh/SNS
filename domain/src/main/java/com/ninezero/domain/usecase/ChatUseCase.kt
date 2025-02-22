package com.ninezero.domain.usecase

import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.chat.ChatMessage
import com.ninezero.domain.model.chat.ChatRoom
import kotlinx.coroutines.flow.Flow

interface ChatUseCase {
    suspend fun connectWebSocket()
    suspend fun disconnect()
    suspend fun checkExistingRoom(userId: Long): ApiResult<String?>
    suspend fun sendMessage(content: String, roomId: String?, otherUserId: Long?)
    suspend fun getRooms(page: Int, size: Int): ApiResult<List<ChatRoom>>
    suspend fun getMessages(roomId: String, page: Int, size: Int): ApiResult<List<ChatMessage>>
    suspend fun markAsRead(roomId: String, messageId: String): ApiResult<Unit>
    suspend fun leaveRoom(roomId: String): ApiResult<Unit>
    fun observeMessages(): Flow<ChatMessage>
}