package com.ninezero.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PullToRefreshLayout(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshThreshold: Dp = 56.dp,
    content: @Composable BoxScope.() -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val refreshThresholdPx = with(LocalDensity.current) { refreshThreshold.toPx() }
    val progress = (offsetY / refreshThresholdPx).coerceIn(0f, 1f)
    val scope = rememberCoroutineScope()
    var isRefreshTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(refreshing) {
        when {
            refreshing -> {
                // Refresh 시작시 threshold 위치로 애니메이션
                animate(
                    initialValue = offsetY,
                    targetValue = refreshThresholdPx,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) { value, _ -> offsetY = value }
            }
            !refreshing -> {
                // Refresh 완료시 원위치로 부드럽게 애니메이션
                delay(300)
                animate(
                    initialValue = offsetY,
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                ) { value, _ -> offsetY = value }
                isRefreshTriggered = false
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (refreshing) return Offset.Zero // 리프레시 중 스크롤 제한

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
                if (refreshing) return Offset.Zero // 리프레시 중 스크롤 제한

                return if (consumed.y == 0f && available.y > 0f) {
                    val maxOffset = refreshThresholdPx
                    val newOffset = (offsetY + available.y * 0.5f).coerceAtMost(maxOffset)

                    if (newOffset >= refreshThresholdPx && !isRefreshTriggered) {
                        isRefreshTriggered = true
                        scope.launch {
                            onRefresh()
                        }
                    } else {
                        offsetY = newOffset
                    }
                    Offset(0f, available.y)
                } else Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (offsetY > 0 && !refreshing && !isRefreshTriggered) {
                    animate(
                        initialValue = offsetY,
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) { value, _ -> offsetY = value }
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .zIndex(0f),
            contentAlignment = Alignment.TopCenter
        ) {
            if (refreshing || offsetY > 0f) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .nestedScroll(nestedScrollConnection)
                .zIndex(1f),
            content = content
        )
    }
}