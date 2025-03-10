package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Notification
import com.ninezero.domain.model.chat.ChatMessage
import com.ninezero.domain.model.chat.ChatRoom
import com.ninezero.domain.model.chat.ChatRoomParticipant
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.LocalTheme
import com.ninezero.presentation.theme.SNSTheme
import com.ninezero.presentation.theme.snsDarkAccent
import com.ninezero.presentation.theme.snsDarkBg3
import com.ninezero.presentation.theme.snsDarkBg4
import com.ninezero.presentation.theme.snsDefault
import com.ninezero.presentation.theme.snsDefaultLight
import com.ninezero.presentation.theme.snsLightBg2
import com.ninezero.presentation.util.formatChatDateTime
import com.ninezero.presentation.util.formatChatTime
import com.ninezero.presentation.util.formatRelativeTime
import com.ninezero.presentation.util.formatSimpleChatDateTime

@Composable
fun CommentItem(
    comment: Comment,
    isOwner: Boolean,
    myUserId: Long,
    isReply: Boolean = false,
    isExpanded: Boolean = false,
    isLoadingReplies: Boolean = false,
    replies: List<Comment> = emptyList(),
    onReplyClick: (Comment) -> Unit,
    onToggleReplies: (Long) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = if (isReply) 46.dp else 0.dp)
        ) {
            SNSProfileImage(
                modifier = Modifier
                    .size(if (isReply) 24.dp else 32.dp)
                    .align(Alignment.Top),
                imageUrl = comment.profileImageUrl
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = comment.userName.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = buildAnnotatedString {
                                if (comment.depth == 1) {
                                    val mentionName = when {
                                        comment.replyToUserName != null -> comment.replyToUserName
                                        comment.parentUserName != null -> comment.parentUserName
                                        else -> null
                                    }

                                    // 텍스트가 이미 @멘션으로 시작하는지 확인
                                    mentionName?.let {
                                        val text = comment.text
                                        val capitalisedMentionName = mentionName.replaceFirstChar { it.uppercase() }
                                        val startsWithMention = text.trimStart().startsWith("@$mentionName") ||
                                                text.trimStart().startsWith("@$capitalisedMentionName")

                                        // 텍스트가 멘션으로 시작하지 않을 경우에만 앞에 추가
                                        if (!startsWithMention) {
                                            withStyle(
                                                style = SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) {
                                                append("@${capitalisedMentionName} ")
                                            }
                                        }
                                    }
                                }

                                // 댓글 텍스트 내의 @멘션 처리
                                val text = comment.text
                                if (text.contains("@") && comment.mentionedUserIds != null) {
                                    // 정규식으로 @username 패턴 찾기
                                    val mentionPattern = Regex("@(\\w+)")
                                    val matches = mentionPattern.findAll(text)

                                    var lastIndex = 0
                                    for (match in matches) {
                                        // 멘션 앞 텍스트 추가
                                        append(text.substring(lastIndex, match.range.first))

                                        // 멘션된 사용자명 추출하고 첫 글자 대문자로 변환
                                        val mentionedName = text.substring(match.range.first + 1, match.range.last + 1)
                                        val capitalisedName = mentionedName.replaceFirstChar { it.uppercase() }

                                        // 멘션된 사용자 강조
                                        withStyle(
                                            style = SpanStyle(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append("@$capitalisedName")
                                        }

                                        lastIndex = match.range.last + 1
                                    }

                                    // 나머지 텍스트
                                    if (lastIndex < text.length) {
                                        append(text.substring(lastIndex))
                                    }
                                } else {
                                    append(text)
                                }
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (isOwner || myUserId == comment.userId) {
                        SNSIconButton(
                            onClick = { onDeleteComment(comment) },
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "delete",
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }

                // 답글 달기 버튼
                if (myUserId != comment.userId) {
                    Text(
                        text = stringResource(R.string.reply),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onReplyClick(comment) }
                    )
                }
            }
        }

        if (!isReply && comment.replyCount > 0) {
            Column {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 46.dp, top = 4.dp, bottom = 4.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        replies.forEachIndexed { _, reply ->
                            key(reply.id) {
                                CommentItem(
                                    comment = reply,
                                    isOwner = isOwner,
                                    myUserId = myUserId,
                                    isReply = true,
                                    onReplyClick = onReplyClick,
                                    onToggleReplies = onToggleReplies,
                                    onDeleteComment = onDeleteComment,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 32.dp, top = 12.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onToggleReplies(comment.id) }
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(42.dp)
                            .height(1.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )

                    Text(
                        text = when {
                            isLoadingReplies -> stringResource(R.string.loading_replies)
                            isExpanded -> stringResource(R.string.hide_replies)
                            else -> stringResource(R.string.view_replies_count, comment.replyCount)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticItem(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ItemSection(
    title: String,
    onShowAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            SNSTextButton(
                text = stringResource(id = R.string.show_all),
                onClick = onShowAllClick,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        content()
    }
}

@Composable
fun ClearSection(
    title: String,
    onClearAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            SNSTextButton(
                text = stringResource(id = R.string.clear_all),
                onClick = onClearAllClick,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        content()
    }
}

@Composable
fun ChatDateHeader(
    date: String,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    val backgroundColor = if (isDarkTheme) {
        snsDarkBg3
    } else {
        snsDefaultLight
    }

    val textColor = if (isDarkTheme) {
        snsDarkAccent
    } else {
        snsDefault
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        ) {
            Text(
                text = formatChatDateTime(date),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isOwner: Boolean,
    profileImageUrl: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (isOwner) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isOwner) {
            key(profileImageUrl) {
                SNSProfileImage(
                    imageUrl = profileImageUrl,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isOwner) Alignment.End else Alignment.Start
        ) {
            if (!isOwner) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Row(
                horizontalArrangement = if (isOwner) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                if (!isOwner) {
                    MessageBubble(
                        message = message,
                        isOwner = isOwner
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatChatTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = formatChatTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    MessageBubble(
                        message = message,
                        isOwner = isOwner
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isOwner: Boolean,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalTheme.current

    val bubbleColor = if (isOwner) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDarkTheme) snsDarkBg4 else snsLightBg2
    }

    val textColor = if (isOwner) {
        if (isDarkTheme) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.surface
        }
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isOwner) 16.dp else 4.dp,
        bottomEnd = if (isOwner) 4.dp else 16.dp
    )

    Surface(
        color = bubbleColor,
        shape = bubbleShape,
        modifier = modifier.widthIn(max = 260.dp)
    ) {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ChatRoomItem(
    room: ChatRoom,
    myUserId: Long,
    onItemClick: (ChatRoom) -> Unit,
    onLeaveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { onItemClick(room) },
                onLongClick = { onLeaveClick(room.id) }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            val otherParticipant = room.participants.firstOrNull {
                it.userId != myUserId
            }

            key(otherParticipant?.profileImagePath) {
                SNSProfileImage(
                    imageUrl = otherParticipant?.profileImagePath,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = otherParticipant?.userName ?: room.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                room.lastMessage?.let { message ->
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                room.lastMessage?.let { message ->
                    Text(
                        text = formatSimpleChatDateTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                val unreadCount = room.participants
                    .firstOrNull { it.userId == myUserId }
                    ?.unreadCount ?: 0

                if (unreadCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick()
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onDelete
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            SNSProfileImage(
                imageUrl = notification.senderProfileImagePath,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Top)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = getNotificationText(notification),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatRelativeTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun getNotificationText(notification: Notification): String {
    return when (notification.type) {
        "like" -> "${notification.senderName ?: ""}님이 회원님의 게시물을 좋아합니다."
        "comment" -> notification.body
        "reply" -> notification.body
        "follow" -> "${notification.senderName ?: ""}님이 회원님을 팔로우하기 시작했습니다."
        "chat" -> "${notification.senderName ?: ""}님이 메시지를 보냈습니다: ${notification.body}"
        else -> notification.body
    }
}

@Composable
fun NotificationSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CommentItemPreview() {
    SNSTheme {
        SNSSurface {
            CommentItem(
                comment = Comment(
                    id = 1L,
                    userId = 1L,
                    text = "This is a sample comment, This is a sample comment, This is a sample comment",
                    userName = "User",
                    profileImageUrl = null,
                    parentId = null,
                    parentUserName = null,
                    depth = 0,
                    mentionedUserIds = null,
                    replyCount = 2,
                    replyToCommentId = null,
                    replyToUserName = null,
                    isExpanded = true,
                    replies = listOf(
                        Comment(
                            id = 2L,
                            userId = 2L,
                            text = "First reply comment",
                            userName = "ReplyUser1",
                            profileImageUrl = null,
                            parentId = 1L,
                            parentUserName = "User",
                            depth = 1,
                            mentionedUserIds = listOf(1L),
                            replyToCommentId = 1L,
                            replyToUserName = "User",
                            replyCount = 0
                        ),
                        Comment(
                            id = 3L,
                            userId = 3L,
                            text = "@ReplyUser1 Second reply comment",
                            userName = "ReplyUser2",
                            profileImageUrl = null,
                            parentId = 1L,
                            parentUserName = "User",
                            depth = 1,
                            mentionedUserIds = listOf(2L),
                            replyToCommentId = 2L,
                            replyToUserName = "ReplyUser1",
                            replyCount = 0
                        )
                    )
                ),
                isOwner = true,
                myUserId = 1L,
                onReplyClick = {},
                onToggleReplies = {},
                onDeleteComment = {}
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatisticItemPreview() {
    SNSTheme {
        SNSSurface {
            StatisticItem(
                count = 100,
                label = "게시물"
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ItemSectionPreview() {
    SNSTheme {
        Surface {
            ItemSection(
                title = "Section Name",
                onShowAllClick = {},
                content = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClearSectionPreview() {
    SNSTheme {
        Surface {
            ClearSection(
                title = "Section Name",
                onClearAllClick = {},
                content = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChatRoomItemPreview() {
    SNSTheme {
        Surface {
            val room = ChatRoom(
                id = "room1",
                name = "채팅방",
                participants = listOf(
                    ChatRoomParticipant(
                        userId = 1L,
                        userLoginId = "user1",
                        userName = "User1",
                        profileImagePath = null,
                        unreadCount = 3,
                        lastReadMessageId = "msg0",
                        leaveTimestamp = null
                    ),
                    ChatRoomParticipant(
                        userId = 2L,
                        userLoginId = "user2",
                        userName = "User2",
                        profileImagePath = null,
                        unreadCount = 0,
                        lastReadMessageId = "msg1",
                        leaveTimestamp = null
                    )
                ),
                lastMessage = ChatMessage(
                    id = "msg1",
                    content = "안녕하세요! 메시지 미리보기입니다.",
                    senderId = 2L,
                    senderName = "User2",
                    roomId = "room1",
                    createdAt = "2024-02-24T14:30:00",
                    leaveTimestamp = null
                ),
                messageCount = 10,
                createdAt = "2024-02-24T10:00:00"
            )

            ChatRoomItem(
                room = room,
                myUserId = 1L,
                onItemClick = {},
                onLeaveClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChatMessageItemPreview() {
    SNSTheme {
        SNSSurface {
            Column {
                val message = ChatMessage(
                    id = "1",
                    content = "안녕하세요?",
                    senderId = 1,
                    senderName = "Username",
                    roomId = "room1",
                    createdAt = "2099-09-99T99:90:00",
                    leaveTimestamp = null
                )
                ChatDateHeader(
                    date = "2099-09-99T99:90:00",
                    modifier = Modifier.fillMaxWidth()
                )
                ChatMessageItem(
                    message = message,
                    isOwner = false,
                    profileImageUrl = null,
                    modifier = Modifier.fillMaxWidth()
                )
                ChatMessageItem(
                    message = message.copy(content = "네, 안녕하세요!"),
                    isOwner = true,
                    profileImageUrl = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessageBubblePreview() {
    SNSTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // 현재 시간의 메시지
            val currentMessage = ChatMessage(
                id = "1",
                content = "안녕하세요, 반갑습니다!",
                senderId = 1,
                senderName = "Username",
                roomId = "room1",
                createdAt = "2024-02-13T10:30:00",
                leaveTimestamp = null
            )

            // 이전 메시지들
            val previousMessages = listOf(
                ChatMessage(
                    id = "2",
                    content = "네, 말씀하세요 :)",
                    senderId = 2,
                    senderName = "Other User",
                    roomId = "room1",
                    createdAt = "2024-02-13T09:15:00",
                    leaveTimestamp = null
                ),
                ChatMessage(
                    id = "3",
                    content = "긴 메시지 예시입니다. 메시지 버블이 어떻게 보이는지 확인하기 위한 긴 텍스트 컨텐츠를 포함하고 있습니다.",
                    senderId = 1,
                    senderName = "Username",
                    roomId = "room1",
                    createdAt = "2024-02-13T09:00:00",
                    leaveTimestamp = null
                )
            )

            Text(
                text = "내 메시지",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MessageBubble(
                message = currentMessage,
                isOwner = true
            )

            Text(
                text = "상대방 메시지",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            MessageBubble(
                message = previousMessages[0],
                isOwner = false
            )

            Text(
                text = "긴 메시지",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            MessageBubble(
                message = previousMessages[1],
                isOwner = true
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CommentWithRepliesPreview() {
    val replies = listOf(
        Comment(
            id = 2L,
            userId = 2L,
            text = "첫 번째 답글입니다.",
            userName = "Reply1",
            profileImageUrl = null,
            parentId = 1L,
            parentUserName = "Parent",
            depth = 1,
            mentionedUserIds = listOf(1L),
            replyToCommentId = 1L,
            replyToUserName = "Parent",
            replyCount = 0
        ),
        Comment(
            id = 3L,
            userId = 3L,
            text = "@Reply1 두 번째 답글입니다. 이것은 첫 번째 답글에 대한 답글입니다.",
            userName = "Reply2",
            profileImageUrl = null,
            parentId = 1L,
            parentUserName = "Parent",
            depth = 1,
            mentionedUserIds = listOf(2L),
            replyToCommentId = 2L,
            replyToUserName = "Reply1",
            replyCount = 0
        )
    )

    SNSTheme {
        Surface {
            CommentItem(
                comment = Comment(
                    id = 1L,
                    userId = 1L,
                    text = "첫 번째 댓글입니다. 이것은 부모 댓글이며 두 개의 답글이 달려있습니다.",
                    userName = "Parent",
                    profileImageUrl = null,
                    parentId = null,
                    parentUserName = null,
                    depth = 0,
                    mentionedUserIds = null,
                    replyCount = 2,
                    replyToCommentId = null,
                    replyToUserName = null
                ),
                isOwner = true,
                myUserId = 1L,
                isReply = false,
                isExpanded = true,
                isLoadingReplies = false,
                replies = replies,
                onReplyClick = {},
                onToggleReplies = {},
                onDeleteComment = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ReplyChainPreview() {
    val parentComment = Comment(
        id = 1L,
        userId = 1L,
        text = "원본 댓글입니다.",
        userName = "User1",
        profileImageUrl = null,
        parentId = null,
        parentUserName = null,
        depth = 0,
        mentionedUserIds = null,
        replyCount = 3,
        replyToCommentId = null,
        replyToUserName = null
    )

    val replies = listOf(
        Comment(
            id = 2L,
            userId = 2L,
            text = "첫 번째 대댓글입니다.",
            userName = "User2",
            profileImageUrl = null,
            parentId = 1L,
            parentUserName = "User1",
            depth = 1,
            mentionedUserIds = listOf(1L),
            replyToCommentId = 1L,
            replyToUserName = "User1",
            replyCount = 0
        ),
        Comment(
            id = 3L,
            userId = 3L,
            text = "@User2 두 번째 대댓글입니다. User2님에게 답변합니다.",
            userName = "User3",
            profileImageUrl = null,
            parentId = 1L,
            parentUserName = "User1",
            depth = 1,
            mentionedUserIds = listOf(2L),
            replyToCommentId = 2L,
            replyToUserName = "User2",
            replyCount = 0
        ),
        Comment(
            id = 4L,
            userId = 4L,
            text = "@User3 세 번째 대댓글입니다. User3님에게 답변합니다.",
            userName = "User4",
            profileImageUrl = null,
            parentId = 1L,
            parentUserName = "User1",
            depth = 1,
            mentionedUserIds = listOf(3L),
            replyToCommentId = 3L,
            replyToUserName = "User3",
            replyCount = 0
        )
    )

    SNSTheme {
        Surface {
            CommentItem(
                comment = parentComment,
                isOwner = false,
                myUserId = 5L, // 현재 사용자는 댓글 작성자가 아님
                isReply = false,
                isExpanded = true,
                isLoadingReplies = false,
                replies = replies,
                onReplyClick = {},
                onToggleReplies = {},
                onDeleteComment = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationItemPreview() {
    SNSTheme {
        Surface {
            NotificationItem(
                notification = Notification(
                    id = 1L,
                    type = "like",
                    body = "회원님의 게시물을 좋아합니다.",
                    senderId = 123L,
                    senderLoginId = "123",
                    senderName = "user123",
                    senderProfileImagePath = null,
                    boardId = 456L,
                    commentId = null,
                    roomId = null,
                    isRead = false,
                    createdAt = "2025-02-27T14:30:00"
                ),
                onClick = { },
                onDelete = { }
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationItemReadStatePreview() {
    SNSTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationItem(
                    notification = Notification(
                        id = 1L,
                        type = "like",
                        body = "회원님의 게시물을 좋아합니다.",
                        senderId = 123L,
                        senderLoginId = "123",
                        senderName = "user123",
                        senderProfileImagePath = null,
                        boardId = 456L,
                        commentId = null,
                        roomId = null,
                        isRead = false,
                        createdAt = "2025-02-27T14:30:00"
                    ),
                    onClick = { },
                    onDelete = { }
                )

                NotificationItem(
                    notification = Notification(
                        id = 2L,
                        type = "like",
                        body = "회원님의 게시물을 좋아합니다.",
                        senderId = 123L,
                        senderLoginId = "123",
                        senderName = "user123",
                        senderProfileImagePath = null,
                        boardId = 456L,
                        commentId = null,
                        roomId = null,
                        isRead = true,
                        createdAt = "2025-02-27T10:30:00"
                    ),
                    onClick = { },
                    onDelete = { }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationItemTypesPreview() {
    SNSTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationItem(
                    notification = Notification(
                        id = 1L,
                        type = "like",
                        body = "회원님의 게시물을 좋아합니다.",
                        senderId = 123L,
                        senderLoginId = "123",
                        senderName = "user123",
                        senderProfileImagePath = null,
                        boardId = 456L,
                        commentId = null,
                        roomId = null,
                        isRead = false,
                        createdAt = "2025-02-27T14:30:00"
                    ),
                    onClick = { },
                    onDelete = { }
                )

                NotificationItem(
                    notification = Notification(
                        id = 2L,
                        type = "comment",
                        body = "멋진 사진이네요! 어디서 찍으셨나요?",
                        senderId = 234L,
                        senderLoginId = "234",
                        senderName = "commenter",
                        senderProfileImagePath = null,
                        boardId = 456L,
                        commentId = 789L,
                        roomId = null,
                        isRead = true,
                        createdAt = "2025-02-26T09:15:00"
                    ),
                    onClick = { },
                    onDelete = { }
                )

                NotificationItem(
                    notification = Notification(
                        id = 3L,
                        type = "follow",
                        body = "회원님을 팔로우하기 시작했습니다.",
                        senderId = 345L,
                        senderLoginId = "345",
                        senderName = "new_follower",
                        senderProfileImagePath = null,
                        boardId = null,
                        commentId = null,
                        roomId = null,
                        isRead = false,
                        createdAt = "2025-02-25T18:42:00"
                    ),
                    onClick = { },
                    onDelete = { }
                )

                NotificationItem(
                    notification = Notification(
                        id = 4L,
                        type = "chat",
                        body = "안녕하세요! 메시지 드립니다.",
                        senderId = 456L,
                        senderLoginId = "456",
                        senderName = "messenger",
                        senderProfileImagePath = null,
                        boardId = null,
                        commentId = null,
                        roomId = "room123",
                        isRead = true,
                        createdAt = "2025-02-24T10:05:00"
                    ),
                    onClick = { },
                    onDelete = { }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotificationSectionHeaderPreview() {
    SNSTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NotificationSectionHeader(title = "오늘")
                NotificationSectionHeader(title = "어제")
                NotificationSectionHeader(title = "최근 30일")
                NotificationSectionHeader(title = "이전 활동")
            }
        }
    }
}