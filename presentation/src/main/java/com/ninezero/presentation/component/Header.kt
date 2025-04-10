package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun PostHeader(
    modifier: Modifier = Modifier,
    username: String,
    profileImageUrl: String? = null,
    isOwner: Boolean,
    isFollowing: Boolean,
    onOptionClick: () -> Unit,
    onFollowClick: () -> Unit,
    onProfileImageClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SNSProfileImage(
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp),
                imageUrl = profileImageUrl,
                onClick = onProfileImageClick
            )
            Text(
                text = username.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (!isOwner) {
                SNSDefaultFollowingButton(
                    isFollowing = isFollowing,
                    onClick = onFollowClick
                )
            }

            AnimatedVisibility(
                visible = isOwner,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SNSIconButton(
                    onClick = onOptionClick,
                    imageVector = Icons.Rounded.MoreVert
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PostHeaderPreview() {
    SNSTheme {
        Surface {
            Column {
                PostHeader(
                    username = "Username",
                    isOwner = true,
                    isFollowing = true,
                    onOptionClick = {},
                    onFollowClick = {},
                    onProfileImageClick = {}
                )
                PostHeader(
                    username = "UsernameUsernameUsernameUsernameUsername",
                    isOwner = false,
                    isFollowing = false,
                    onOptionClick = {},
                    onFollowClick = {},
                    onProfileImageClick = {}
                )
            }
        }
    }
}