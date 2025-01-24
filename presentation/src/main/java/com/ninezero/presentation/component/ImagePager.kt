package com.ninezero.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun SNSImagePager(
    modifier: Modifier = Modifier,
    images: List<String>
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (images.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { images.size })

            HorizontalPager(
                state = pagerState,
            ) { index ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = rememberAsyncImagePainter(model = images[index]),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .blur(
                            radiusX = 5.dp,
                            radiusY = 5.dp
                        )
                )

                Text(
                    text = "${pagerState.currentPage + 1}/${images.size}",
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun SNSPostPager(
    modifier: Modifier = Modifier,
    images: List<String>,
    pagerState: PagerState
) {
    Box(modifier = modifier) {
        if (images.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
            ) { index ->
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = rememberAsyncImagePainter(model = images[index]),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            if (images.size > 1) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${images.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Preview(showBackground = false, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SNSImagePagerPreview() {
    SNSTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            SNSImagePager(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                images = listOf(
                    "https://developer.android.com/images/brand/Android_Robot.png",
                    "https://developer.android.com/images/brand/Android_Robot_100.png",
                    "https://developer.android.com/images/brand/Android_Robot_200.png"
                )
            )
        }
    }
}