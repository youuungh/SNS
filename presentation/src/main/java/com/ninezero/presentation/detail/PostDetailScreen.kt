package com.ninezero.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.ninezero.presentation.component.LeftAlignedDetailScaffold
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.ninezero.presentation.R
import com.ninezero.presentation.component.DeleteCommentDialog
import com.ninezero.presentation.component.DeletePostDialog
import com.ninezero.presentation.component.EmptyPostDetailScreen
import com.ninezero.presentation.component.ErrorDialog
import com.ninezero.presentation.component.LoadingError
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.PostCard
import com.ninezero.presentation.component.bottomsheet.CommentsBottomSheet
import com.ninezero.presentation.component.bottomsheet.EditPostBottomSheet
import com.ninezero.presentation.component.bottomsheet.OptionsBottomSheet
import com.ninezero.presentation.model.PostCardModel

@Composable
fun PostDetailScreen(
    userId: Long,
    postId: Long,
    showComments: Boolean = false,
    commentId: Long? = null,
    viewModel: PostDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToUser: (Long) -> Unit
) {
    val state = viewModel.collectAsState().value
    val posts = state.posts.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(commentId) {
        commentId?.let {
            viewModel.setTargetCommentId(commentId)
        }
    }

    LaunchedEffect(posts.loadState.refresh, state.initialPostId, showComments) {
        if (posts.loadState.refresh is LoadState.NotLoading && posts.itemCount > 0) {
            val index = findPostIndex(posts, state.initialPostId)
            if (index >= 0) {
                listState.scrollToItem(index)

                if (showComments) {
                    posts[index]?.let { post ->
                        viewModel.showCommentsSheet(post)
                    }
                }
            }
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PostDetailSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    LeftAlignedDetailScaffold(
        title = stringResource(R.string.post),
        showBackButton = true,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                LoadingProgress()
            } else {
                when (posts.loadState.refresh) {
                    is LoadState.Loading -> {
                        LoadingProgress()
                    }
                    is LoadState.Error -> {
                        LoadingError(onRetry = { posts.refresh() })
                    }
                    else -> {
                        if (posts.itemCount == 0 && posts.loadState.refresh is LoadState.NotLoading) {
                            EmptyPostDetailScreen()
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    count = posts.itemCount,
                                    key = posts.itemKey { it.postId }
                                ) { index ->
                                    posts[index]?.let { post ->
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
                                            onNavigateToProfile = {
                                                if (post.userId == state.myUserId) {
                                                    onNavigateBack
                                                } else {
                                                    onNavigateToUser(post.userId)
                                                }
                                            },
                                            onNavigateToUser = { onNavigateToUser(post.userId) }
                                        )
                                    }
                                }

                                if (posts.loadState.append is LoadState.Loading) {
                                    item {
                                        LoadingProgress()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    when (state.dialog) {
        is PostDetailDialog.DeletePost -> {
            DeletePostDialog(
                openDialog = true,
                onDismiss = { viewModel.hideDialog() },
                onConfirm = {
                    viewModel.onPostDelete(state.dialog.post)
                    viewModel.hideDialog()
                }
            )
        }

        is PostDetailDialog.DeleteComment -> {
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

        is PostDetailDialog.Error -> {
            ErrorDialog(
                message = state.dialog.message,
                onDismiss = { viewModel.hideDialog() }
            )
        }

        PostDetailDialog.Hidden -> Unit
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
                viewModel.clearTargetCommentId()
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

private fun findPostIndex(posts: LazyPagingItems<PostCardModel>, postId: Long): Int {
    val items = posts.itemSnapshotList.items
    return items.indexOfFirst { it.postId == postId }.takeIf { it >= 0 } ?: 0
}