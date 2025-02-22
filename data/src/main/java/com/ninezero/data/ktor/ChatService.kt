package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.dto.chat.ChatMessageDto
import com.ninezero.data.model.dto.chat.ChatMessageRequest
import com.ninezero.data.model.dto.chat.ChatRoomDto
import com.ninezero.data.model.dto.chat.ChatSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChatService @Inject constructor(
    private val client: HttpClient
) {
    private var webSocketSession: WebSocketSession? = null

    suspend fun checkExistingRoom(userId: Long): CommonResponse<String?> {
        return client.get("chat/rooms/check/$userId").body()
    }

    suspend fun connectWebSocket(
        session: ChatSession,
        onMessage: suspend (String) -> Unit,
        onClose: suspend (cause: Throwable?) -> Unit
    ) {
        try {
            client.webSocket("chat/ws") {
                webSocketSession = this
                try {
                    send(Frame.Text(Json.encodeToString(session)))

                    incoming.consumeEach { frame ->
                        when (frame) {
                            is Frame.Text -> onMessage(frame.readText())
                            is Frame.Close -> {
                                onClose(null)
                                webSocketSession = null
                            }
                            else -> Unit
                        }
                    }
                } catch (e: Exception) {
                    onClose(e)
                    throw e
                }
            }
        } catch (e: Exception) {
            onClose(e)
            throw e
        }
    }

    suspend fun disconnect() {
        webSocketSession?.close()
        webSocketSession = null
    }

    suspend fun sendMessage(session: ChatSession, request: ChatMessageRequest) {
        webSocketSession?.send(Frame.Text(Json.encodeToString(request)))
            ?: throw WebSocketException("WebSocket not connected")
    }

    suspend fun getRooms(page: Int, size: Int): CommonResponse<List<ChatRoomDto>> {
        return client.get("chat/rooms") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getMessages(roomId: String, page: Int, size: Int): CommonResponse<List<ChatMessageDto>> {
        return client.get("chat/rooms/$roomId/messages") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun markAsRead(roomId: String, messageId: String): CommonResponse<Unit> {
        return client.post("chat/rooms/$roomId/messages/$messageId/read").body()
    }

    suspend fun leaveRoom(roomId: String): CommonResponse<Unit> {
        return client.delete("chat/rooms/$roomId").body()
    }

    class WebSocketException(message: String) : Exception(message)
}