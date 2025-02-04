package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ninezero.presentation.R
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun SNSProfileImage(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    onClick: () -> Unit = {}
) {
    Box {
        Image(
            modifier = modifier
                .clip(CircleShape)
                .clickable { onClick() },
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                error = painterResource(id = R.drawable.user_placeholder),
                placeholder = painterResource(id = R.drawable.user_placeholder)
            ),
            contentDescription = "profile_image",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun SNSEditProfileImage(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    onClick: () -> Unit = {}
) {
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .crossfade(true)
                .build(),
            contentDescription = "profile_image",
            modifier = modifier
                .clip(CircleShape)
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.2f)
                    .align(Alignment.BottomCenter)
                    .background(color = Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "edit",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SNSProfileImagePreview() {
    SNSTheme {
        Surface {
            Row {
                SNSProfileImage(
                    modifier = Modifier.size(150.dp),
                    imageUrl = null
                )
                SNSEditProfileImage(
                    modifier = Modifier.size(150.dp),
                    imageUrl = null
                )
            }
        }
    }
}