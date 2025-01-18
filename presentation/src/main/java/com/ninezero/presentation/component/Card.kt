package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichText
import com.ninezero.domain.model.Comment
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.LocalTheme
import com.ninezero.presentation.theme.SNSTheme
import com.ninezero.presentation.theme.snsSmallButtonDarkBackground
import com.ninezero.presentation.theme.snsSmallButtonDarkText
import com.ninezero.presentation.theme.snsSmallButtonLightBackground
import com.ninezero.presentation.theme.snsSmallButtonLightText

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
    onOptionClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val isDarkTheme = LocalTheme.current

    Surface {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            PostHeader(
                isOwner = isOwner,
                profileImageUrl = profileImageUrl,
                username = username,
                onOptionClick = onOptionClick
            )

            if (images.isNotEmpty()) {
                SNSPostPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    images = images,
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .background(
                        color = if (isDarkTheme) {
                            snsSmallButtonDarkBackground
                        } else {
                            snsSmallButtonLightBackground
                        },
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .bounceClick()
                    .clickable { onCommentClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_comment),
                    contentDescription = "comments",
                    modifier = Modifier.size(24.dp),
                    tint = if (isDarkTheme) {
                        snsSmallButtonDarkText
                    } else {
                        snsSmallButtonLightText
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comments.size.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = if (isDarkTheme) {
                        snsSmallButtonDarkText
                    } else {
                        snsSmallButtonLightText
                    }
                )
            }

            var maxLines by remember(richTextState) { mutableIntStateOf(1) }
            var showMore by remember { mutableStateOf(false) }
            var isExpanded by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicRichText(
                        modifier = Modifier.weight(1f),
                        state = richTextState,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        onTextLayout = { textLayoutResult ->
                            showMore = textLayoutResult.didOverflowHeight
                        }
                    )

                    if (showMore && !isExpanded && !richTextState.annotatedString.text.isBlank()) {
                        Text(
                            text = stringResource(id = R.string.show_more),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    maxLines = Int.MAX_VALUE
                                    isExpanded = true
                                }
                        )
                    }
                }

                if (isExpanded) {
                    Text(
                        text = stringResource(id = R.string.show_less),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                maxLines = 1
                                isExpanded = false
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                "https://picsum.photos/300/300"
            ),
            richTextState = RichTextState().apply {
                setText("이것은 테스트 포스트입니다. 매우 긴 텍스트를 넣어서 더보기가 표시되는지 확인해보겠습니다. " +
                        "이것은 테스트 포스트입니다. 매우 긴 텍스트를 넣어서 더보기가 표시되는지 확인해보겠습니다.")
            },
            comments = emptyList(),
            isOwner = true,
            onOptionClick = {},
            onCommentClick = {}
        )
    }
}