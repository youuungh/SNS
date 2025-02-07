package com.ninezero.presentation.component.bottomsheet

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.ninezero.domain.model.Comment
import com.ninezero.presentation.component.SNSIconButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme
import kotlinx.coroutines.launch
import com.ninezero.presentation.component.CommentInputField
import com.ninezero.presentation.component.CommentItem
import com.ninezero.presentation.R

@Composable
fun CommentsBottomSheet(
    showBottomSheet: Boolean,
    comments: List<Comment>,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onCommentSend: (String) -> Unit,
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
    val sortedComments = remember(comments) { comments.sortedByDescending { it.id } }

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
            Column(modifier = Modifier.fillMaxHeight(0.7f)) {
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        if (sortedComments.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.label_no_comment),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(sortedComments) { comment ->
                                CommentItem(
                                    comment = comment,
                                    isOwner = isOwner,
                                    onDeleteComment = { onDeleteComment(comment) }
                                )
                            }
                        }
                    }
                }

                SNSSurface(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 1.dp
                ) {
                    CommentInputField(
                        onSend = { text ->
                            onCommentSend(text)
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                    )
                }
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
            userName = "User One",
            profileImageUrl = null,
            text = "첫 번째 댓글입니다."
        ),
        Comment(
            id = 2L,
            userName = "User Two",
            profileImageUrl = null,
            text = "두 번째 댓글입니다. 조금 더 긴 댓글을 작성해보았습니다."
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
                            onDeleteComment = { }
                        )
                    }
                }
            }

            SNSSurface(
                modifier = Modifier.fillMaxWidth(),
                elevation = 1.dp
            ) {
                CommentInputField(
                    onSend = { },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}