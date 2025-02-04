package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.domain.model.Comment
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun CommentItem(
    comment: Comment,
    isOwner: Boolean,
    onDeleteComment: (Comment) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SNSProfileImage(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Top),
            imageUrl = comment.profileImageUrl
        )

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
                text = comment.text,
                style = MaterialTheme.typography.labelMedium
            )
        }

        if (isOwner) {
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

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CommentItemPreview() {
    SNSTheme {
        CommentItem(
            comment = Comment(
                id = 1L,
                userName = "User",
                profileImageUrl = null,
                text = "This is a sample comment, This is a sample comment, This is a sample comment"
            ),
            isOwner = true,
            onDeleteComment = { }
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatisticItemPreview() {
    SNSTheme {
        StatisticItem(
            count = 100,
            label = "게시물"
        )
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