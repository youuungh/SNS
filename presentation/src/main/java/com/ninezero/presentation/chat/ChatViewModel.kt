package com.ninezero.presentation.chat

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.chat.ChatMessage
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.ChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val networkRepository: NetworkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ContainerHost<ChatState, ChatSideEffect> {
    private val otherUserId: Long = checkNotNull(savedStateHandle["otherUserId"])
    private val myUserId: Long = checkNotNull(savedStateHandle["myUserId"])
    private val otherUserLoginId: String = checkNotNull(savedStateHandle["otherUserLoginId"])
    private val otherUserName: String = checkNotNull(savedStateHandle["otherUserName"])
    private val otherUserProfilePath: String? = savedStateHandle["otherUserProfilePath"]
    private var currentRoomId: String? = savedStateHandle["roomId"]

    private val socketScope = viewModelScope.plus(Main.immediate)
    private val messageScope = viewModelScope.plus(Default)
    private val pageSize = 30

    private var networkObserverJob: Job? = null
    private var lastNetworkConnectAttempt = 0L
    private val networkConnectCooldown = 10000L

    override val container: Container<ChatState, ChatSideEffect> = container(
        ChatState(
            myUserId = myUserId,
            otherUserLoginId = otherUserLoginId,
            otherUserName = otherUserName,
            otherUserProfilePath = otherUserProfilePath,
            roomId = currentRoomId
        )
    )

    private var isInitialLoad = true

    init {
        viewModelScope.launch {
            networkObserverJob?.cancel()
            networkObserverJob = viewModelScope.launch {
                networkRepository.observeNetworkConnection()
                    .distinctUntilChanged() // 중복 이벤트 필터링
                    .sample(1000) // 짧은 시간 내의 변경 필터링
                    .collect { isOnline ->
                        if (isOnline && !isInitialLoad) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastNetworkConnectAttempt > networkConnectCooldown) {
                                lastNetworkConnectAttempt = currentTime
                                Timber.d("네트워크 연결 감지됨, 채팅 초기화 시도")

                                chatUseCase.resetReconnectAttempts()

                                viewModelScope.launch {
                                    try {
                                        initializeChat()
                                    } catch (e: Exception) {
                                        Timber.e(e, "네트워크 연결 후 초기화 실패")
                                    }
                                }
                            } else {
                                Timber.d("네트워크 연결 감지됨, 쿨다운 중 (${networkConnectCooldown - (currentTime - lastNetworkConnectAttempt)}ms 남음)")
                            }
                        } else if (!isOnline) {
                            Timber.d("네트워크 연결 끊김")
                        }
                    }
            }
        }

        viewModelScope.launch {
            initializeChat()
            isInitialLoad = false
        }
    }

    fun initializeChat() = intent {
        try {
            reduce { state.copy(isError = false) }

            // WebSocket 연결
            socketScope.launch {
                try {
                    chatUseCase.connectWebSocket()
                } catch (e: Exception) {
                    reduce { state.copy(isLoading = false, isError = true) }
                    Timber.e(e, "WebSocket 연결 실패")
                    postSideEffect(ChatSideEffect.ShowSnackbar("채팅 연결 실패"))
                }
            }

            // 메시지 구독
            messageScope.launch {
                chatUseCase.observeMessages()
                    .catch { e -> Timber.e(e, "Message subscription error") }
                    .flowOn(Default)
                    .collect { message ->
                        withContext(Main) {
                            intent { handleNewMessage(message) }
                        }
                    }
            }

            currentRoomId?.let { loadInitialMessages(it) }

            reduce { state.copy(isLoading = false) }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, isError = true) }
            Timber.e(e)
        }
    }

    private fun handleNewMessage(message: ChatMessage) = intent {
        when {
            message.roomId == currentRoomId -> {
                reduce {
                    state.copy(
                        messages = (state.messages + message)
                            .distinctBy { it.id }
                            .sortedByDescending { it.createdAt }
                    )
                }
            }
            currentRoomId == null -> {
                currentRoomId = message.roomId
                reduce { state.copy(roomId = message.roomId) }
                loadInitialMessages(message.roomId)
            }
        }
    }

    private fun loadInitialMessages(roomId: String) = intent {
        try {
            when (val result = chatUseCase.getMessages(roomId, 1, pageSize)) {
                is ApiResult.Success -> {
                    val sortedMessages = result.data.sortedByDescending { it.createdAt }
                    reduce {
                        state.copy(
                            messages = sortedMessages,
                            hasMoreMessages = result.data.size >= pageSize,
                            currentPage = 1
                        )
                    }
                }
                is ApiResult.Error -> {
                    reduce { state.copy(isError = true) }
                    postSideEffect(ChatSideEffect.ShowSnackbar(result.message))
                }
            }
        } catch (e: Exception) {
            reduce { state.copy(isError = true) }
            Timber.e(e)
        }
    }

    fun loadMoreMessages() = intent {
        if (!state.hasMoreMessages || state.isLoadingMore) return@intent

        try {
            reduce { state.copy(isLoadingMore = true) }

            currentRoomId?.let { roomId ->
                when (val result = chatUseCase.getMessages(roomId, state.currentPage + 1, pageSize)) {
                    is ApiResult.Success -> {
                        if (result.data.isEmpty()) {
                            reduce {
                                state.copy(
                                    hasMoreMessages = false,
                                    isLoadingMore = false
                                )
                            }
                            return@intent
                        }

                        val updatedMessages = (state.messages + result.data)
                            .distinctBy { it.id }
                            .sortedByDescending { it.createdAt }

                        reduce {
                            state.copy(
                                messages = updatedMessages,
                                isLoadingMore = false,
                                hasMoreMessages = result.data.size >= pageSize,
                                currentPage = state.currentPage + 1
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        reduce { state.copy(isLoadingMore = false) }
                        postSideEffect(ChatSideEffect.ShowSnackbar(result.message))
                    }
                }
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoadingMore = false) }
            Timber.e(e)
        }
    }

    fun sendMessage(content: String) = intent {
        if (content.isBlank()) return@intent

        try {
            // 채팅방이 없는 경우 먼저 확인
            if (currentRoomId == null) {
                when (val result = chatUseCase.checkExistingRoom(otherUserId)) {
                    is ApiResult.Success -> {
                        result.data?.let { newRoomId ->
                            currentRoomId = newRoomId
                            reduce { state.copy(roomId = newRoomId) }
                        }
                    }
                    is ApiResult.Error -> {
                        postSideEffect(ChatSideEffect.ShowSnackbar(result.message))
                    }
                }
            }

            chatUseCase.sendMessage(content, currentRoomId, otherUserId)

            // 새로운 채팅방이 생성된 경우
            if (currentRoomId == null) {
                delay(300)
                when (val result = chatUseCase.checkExistingRoom(otherUserId)) {
                    is ApiResult.Success -> {
                        result.data?.let { newRoomId ->
                            currentRoomId = newRoomId
                            reduce { state.copy(roomId = newRoomId) }
                            loadInitialMessages(newRoomId)
                        }
                    }
                    is ApiResult.Error -> {
                        postSideEffect(ChatSideEffect.ShowSnackbar(result.message))
                    }
                }
            } else {
                delay(300)
                loadInitialMessages(currentRoomId!!)
            }
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowSnackbar(e.message ?: "메시지 전송 실패"))
        }
    }

    fun markAsRead(messageId: String) = intent {
        currentRoomId?.let { roomId ->
            when (val result = chatUseCase.markAsRead(roomId, messageId)) {
                is ApiResult.Success -> Unit
                is ApiResult.Error -> Timber.e(result.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            chatUseCase.disconnect()
        }
    }
}

@Immutable
data class ChatState(
    val myUserId: Long = -1L,
    val otherUserLoginId: String,
    val otherUserName: String,
    val otherUserProfilePath: String?,
    val roomId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val currentPage: Int = 1
)

sealed interface ChatSideEffect {
    data class ShowSnackbar(val message: String) : ChatSideEffect
}