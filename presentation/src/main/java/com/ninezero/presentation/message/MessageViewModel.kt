package com.ninezero.presentation.message

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.chat.ChatMessage
import com.ninezero.domain.model.chat.ChatRoom
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.ChatUseCase
import com.ninezero.domain.usecase.UserUseCase
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
class MessageViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val userUseCase: UserUseCase,
    private val networkRepository: NetworkRepository
) : ViewModel(), ContainerHost<MessageState, MessageSideEffect> {
    override val container: Container<MessageState, MessageSideEffect> = container(MessageState())

    private var isInitialLoad = true
    private var currentChatRoomId: String? = null
    private var messageCollectJob: Job? = null

    private val socketScope = viewModelScope.plus(Main.immediate)
    private val messageScope = viewModelScope.plus(Default)
    private val pageSize = 20

    private var networkObserverJob: Job? = null
    private var lastNetworkConnectAttempt = 0L
    private val networkConnectCooldown = 10000L

    init {
        viewModelScope.launch {
            networkObserverJob?.cancel()
            networkObserverJob = viewModelScope.launch {
                networkRepository.observeNetworkConnection()
                    .distinctUntilChanged()
                    .sample(1000)
                    .collect { isOnline ->
                        if (isOnline && !isInitialLoad) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastNetworkConnectAttempt > networkConnectCooldown) {
                                lastNetworkConnectAttempt = currentTime
                                Timber.d("네트워크 연결 감지됨, 메시지 화면 초기화 시도")

                                chatUseCase.resetReconnectAttempts()

                                viewModelScope.launch {
                                    try {
                                        load()
                                        initializeWebSocket()
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
            load()
            initializeWebSocket()
            isInitialLoad = false
        }
    }

    fun initializeWebSocket() = intent {
        messageCollectJob?.cancel()

        socketScope.launch {
            try {
                chatUseCase.connectWebSocket()
            } catch (e: Exception) {
                Timber.e(e, "WebSocket 연결 실패")
                postSideEffect(MessageSideEffect.ShowSnackbar("채팅 연결 실패"))
            }
        }

        messageCollectJob = messageScope.launch {
            chatUseCase.observeMessages()
                .catch { e -> Timber.e(e, "Message subscription error") }
                .flowOn(Default)
                .collect { message ->
                    withContext(Main) {
                        intent { handleNewMessage(message) }
                    }
                }
        }
    }

    private fun handleNewMessage(message: ChatMessage) = intent {
        when (val result = chatUseCase.getRooms(1, pageSize)) {
            is ApiResult.Success -> {
                val updatedRooms = result.data.sortedByDescending { it.lastMessage?.createdAt }
                reduce { state.copy(rooms = updatedRooms) }
            }
            is ApiResult.Error -> {
                val updatedRooms = state.rooms.map { room ->
                    if (room.id == message.roomId) {
                        room.copy(lastMessage = message)
                    } else {
                        room
                    }
                }.sortedByDescending { it.lastMessage?.createdAt }
                reduce { state.copy(rooms = updatedRooms) }
            }
        }
    }

    fun load() = intent {
        try {
            reduce {
                state.copy(
                    isError = false,
                    currentPage = 1
                )
            }

            val myUserId = userUseCase.getMyUserId()
            reduce { state.copy(myUserId = myUserId) }

            when (val result = chatUseCase.getRooms(1, pageSize)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            rooms = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            hasMoreRooms = result.data.size >= pageSize
                        )
                    }
                }
                is ApiResult.Error -> {
                    reduce {
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = true
                        )
                    }
                    postSideEffect(MessageSideEffect.ShowSnackbar(result.message))
                }
            }
        } catch (e: Exception) {
            reduce {
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = true
                )
            }
            Timber.e(e)
        }
    }

    fun loadMoreRooms() = intent {
        if (!state.hasMoreRooms || state.isLoadingMore) return@intent

        try {
            reduce { state.copy(isLoadingMore = true) }

            when (val result = chatUseCase.getRooms(state.currentPage + 1, pageSize)) {
                is ApiResult.Success -> {
                    if (result.data.isEmpty()) {
                        reduce {
                            state.copy(
                                hasMoreRooms = false,
                                isLoadingMore = false
                            )
                        }
                        return@intent
                    }

                    val updatedRooms = (state.rooms + result.data)
                        .distinctBy { it.id }
                        .sortedByDescending { it.lastMessage?.createdAt }

                    reduce {
                        state.copy(
                            rooms = updatedRooms,
                            currentPage = state.currentPage + 1,
                            isLoadingMore = false,
                            hasMoreRooms = result.data.size >= pageSize
                        )
                    }
                }
                is ApiResult.Error -> {
                    reduce { state.copy(isLoadingMore = false) }
                    postSideEffect(MessageSideEffect.ShowSnackbar(result.message))
                }
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoadingMore = false) }
            Timber.e(e)
        }
    }

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        delay(1000)
        load()
    }

    fun leaveRoom(roomId: String) = intent {
        when (val result = chatUseCase.leaveRoom(roomId)) {
            is ApiResult.Success -> {
                hideLeaveDialog()
                load()
            }
            is ApiResult.Error -> {
                hideLeaveDialog()
                postSideEffect(MessageSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun navigateToChat(
        otherUserId: Long,
        roomId: String,
        otherUserLoginId: String,
        otherUserName: String,
        otherUserProfilePath: String?
    ) = intent {
        currentChatRoomId = roomId

        postSideEffect(
            MessageSideEffect.NavigateToChat(
                otherUserId = otherUserId,
                roomId = roomId,
                otherUserLoginId = otherUserLoginId,
                otherUserName = otherUserName,
                otherUserProfilePath = otherUserProfilePath,
                myUserId = state.myUserId
            )
        )

        val updatedRooms = state.rooms.map { room ->
            if (room.id == currentChatRoomId) {
                room.copy(
                    participants = room.participants.map { participant ->
                        if (participant.userId == state.myUserId) {
                            participant.copy(unreadCount = 0)
                        } else {
                            participant
                        }
                    }
                )
            } else {
                room
            }
        }
        reduce { state.copy(rooms = updatedRooms) }
    }

    fun showLeaveRoomBottomSheet(roomId: String) = intent {
        reduce { state.copy(leaveRoomBottomSheet = roomId) }
    }

    fun hideLeaveRoomBottomSheet() = intent {
        reduce { state.copy(leaveRoomBottomSheet = null) }
    }

    fun showLeaveDialog(roomId: String) = intent {
        reduce { state.copy(dialog = MessageDialog.LeaveRoom(roomId)) }
    }

    fun hideLeaveDialog() = intent {
        reduce { state.copy(dialog = MessageDialog.Hidden) }
    }

    fun getCurrentUserId(): Long = container.stateFlow.value.myUserId

    override fun onCleared() {
        super.onCleared()
        messageCollectJob?.cancel()
        networkObserverJob?.cancel()
    }

    fun onResume() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNetworkConnectAttempt > networkConnectCooldown) {
            lastNetworkConnectAttempt = currentTime
            Timber.d("화면 진입, 채팅 초기화 시도")

            viewModelScope.launch {
                chatUseCase.resetReconnectAttempts()
                initializeWebSocket()
                load()
            }
        } else {
            Timber.d("화면 진입, 쿨다운 중 (${networkConnectCooldown - (currentTime - lastNetworkConnectAttempt)}ms 남음)")
        }
    }
}

@Immutable
data class MessageState(
    val myUserId: Long = -1L,
    val rooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreRooms: Boolean = true,
    val currentPage: Int = 1,
    val dialog: MessageDialog = MessageDialog.Hidden,
    val leaveRoomBottomSheet: String? = null
)

sealed interface MessageDialog {
    data object Hidden : MessageDialog
    data class LeaveRoom(val roomId: String) : MessageDialog
}

sealed interface MessageSideEffect {
    data class ShowSnackbar(val message: String) : MessageSideEffect
    data class NavigateToChat(
        val otherUserId: Long,
        val roomId: String?,
        val otherUserLoginId: String,
        val otherUserName: String,
        val otherUserProfilePath: String?,
        val myUserId: Long
    ) : MessageSideEffect
}