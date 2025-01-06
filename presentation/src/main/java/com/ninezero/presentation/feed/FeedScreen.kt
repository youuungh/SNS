package com.ninezero.presentation.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ninezero.presentation.component.DeleteCommentDialog
import com.ninezero.presentation.component.DeletePostDialog
import com.ninezero.presentation.component.EmptyFeedScreen
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.LoadingScreen
import com.ninezero.presentation.component.NetworkErrorScreen
import com.ninezero.presentation.component.NewPostBanner
import com.ninezero.presentation.component.PostCard
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.bottomsheet.CommentsBottomSheet
import com.ninezero.presentation.component.bottomsheet.OptionsBottomSheet
import com.ninezero.presentation.model.feed.PostModel
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FeedScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val posts = state.posts.collectAsLazyPagingItems()
    val showNewPost by viewModel.showNewPost.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FeedSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.hasError) {
            NetworkErrorScreen(onRetry = { viewModel.refresh() })
        } else {
            FeedContent(
                state = state,
                posts = posts,
                listState = listState,
                isLoading = state.isLoading,
                onRefresh = { viewModel.refresh() },
                onLoadStateChange = { viewModel.updateLoadState(it) },
                onOptionClick = { viewModel.showOptionsSheet(it) },
                onCommentClick = { viewModel.showCommentsSheet(it) },
                onScrollToTop = { viewModel.onNewPostBannerClick() }
            )
        }

        NewPostBanner(
            visible = showNewPost,
            onClick = {
                scope.launch {
                    viewModel.onNewPostBannerClick()
                    listState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
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

@Composable
private fun FeedContent(
    state: FeedState,
    listState: LazyListState,
    posts: LazyPagingItems<PostModel>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onLoadStateChange: (LoadState) -> Unit,
    onOptionClick: (PostModel) -> Unit,
    onCommentClick: (PostModel) -> Unit,
    onScrollToTop: () -> Unit = {}
) {
    var hadItems by remember { mutableStateOf(false) }
    var isReloading by remember { mutableStateOf(false) }
    var enableScrollDetection by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading, posts.itemCount) {
        when {
            isLoading -> {
                hadItems = false
                isReloading = true
                enableScrollDetection = false
            }
            isReloading && posts.itemCount > 0 -> {
                hadItems = true
                isReloading = false
                enableScrollDetection = true
            }
        }
    }

    LaunchedEffect(enableScrollDetection) {
        if (!enableScrollDetection) return@LaunchedEffect

        var wasAtTop = true
        snapshotFlow {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }.collect { isAtTop ->
            if (!wasAtTop && isAtTop) {
                onScrollToTop()
            }
            wasAtTop = isAtTop
        }
    }

    LaunchedEffect(posts.loadState) {
        onLoadStateChange(posts.loadState.refresh)
    }

    SNSSurface {
        when {
            posts.loadState.refresh is LoadState.Loading && !hadItems -> {
                LoadingScreen(onDismissRequest = {})
            }
            posts.itemCount == 0 && posts.loadState.append.endOfPaginationReached -> {
                EmptyFeedScreen()
            }
            else -> {
                PullToRefreshLayout(
                    refreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = posts.itemCount,
                            key = { posts[it]?.postId ?: it }
                        ) {
                            posts[it]?.let { post ->
                                if (!state.deletedPostIds.contains(post.postId)) {
                                    val combinedComments = remember(
                                        post.postId,
                                        state.addedComments[post.postId],
                                        state.deletedComments[post.postId]
                                    ) {
                                        val initialComments = post.comments.toMutableList()
                                        val addedComments = state.addedComments[post.postId].orEmpty()
                                        val deletedCommentIds = state.deletedComments[post.postId]?.map { it.id }?.toSet() ?: emptySet()

                                        (initialComments + addedComments).distinctBy { it.id }
                                            .filterNot { deletedCommentIds.contains(it.id) }
                                    }

                                    PostCard(
                                        postId = post.postId,
                                        username = post.username,
                                        profileImageUrl = post.profileImageUrl,
                                        images = post.images,
                                        richTextState = post.richTextState,
                                        comments = combinedComments,
                                        isOwner = post.userId == state.myUserId,
                                        onOptionClick = { onOptionClick(post) },
                                        onCommentClick = { onCommentClick(post) }
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
        }
    }
}