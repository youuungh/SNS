package com.ninezero.presentation.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ninezero.presentation.component.DeleteCommentDialog
import com.ninezero.presentation.component.DeletePostDialog
import com.ninezero.presentation.component.EmptyFeedScreen
import com.ninezero.presentation.component.ErrorDialog
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.NetworkErrorScreen
import com.ninezero.presentation.component.PostCard
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.ScrollToTopButton
import com.ninezero.presentation.component.ShimmerPostCards
import com.ninezero.presentation.component.bottomsheet.CommentsBottomSheet
import com.ninezero.presentation.component.bottomsheet.OptionsBottomSheet
import com.ninezero.presentation.util.isEmpty
import com.ninezero.presentation.util.isError
import com.ninezero.presentation.util.isLoading
import com.ninezero.presentation.util.isNotEmpty
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FeedScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
) {
    val state = viewModel.collectAsState().value
    val posts = state.posts.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isInit by remember { mutableStateOf(true ) }

    LaunchedEffect(posts.loadState.refresh) {
        if (posts.loadState.refresh is LoadState.NotLoading) {
            isInit = false
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FeedSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
            FeedSideEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    SNSSurface {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isInit && posts.isLoading() -> repeat(3) { ShimmerPostCards() }
                posts.isEmpty() -> EmptyFeedScreen()
                posts.isError() -> NetworkErrorScreen(
                    onRetry = {
                        isInit = true
                        posts.refresh()
                    }
                )
                else -> {
                    PullToRefreshLayout(
                        refreshing = state.isRefresh,
                        onRefresh = { viewModel.refresh() }
                    ) {
                        AnimatedVisibility(
                            visible = posts.isNotEmpty(),
                            enter = fadeIn(animationSpec = tween(100)),
                            exit = fadeOut(animationSpec = tween(100))
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    count = posts.itemCount,
                                    key = { posts[it]?.postId ?: it }
                                ) { index ->
                                    posts[index]?.let { post ->
                                        if (!state.deletedPostIds.contains(post.postId)) {
                                            PostCard(
                                                postId = post.postId,
                                                username = post.username,
                                                profileImageUrl = post.profileImageUrl,
                                                images = post.images,
                                                richTextState = post.richTextState,
                                                comments = viewModel.getCombinedComments(post),
                                                isOwner = post.userId == state.myUserId,
                                                onOptionClick = { viewModel.showOptionsSheet(post) },
                                                onCommentClick = { viewModel.showCommentsSheet(post) }
                                            )
                                        }
                                    }
                                }

                                if (posts.loadState.append is LoadState.Loading) {
                                    item { LoadingProgress() }
                                }
                            }
                        }
                    }

                    ScrollToTopButton(
                        listState = listState,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }

    when (state.currentDialog) {
        is FeedDialog.DeletePost -> {
            DeletePostDialog(
                openDialog = true,
                onDismiss = { viewModel.hideDialog() },
                onConfirm = {
                    viewModel.onPostDelete(state.currentDialog.post)
                    viewModel.hideDialog()
                }
            )
        }

        is FeedDialog.DeleteComment -> {
            DeleteCommentDialog(
                openDialog = true,
                onDismiss = { viewModel.hideDialog() },
                onConfirm = {
                    viewModel.onDeleteComment(
                        state.currentDialog.postId,
                        state.currentDialog.comment
                    )
                    viewModel.hideDialog()
                }
            )
        }

        is FeedDialog.Error -> {
            ErrorDialog(
                message = state.currentDialog.message,
                onDismiss = { viewModel.hideDialog() }
            )
        }

        FeedDialog.Hidden -> Unit
    }

    state.optionsSheetPost?.let { post ->
        OptionsBottomSheet(
            showBottomSheet = true,
            onDismiss = { viewModel.hideOptionsSheet() },
            onDelete = {
                viewModel.showDeletePostDialog(post)
                viewModel.hideOptionsSheet()
            }
        )
    }

    state.commentsSheetPost?.let { post ->
        val combinedComments = viewModel.getCombinedComments(post)
        CommentsBottomSheet(
            showBottomSheet = true,
            comments = combinedComments,
            isOwner = post.userId == state.myUserId,
            onDismiss = { viewModel.hideCommentsSheet() },
            onDeleteComment = { comment ->
                viewModel.showDeleteCommentDialog(post.postId, comment)
            },
            onCommentSend = { text -> viewModel.onCommentSend(post.postId, text) }
        )
    }
}