package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.domain.model.Comment
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