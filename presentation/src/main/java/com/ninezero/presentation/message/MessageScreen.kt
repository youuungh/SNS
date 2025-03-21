package com.ninezero.presentation.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ninezero.domain.model.chat.ChatRoom
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.PullToRefreshLayout
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.ninezero.presentation.component.ChatRoomItem
import com.ninezero.presentation.component.EmptyMessageScreen
import com.ninezero.presentation.component.LeaveRoomDialog
import com.ninezero.presentation.component.NetworkErrorScreen
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.bottomsheet.LeaveRoomBottomSheet

@Composable
fun MessageScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: MessageViewModel = hiltViewModel(),
    onNavigateToChat: (
        otherUserId: Long,
        roomId: String?,
        otherUserLoginId: String,
        otherUserName: String,
        otherUserProfilePath: String?,
        myUserId: Long
    ) -> Unit
) {
    val state = viewModel.collectAsState().value
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val shouldLoadMoreRooms = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            lastVisibleItemIndex >= (totalItemsCount - 5) &&
                    !state.isLoadingMore &&
                    state.hasMoreRooms
        }
    }

    LaunchedEffect(shouldLoadMoreRooms.value) {
        if (shouldLoadMoreRooms.value) {
            viewModel.loadMoreRooms()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MessageSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
            is MessageSideEffect.NavigateToChat -> {
                onNavigateToChat(
                    sideEffect.otherUserId,
                    sideEffect.roomId,
                    sideEffect.otherUserLoginId,
                    sideEffect.otherUserName,
                    sideEffect.otherUserProfilePath,
                    sideEffect.myUserId
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingProgress()
        } else if (state.isError) {
            NetworkErrorScreen(
                onRetry = { viewModel.load() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PullToRefreshLayout(
                refreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.03f))
            ) {
                if (state.rooms.isEmpty()) {
                    EmptyMessageScreen()
                } else {
                    SNSSurface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.rooms,
                                key = { it.id }
                            ) { room ->
                                val otherParticipant = remember(room) {
                                    room.participants.firstOrNull {
                                        it.userId != viewModel.getCurrentUserId()
                                    }
                                }

                                val callbacks = remember(room.id, otherParticipant) {
                                    object {
                                        val onItemClick: (ChatRoom) -> Unit = { clickedRoom ->
                                            otherParticipant?.let {
                                                viewModel.navigateToChat(
                                                    otherUserId = it.userId,
                                                    roomId = clickedRoom.id,
                                                    otherUserLoginId = it.userLoginId,
                                                    otherUserName = it.userName,
                                                    otherUserProfilePath = it.profileImagePath
                                                )
                                            }
                                        }
                                        val onLeaveClick: (String) -> Unit = { roomId ->
                                            viewModel.showLeaveRoomBottomSheet(roomId)
                                        }
                                    }
                                }

                                ChatRoomItem(
                                    room = room,
                                    myUserId = state.myUserId,
                                    onItemClick = callbacks.onItemClick,
                                    onLeaveClick = callbacks.onLeaveClick
                                )
                            }

                            if (state.isLoadingMore) {
                                item {
                                    LoadingProgress(fullScreen = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    when (state.dialog) {
        is MessageDialog.LeaveRoom -> {
            LeaveRoomDialog(
                openDialog = true,
                onDismiss = { viewModel.hideLeaveDialog() },
                onConfirm = {
                    viewModel.leaveRoom(state.dialog.roomId)
                }
            )
        }
        MessageDialog.Hidden -> Unit
    }

    state.leaveRoomBottomSheet?.let { roomId ->
        LeaveRoomBottomSheet(
            showBottomSheet = true,
            onDismiss = { viewModel.hideLeaveRoomBottomSheet() },
            onLeave = {
                viewModel.showLeaveDialog(roomId)
                viewModel.hideLeaveRoomBottomSheet()
            }
        )
    }
}