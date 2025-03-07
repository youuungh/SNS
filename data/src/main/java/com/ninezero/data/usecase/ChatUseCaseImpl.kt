package com.ninezero.data.usecase

import com.ninezero.data.ktor.ChatService
import com.ninezero.data.model.dto.chat.ChatMessageDto
import com.ninezero.data.model.dto.chat.ChatMessageRequest
import com.ninezero.data.model.dto.chat.ChatSession
import com.ninezero.data.model.dto.chat.toDomain
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.chat.ChatMessage
import com.ninezero.domain.model.chat.ChatRoom
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.ChatUseCase
import com.ninezero.domain.usecase.UserUseCase
import io.ktor.util.generateNonce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min

class ChatUseCaseImpl @Inject constructor(
    private val chatService: ChatService,
    private val userUseCase: UserUseCase,
    private val networkRepository: NetworkRepository
) : ChatUseCase {
    private val _messages = MutableSharedFlow<ChatMessage>()
    private val scope = CoroutineScope(SupervisorJob() + Default)
    private var currentSession: ChatSession? = null
    private var reconnectJob: Job? = null
    private var isCleanedUp = false

    private var reconnectAttempts = AtomicInteger(0)
    private val maxReconnectAttempts = 5
    private val isReconnecting = AtomicBoolean(false)
    private val baseReconnectDelay = 5000L
    private val maxReconnectDelay = 60000L

    private var lastReconnectAttemptTime = 0L
    private val reconnectCooldown = 10000L

    override suspend fun connectWebSocket() {
        if (isReconnecting.get()) {
            Timber.d("이미 WebSocket 재연결 진행 중입니다")
            return
        }

        try {
            isReconnecting.set(true)

            val user = when (val result = userUseCase.getMyUser()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> throw Exception(result.message)
            }

            val session = ChatSession(
                userId = user.id,
                userName = user.userName,
                sessionId = generateNonce()
            )
            currentSession = session

            chatService.connectWebSocket(
                session = session,
                onMessage = { messageText ->
                    val message = Json.decodeFromString<ChatMessageDto>(messageText).toDomain()
                    scope.launch(Default) {
                        _messages.emit(message)
                    }
                },
                onClose = { cause ->
                    if (cause !is CancellationException && !isCleanedUp) {
                        Timber.e(cause, "WebSocket 연결 종료")
                        currentSession = null
                        handleReconnection()
                    }
                }
            )

            reconnectAttempts.set(0)
            isReconnecting.set(false)
        } catch (e: Exception) {
            isReconnecting.set(false)
            if (e !is CancellationException) {
                Timber.e(e, "WebSocket 연결 실패")
                throw e
            }
        }
    }

    private fun handleReconnection() {
        if (isCleanedUp) return
        if (isReconnecting.get()) return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            if (!networkRepository.isNetworkAvailable()) {
                Timber.d("네트워크 연결 실패, 재연결을 시도하지 않음")
                return@launch
            }

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastReconnectAttemptTime < reconnectCooldown) {
                Timber.d("재연결 쿨다운, 재연결을 시도하지 않음")
                return@launch
            }

            val attempts = reconnectAttempts.incrementAndGet()
            if (attempts > maxReconnectAttempts) {
                Timber.d("최대 재연결 시도 횟수($maxReconnectAttempts) 초과, 더 이상 재연결을 시도하지 않음")
            }

            val delayTime = min(baseReconnectDelay * (1 shl (attempts - 1)), maxReconnectDelay)
            Timber.d("WebSocket 재연결 시도 (${attempts}/${maxReconnectAttempts}) - ${delayTime}ms 후 재시도")
            lastReconnectAttemptTime = currentTime

            delay(delayTime)
            try {
                if (!isCleanedUp) {
                    Timber.d("WebSocket 재연결 시도 중...")
                    connectWebSocket()
                }
            } catch (e: Exception) {
                Timber.e(e, "WebSocket 재연결 실패")
                isReconnecting.set(false)
            }
        }
    }

    override fun resetReconnectAttempts() {
        reconnectAttempts.set(0)
    }

    fun cleanup() {
        isCleanedUp = true
        scope.cancel()
        currentSession = null
        reconnectJob?.cancel()
    }

    override suspend fun disconnect() {
        try {
            chatService.disconnect()
            currentSession = null
            reconnectJob?.cancel()
            cleanup()
        } catch (e: Exception) {
            Timber.e(e, "WebSocket 연결 해제 실패")
            throw e
        }
    }

    override suspend fun checkExistingRoom(userId: Long): ApiResult<String?> = try {
        checkNetwork()?.let { return it }

        val response = chatService.checkExistingRoom(userId)
        if (response.result == "SUCCESS") {
            ApiResult.Success(response.data)
        } else {
            ApiResult.Error.ServerError(response.errorMessage ?: "채팅방 확인에 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun sendMessage(content: String, roomId: String?, otherUserId: Long?) = try {
        checkNetwork()?.let { throw Exception(it.message) }
        val session = currentSession ?: throw Exception("WebSocket not connected")

        chatService.sendMessage(session, ChatMessageRequest(
            content = content,
            roomId = roomId,
            otherUserId = otherUserId
        ))
    } catch (e: Exception) {
        Timber.e(e, "메시지 전송 실패")
        throw e
    }

    override suspend fun getRooms(page: Int, size: Int): ApiResult<List<ChatRoom>> = try {
        checkNetwork()?.let { return it }

        val response = chatService.getRooms(page, size)
        if (response.result == "SUCCESS") {
            ApiResult.Success(response.data?.map { it.toDomain() } ?: emptyList())
        } else {
            ApiResult.Error.ServerError(response.errorMessage ?: "채팅방 목록을 불러오는데 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun getMessages(roomId: String, page: Int, size: Int): ApiResult<List<ChatMessage>> = try {
        checkNetwork()?.let { return it }

        val response = chatService.getMessages(roomId, page, size)
        if (response.result == "SUCCESS") {
            ApiResult.Success(response.data?.map { it.toDomain() } ?: emptyList())
        } else {
            ApiResult.Error.ServerError(response.errorMessage ?: "메시지를 불러오는데 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun markAsRead(roomId: String, messageId: String): ApiResult<Unit> = try {
        checkNetwork()?.let { return it }

        val response = chatService.markAsRead(roomId, messageId)
        if (response.result == "SUCCESS") {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Error.ServerError(response.errorMessage ?: "읽음 처리에 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override suspend fun leaveRoom(roomId: String): ApiResult<Unit> = try {
        checkNetwork()?.let { return it }

        val response = chatService.leaveRoom(roomId)
        if (response.result == "SUCCESS") {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Error.ServerError(response.errorMessage ?: "채팅방 나가기에 실패했습니다")
        }
    } catch (e: Exception) {
        e.handleNetworkException()
    }

    override fun observeMessages(): Flow<ChatMessage> = _messages.asSharedFlow()

    private suspend fun checkNetwork(): ApiResult.Error.NetworkError? {
        return if (!networkRepository.isNetworkAvailable()) {
            ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
        } else {
            null
        }
    }
}