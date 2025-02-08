package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichText
import com.ninezero.domain.model.Comment
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.SNSTheme
import com.ninezero.presentation.util.formatRelativeTime

@Suppress("UNUSED_PARAMETER")
@Composable
fun PostCard(
    postId: Long,
    username: String,
    profileImageUrl: String? = null,
    images: List<String>,
    richTextState: RichTextState,
    comments: List<Comment>,
    isOwner: Boolean,
    isLiked: Boolean,
    isFollowing: Boolean,
    isSaved: Boolean,
    likesCount: Int,
    createdAt: String,
    onOptionClick: () -> Unit,
    onCommentClick: () -> Unit,
    onLikeClick: () -> Unit,
    onFollowClick: () -> Unit,
    onSavedClick: () -> Unit,
) {
    Surface {
        Column(modifier = Modifier.fillMaxWidth()) {
            key(username, profileImageUrl) {
                PostHeader(
                    username = username,
                    profileImageUrl = profileImageUrl,
                    isOwner = isOwner,
                    isFollowing = isFollowing,
                    onOptionClick = onOptionClick,
                    onFollowClick = onFollowClick
                )
            }

            if (images.isNotEmpty()) {
                key(images) {
                    val pagerState = rememberPagerState(pageCount = { images.size })

                    Box {
                        SNSPostPager(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            images = images,
                            pagerState = pagerState
                        )
                    }

                    if (images.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            HorizontalPagerIndicator(
                                pagerState = pagerState,
                                activeColor = MaterialTheme.colorScheme.primary,
                                inactiveColor = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    key(isLiked, likesCount) {
                        LikeButton(
                            isLiked = isLiked,
                            likesCount = likesCount,
                            onClick = onLikeClick
                        )
                    }
                    key(comments.size) {
                        CommentButton(
                            commentsCount = comments.size,
                            onClick = onCommentClick
                        )
                    }
                }

                if (!isOwner) {
                    key(isSaved) {
                        SaveButton(
                            isSaved = isSaved,
                            onClick = onSavedClick
                        )
                    }
                }
            }

            var isTextOverflow by remember { mutableStateOf(false) }
            var expanded by remember { mutableStateOf(false) }

            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                key(richTextState.annotatedString) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BasicRichText(
                            modifier = Modifier.weight(1f),
                            state = richTextState,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            onTextLayout = { textLayoutResult ->
                                isTextOverflow = textLayoutResult.didOverflowHeight
                            }
                        )

                        if (isTextOverflow && !expanded && !richTextState.annotatedString.text.isBlank()) {
                            Text(
                                text = stringResource(id = R.string.show_more),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        expanded = true
                                    }
                            )
                        }
                    }
                }
                key(createdAt) {
                    Text(
                        text = formatRelativeTime(createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = activeColor.copy(alpha = 0.5f),
    indicatorSize: Dp = 6.dp,
    spacing: Dp = 6.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val currentPage = pagerState.currentPage
        val totalPages = pagerState.pageCount
        val maxVisibleDots = when {
            totalPages < 6 -> totalPages
            currentPage >= 5 && currentPage < totalPages - 1 -> 7
            else -> 6
        }

        val startOffset = when {
            currentPage <= 4 -> 0
            currentPage >= totalPages - 3 -> totalPages - maxVisibleDots
            else -> currentPage - 3
        }

        repeat(maxVisibleDots) { index ->
            val actualIndex = startOffset + index
            val isActive = actualIndex == currentPage
            val isSmallest = when {
                maxVisibleDots >= 6
                        && (currentPage >= 5 && index == 0
                        || index == maxVisibleDots - 1
                        && currentPage < totalPages - 1) -> true
                else -> false
            }

            val targetSize = when {
                isActive -> indicatorSize
                isSmallest -> indicatorSize * 0.4f
                else -> indicatorSize * 0.8f
            }

            val animatedSize by animateDpAsState(
                targetValue = targetSize,
                animationSpec = spring(dampingRatio = 0.8f)
            )

            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .background(
                        color = if (isActive) activeColor else inactiveColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun UserCard(
    userId: Long,
    username: String,
    profileImagePath: String? = null,
    isFollowing: Boolean,
    onFollowClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(150.dp),
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 0.1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            key(profileImagePath) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = profileImagePath,
                        error = painterResource(id = R.drawable.user_placeholder),
                        placeholder = painterResource(id = R.drawable.user_placeholder)
                    ),
                    contentDescription = "profile_image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .bounceClick(),
                    contentScale = ContentScale.Crop
                )
            }

            key(username) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            SNSFollowingButton(
                isFollowing = isFollowing,
                onClick = { onFollowClick(userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PostCardPreview() {
    SNSTheme {
        PostCard(
            postId = 1L,
            username = "Username",
            profileImageUrl = null,
            images = listOf(
                "https://picsum.photos/300/300",
                "https://picsum.photos/300/300",
                "https://picsum.photos/300/300",
                "https://picsum.photos/300/300",
                "https://picsum.photos/300/300",
                "https://picsum.photos/300/300",
                "https://picsum.photos/300/300",
                "https://picsum.photos/300/300",
            ),
            richTextState = RichTextState().apply {
                setText(
                    "이것은 테스트 포스트입니다. 매우 긴 텍스트를 넣어서 더보기가 표시되는지 확인해보겠습니다. " +
                            "이것은 테스트 포스트입니다. 매우 긴 텍스트를 넣어서 더보기가 표시되는지 확인해보겠습니다."
                )
            },
            comments = emptyList(),
            isOwner = false,
            isLiked = false,
            isFollowing = false,
            isSaved = false,
            likesCount = 100,
            createdAt = "2025-01-25T17:14:54.153",
            onOptionClick = {},
            onCommentClick = {},
            onLikeClick = {},
            onFollowClick = {},
            onSavedClick = {}
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserCardPreview() {
    SNSTheme {
        UserCard(
            userId = 1L,
            username = "Username",
            profileImagePath = null,
            isFollowing = false,
            onFollowClick = {}
        )
    }
}