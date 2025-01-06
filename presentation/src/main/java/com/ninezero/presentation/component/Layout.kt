package com.ninezero.presentation.component

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ninezero.presentation.theme.LocalTheme
import com.ninezero.presentation.theme.snsProgressDark
import com.ninezero.presentation.theme.snsProgressDarkBackground
import com.ninezero.presentation.theme.snsProgressLight
import com.ninezero.presentation.theme.snsProgressLightBackground

@Composable
fun PullToRefreshLayout(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshThreshold: Dp = 56.dp,
    refreshingOffset: Dp = 56.dp,
    content: @Composable () -> Unit
) {
    val isDarkTheme = LocalTheme.current

    val backgroundColor = if (isDarkTheme) {
        snsProgressDarkBackground
    } else {
        snsProgressLightBackground
    }

    val progressColor = if (isDarkTheme) {
        snsProgressDark
    } else {
        snsProgressLight
    }

    val refreshThresholdPx = with(LocalDensity.current) { refreshThreshold.toPx() }
    val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }

    var offsetY by remember { mutableFloatStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshing) {
        isRefreshing = refreshing
        if (refreshing) {
            animate(
                initialValue = offsetY,
                targetValue = refreshingOffsetPx,
            ) { value, _ -> offsetY = value }
        } else {
            animate(
                initialValue = offsetY,
                targetValue = 0f,
            ) { value, _ -> offsetY = value }
        }
    }

    val progress = (offsetY / refreshThresholdPx).coerceIn(0f, 1f)
    val showProgress = offsetY > 0f || isRefreshing
    val progressAlpha by animateFloatAsState(
        targetValue = if (showProgress) 1f else 0f,
        label = "progress_alpha"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                if (available.y < 0 && offsetY > 0f) {
                    val newOffset = (offsetY + available.y).coerceAtLeast(0f)
                    val consumed = newOffset - offsetY
                    offsetY = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (isRefreshing) return Offset.Zero

                return if (consumed.y == 0f && available.y > 0f) {
                    val newOffset = (offsetY + available.y * 0.5f)
                    if (newOffset > refreshThresholdPx && !isRefreshing) {
                        isRefreshing = true
                        offsetY = refreshingOffsetPx
                        onRefresh()
                    }
                    offsetY = newOffset
                    Offset(0f, available.y)
                } else Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                if (offsetY > 0 && !isRefreshing) {
                    animate(
                        initialValue = offsetY,
                        targetValue = 0f
                    ) { value, _ -> offsetY = value }
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(
        modifier = modifier.nestedScroll(nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .offset(y = with(LocalDensity.current) { offsetY.toDp() })
                .zIndex(1f)
        ) {
            content()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .alpha(progressAlpha)
                .zIndex(0f),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp,
                    color = progressColor,
                    trackColor = backgroundColor,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp,
                    color = progressColor,
                    trackColor = backgroundColor,
                    progress = { progress }
                )
            }
        }
    }
}

@Composable
fun SettingLayout(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                content()
            }
        }
    }
}