package com.ninezero.presentation.feed

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
import androidx.paging.compose.itemKey
import com.ninezero.presentation.component.DeleteCommentDialog
import com.ninezero.presentation.component.DeletePostDialog
import com.ninezero.presentation.component.AppendEnd
import com.ninezero.presentation.component.ErrorDialog
import com.ninezero.presentation.component.AppendError
import com.ninezero.presentation.component.EmptyFeedScreen
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.PostCard
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.ShimmerPostCards
import com.ninezero.presentation.component.TopFAB
import com.ninezero.presentation.component.bottomsheet.CommentsBottomSheet
import com.ninezero.presentation.component.bottomsheet.EditPostBottomSheet
import com.ninezero.presentation.component.bottomsheet.OptionsBottomSheet
import com.ninezero.presentation.util.onScroll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FeedScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToUser: (Long) -> Unit
) {
    val state = viewModel.collectAsState().value
    val posts = state.posts.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isInit by remember { mutableStateOf(true) }
    var showFab by remember { mutableStateOf(false) }
    var lastScrollTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(posts.loadState.refresh) {
        if (posts.loadState.refresh is LoadState.NotLoading) {
            isInit = false
        }
    }

    LaunchedEffect(lastScrollTime) {
        if (lastScrollTime > 0) {
            delay(3000)
            if (System.currentTimeMillis() - lastScrollTime >= 3000) {
                showFab = false
            }
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
        PullToRefreshLayout(
            refreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            when (posts.loadState.refresh) {
                is LoadState.Loading -> {
                    if (isInit && !state.isRefreshing) {
                        repeat(3) { ShimmerPostCards() }
                    }
                }

                else -> {
                    if (posts.itemCount == 0 && !isInit) {
                        EmptyFeedScreen()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .onScroll { _, _ ->
                                    if (listState.firstVisibleItemIndex > 0) {
                                        showFab = true
                                        lastScrollTime = System.currentTimeMillis()
                                    } else {
                                        showFab = false
                                    }
                                }
                        ) {
                            items(
                                count = posts.itemCount,
                                key = posts.itemKey { it.postId }
                            ) { index ->
                                posts[index]?.let { post ->
                                    if (!state.deletedPostIds.contains(post.postId)) {
                                        PostCard(
                                            postId = post.postId,
                                            userId = post.userId,
                                            username = post.userName,
                                            profileImageUrl = post.profileImageUrl,
                                            images = post.images,
                                            richTextState = post.richTextState,
                                            isOwner = post.userId == state.myUserId,
                                            commentCount = state.commentCount[post.postId] ?: post.commentCount,
                                            likesCount = state.likesCount[post.postId] ?: post.likesCount,
                                            isLiked = state.isLiked[post.postId] ?: post.isLiked,
                                            isFollowing = state.isFollowing[post.userId] ?: post.isFollowing,
                                            isSaved = state.isSaved[post.postId] ?: post.isSaved,
                                            createdAt = post.createdAt,
                                            onOptionClick = { viewModel.showOptionsSheet(post) },
                                            onCommentClick = { viewModel.showCommentsSheet(post) },
                                            onLikeClick = { viewModel.handleLikeClick(post.postId, post) },
                                            onFollowClick = { viewModel.handleFollowClick(post.userId, post) },
                                            onSavedClick = { viewModel.handleSavedClick(post.postId, post) },
                                            onNavigateToProfile = onNavigateToProfile,
                                            onNavigateToUser = { onNavigateToUser(post.userId) }
                                        )
                                    }
                                }
                            }

                            item {
                                when (val append = posts.loadState.append) {
                                    is LoadState.Loading -> {
                                        if (posts.loadState.refresh !is LoadState.Loading) {
                                            LoadingProgress()
                                        }
                                    }

                                    is LoadState.Error -> {
                                        AppendError(onRetry = { posts.retry() })
                                    }

                                    is LoadState.NotLoading -> {
                                        if (append.endOfPaginationReached) {
                                            AppendEnd()
                                        }
                                    }
                                }
                            }
                        }

                        TopFAB(
                            visible = showFab,
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                    showFab = false
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }

    when (state.dialog) {
        is FeedDialog.DeletePost -> {
            DeletePostDialog(
                openDialog = true,
                onDismiss = { viewModel.hideDialog() },
                onConfirm = {
                    viewModel.onPostDelete(state.dialog.post)
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
                        state.dialog.postId,
                        state.dialog.comment
                    )
                    viewModel.hideDialog()
                }
            )
        }

        is FeedDialog.Error -> {
            ErrorDialog(
                message = state.dialog.message,
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
            },
            onEdit = {
                viewModel.showEditSheet(post)
                viewModel.hideOptionsSheet()
            }
        )
    }

    state.editSheetPost?.let { post ->
        EditPostBottomSheet(
            showBottomSheet = true,
            post = post,
            isEditing = state.isEditing,
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { content, images ->
                viewModel.onPostEdit(post.postId, content, images)
            }
        )
    }

    state.commentsSheetPost?.let { post ->
        val comments = state.comments.collectAsLazyPagingItems()

        CommentsBottomSheet(
            showBottomSheet = true,
            isLoadingComments = state.isLoadingComments,
            comments = comments,
            isOwner = post.userId == state.myUserId,
            myUserId = state.myUserId,
            replyToComment = state.replyToComment,
            expandedCommentIds = state.expandedCommentIds,
            loadingReplyIds = state.loadingReplyIds,
            replies = state.replies,
            targetCommentId = state.targetCommentId,
            onDismiss = {
                viewModel.hideCommentsSheet()
                viewModel.setReplyToComment(null)
            },
            onCommentSend = { text, mentionedUserIds, replyToCommentId ->
                viewModel.onCommentSend(post.postId, text, mentionedUserIds, replyToCommentId)
            },
            onReplyClick = { comment -> viewModel.setReplyToComment(comment) },
            onCancelReply = { viewModel.setReplyToComment(null) },
            onToggleReplies = { commentId -> viewModel.toggleRepliesVisibility(commentId) },
            onDeleteComment = { comment -> viewModel.showDeleteCommentDialog(post.postId, comment) }
        )
    }
}