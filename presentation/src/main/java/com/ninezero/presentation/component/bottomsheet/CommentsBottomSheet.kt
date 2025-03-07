package com.ninezero.presentation.component.bottomsheet

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.ninezero.domain.model.Comment
import com.ninezero.presentation.component.SNSIconButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme
import kotlinx.coroutines.launch
import com.ninezero.presentation.component.CommentItem
import com.ninezero.presentation.R
import com.ninezero.presentation.component.AppendError
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.SNSCommentInputField
import com.ninezero.presentation.component.ShimmerCommentCards
import kotlinx.coroutines.delay

@Composable
fun CommentsBottomSheet(
    showBottomSheet: Boolean,
    isLoadingComments: Boolean,
    comments: LazyPagingItems<Comment>,
    isOwner: Boolean,
    myUserId: Long,
    replyToComment: Comment? = null,
    expandedCommentIds: Map<Long, Boolean>,
    loadingReplyIds: Set<Long>,
    replies: Map<Long, List<Comment>>,
    targetCommentId: Long? = null,
    onDismiss: () -> Unit,
    onCommentSend: (String) -> Unit,
    onReplyClick: (Comment) -> Unit,
    onCancelReply: () -> Unit,
    onToggleReplies: (Long) -> Unit,
    onDeleteComment: (Comment) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var isClosing by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            when {
                it == SheetValue.Hidden && isClosing -> true
                it == SheetValue.Hidden -> false
                else -> true
            }
        }
    )

    LaunchedEffect(comments.loadState.refresh, targetCommentId) {
        if (comments.loadState.refresh is LoadState.NotLoading && targetCommentId != null) {
            delay(500) // 댓글 로드 후 약간의 지연 필요

            // 먼저 메인 댓글 목록에서 대상 댓글 찾기
            var targetIndex = -1
            for (i in 0 until comments.itemCount) {
                comments[i]?.let { comment ->
                    if (comment.id == targetCommentId) {
                        targetIndex = i
                        return@let
                    }
                }
            }

            if (targetIndex >= 0) {
                // 댓글 찾음, 해당 위치로 스크롤
                listState.animateScrollToItem(targetIndex)
            } else {
                // 대댓글(답글)인 경우 부모 댓글 찾기
                var foundParent = false
                for (i in 0 until comments.itemCount) {
                    if (foundParent) break

                    comments[i]?.let { comment ->
                        if (comment.replyCount > 0) {
                            // 이 댓글의 답글 목록에 있는지 확인하기 위해 펼치기
                            onToggleReplies(comment.id)

                            // 펼침 상태 확인
                            delay(300) // 답글 로드 대기

                            // 이 댓글의 답글 목록에 대상 댓글 있는지 확인
                            val repliesList = replies[comment.id] ?: emptyList()
                            if (repliesList.any { it.id == targetCommentId }) {
                                // 부모 댓글로 스크롤
                                listState.animateScrollToItem(i)
                                foundParent = true
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    isClosing = true
                    try {
                        sheetState.hide()
                    } finally {
                        onDismiss()
                        isClosing = false
                    }
                }
            },
            sheetState = sheetState,
            dragHandle = null,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0) }
        ) {
            Column(modifier = Modifier.fillMaxHeight(0.8f)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.comment),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        SNSIconButton(
                            onClick = {
                                scope.launch {
                                    isClosing = true
                                    try {
                                        sheetState.hide()
                                    } finally {
                                        onDismiss()
                                        isClosing = false
                                    }
                                }
                            },
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "close",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    windowInsets = WindowInsets(0)
                )

                SNSSurface(modifier = Modifier.weight(1f)) {
                    when (comments.loadState.refresh) {
                        is LoadState.Loading -> {
                            if (isLoadingComments) {
                                repeat(3) { ShimmerCommentCards() }
                            }
                        }

                        else -> {
                            if (comments.itemCount == 0) {
                                Text(
                                    text = stringResource(R.string.label_no_comment),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier
                                        .padding(top = 60.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    items(
                                        count = comments.itemCount,
                                        key = comments.itemKey { it.id },
                                        contentType = { "comment" }
                                    ) { index ->
                                        comments[index]?.let { comment ->
                                            CommentItem(
                                                comment = comment,
                                                isOwner = isOwner,
                                                myUserId = myUserId,
                                                isExpanded = expandedCommentIds[comment.id] == true,
                                                isLoadingReplies = loadingReplyIds.contains(comment.id),
                                                replies = replies[comment.id] ?: emptyList(),
                                                onReplyClick = onReplyClick,
                                                onToggleReplies = onToggleReplies,
                                                onDeleteComment = onDeleteComment
                                            )
                                        }
                                    }

                                    when (comments.loadState.append) {
                                        is LoadState.Loading -> {
                                            item { LoadingProgress() }
                                        }

                                        is LoadState.Error -> {
                                            item {
                                                AppendError(
                                                    onRetry = { comments.retry() }
                                                )
                                            }
                                        }

                                        else -> Unit
                                    }
                                }
                            }
                        }
                    }
                }

                SNSCommentInputField(
                    hint = R.string.label_input_comment,
                    replyToComment = replyToComment,
                    onSend = { text ->
                        onCommentSend(text)
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    onCancelReply = onCancelReply
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CommentsBottomSheetContentPreview() {
    val sampleComments = listOf(
        Comment(
            id = 1L,
            userId = 1L,
            text = "첫 번째 댓글입니다.",
            userName = "UserOne",
            profileImageUrl = null,
            parentId = null,
            parentUserName = null,
            depth = 0,
            replyCount = 2,
            isExpanded = true,
            replies = listOf(
                Comment(
                    id = 2L,
                    userId = 1L,
                    text = "첫 번째 댓글의 첫 번째 답글입니다.",
                    userName = "ReplyUser1",
                    profileImageUrl = null,
                    parentId = 1L,
                    parentUserName = "UserOne",
                    depth = 1,
                    replyCount = 0
                ),
                Comment(
                    id = 3L,
                    userId = 1L,
                    text = "첫 번째 댓글의 두 번째 답글입니다.",
                    userName = "ReplyUser2",
                    profileImageUrl = null,
                    parentId = 1L,
                    parentUserName = "UserOne",
                    depth = 1,
                    replyCount = 0
                )
            )
        ),
        Comment(
            id = 4L,
            userId = 1L,
            text = "두 번째 댓글입니다. 조금 더 긴 댓글을 작성해보았습니다.",
            userName = "UserTwo",
            profileImageUrl = null,
            parentId = null,
            parentUserName = null,
            depth = 0,
            replyCount = 0
        )
    )

    SNSTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            SNSSurface(
                modifier = Modifier.fillMaxWidth(),
                elevation = 1.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "댓글",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        SNSIconButton(
                            onClick = { },
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "close",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
            }

            SNSSurface {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(sampleComments) { comment ->
                        CommentItem(
                            comment = comment,
                            isOwner = true,
                            myUserId = 1L,
                            isExpanded = comment.isExpanded,
                            isLoadingReplies = false,
                            replies = comment.replies,
                            onReplyClick = {},
                            onToggleReplies = {},
                            onDeleteComment = {}
                        )
                    }
                }
            }

            SNSSurface(
                modifier = Modifier.fillMaxWidth(),
                elevation = 1.dp
            ) {
                SNSCommentInputField(
                    hint = R.string.label_input_comment,
                    onSend = {},
                    onCancelReply = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}