package com.ninezero.presentation.chat

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninezero.presentation.R
import com.ninezero.presentation.component.ChatDateHeader
import com.ninezero.presentation.component.ChatMessageItem
import com.ninezero.presentation.component.SNSMessageInputField
import com.ninezero.presentation.component.LeftAlignedDetailScaffold
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.NetworkErrorScreen
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.util.parseMessageDate
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate

@Composable
fun ChatScreen(
    otherUserId: Long,
    myUserId: Long,
    roomId: String?,
    otherUserLoginId: String,
    otherUserName: String,
    otherUserProfilePath: String?,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val markedAsReadMessageIds = remember { mutableSetOf<String>() }

    val groupedMessages by remember {
        derivedStateOf {
            state.messages
                .asSequence()
                .groupBy { message -> parseMessageDate(message.createdAt) }
                .filterKeys { it != null }
                .mapKeys { (_, values) -> values.first().createdAt }
                .toSortedMap(compareByDescending { it })
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            if (listState.layoutInfo.totalItemsCount > 0) {
                val firstVisibleIndex = listState.firstVisibleItemIndex
                firstVisibleIndex >= state.messages.size - 10 &&
                        !state.isLoadingMore &&
                        state.hasMoreMessages
            } else false
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMoreMessages()
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LeftAlignedDetailScaffold(
        title = otherUserName,
        subtitle = otherUserLoginId,
        profileImageUrl = otherUserProfilePath,
        showBackButton = true,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            SNSSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                elevation = 1.dp
            ) {
                val onSend = remember(viewModel, listState) {
                    { text: String ->
                        viewModel.sendMessage(text)
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                        Unit
                    }
                }

                SNSMessageInputField(
                    hint = R.string.label_input_message,
                    onSend = onSend,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) {
        if (state.isError) {
            NetworkErrorScreen(
                onRetry = { viewModel.initializeChat() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
            ) {
                if (state.isLoading) {
                    LoadingProgress()
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    if (state.isLoadingMore) {
                        item(key = "loading") {
                            LoadingProgress(fullScreen = false)
                        }
                    }

                    groupedMessages.forEach { (dateStr, messages) ->
                        items(
                            items = messages,
                            key = { it.id }
                        ) { message ->
                            LaunchedEffect(message.id) {
                                if (message.senderId != myUserId && !markedAsReadMessageIds.contains(message.id)) {
                                    markedAsReadMessageIds.add(message.id)
                                    viewModel.markAsRead(message.id)
                                }
                            }

                            ChatMessageItem(
                                message = message,
                                isOwner = message.senderId == myUserId,
                                profileImageUrl = otherUserProfilePath,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item(key = "header-${dateStr}") {
                            ChatDateHeader(
                                date = dateStr,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}