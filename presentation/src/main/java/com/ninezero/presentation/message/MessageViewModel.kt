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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

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

    init {
        viewModelScope.launch {
            networkRepository.observeNetworkConnection()
                .collect { isOnline ->
                    if (isOnline && !isInitialLoad) {
                        load()
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
                Timber.e(e)
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

            when (val userResult = userUseCase.getMyUser()) {
                is ApiResult.Success -> {
                    reduce { state.copy(myUserId = userResult.data.id) }

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
                }
                is ApiResult.Error -> {
                    reduce {
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = true
                        )
                    }
                    postSideEffect(MessageSideEffect.ShowSnackbar(userResult.message))
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
    }

    fun onResume() {
        initializeWebSocket()
        load()
    }
}

@Immutable
data class MessageState(
    val myUserId: Long = -1,
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