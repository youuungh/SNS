package com.ninezero.presentation.post

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ninezero.presentation.component.DetailScaffold
import org.orbitmvi.orbit.compose.collectAsState
import com.ninezero.presentation.R
import com.ninezero.presentation.component.SNSIconToggleButton
import com.ninezero.presentation.component.SNSSmallText
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.SNSTextButton
import com.ninezero.presentation.component.bounceClick
import com.ninezero.presentation.util.Constants.CELL_SIZE
import com.ninezero.presentation.util.Constants.GRID_SPACING

@Composable
fun PostImageScreen(
    viewModel: PostViewModel,
    onNavigateToBack: () -> Unit,
    onNavigateToNext: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    DetailScaffold(
        titleRes = R.string.new_post,
        showBackButton = true,
        onBackClick = onNavigateToBack,
        actions = {
            SNSTextButton(
                text = stringResource(R.string.next),
                onClick = onNavigateToNext,
                enabled = state.selectedImages.isNotEmpty()
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        SNSSurface {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberLazyListState()
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.selectedImages.isNotEmpty()) {
                                androidx.compose.foundation.Image(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = rememberAsyncImagePainter(model = state.selectedImages.lastOrNull()?.uri),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.label_no_selected_image),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    stickyHeader {
                        SNSSurface(elevation = 2.dp) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SNSSmallText(text = if (state.isMultiSelectMode) {
                                        stringResource(R.string.multi_select)
                                    } else {
                                        stringResource(R.string.single_select)
                                    })

                                    SNSIconToggleButton(
                                        onClick = { viewModel.toggleMultiSelectMode() },
                                        icon = painterResource(id = R.drawable.ic_multiple),
                                        isActive = state.isMultiSelectMode
                                    )
                                }
                            }
                        }
                    }

                    val columns = (screenWidth.value / (CELL_SIZE + GRID_SPACING)).toInt().coerceAtLeast(2)
                    val itemRows = state.images.chunked(columns)

                    itemRows.forEach { rowItems ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = GRID_SPACING.dp / 2),
                                horizontalArrangement = Arrangement.spacedBy(GRID_SPACING.dp)
                            ) {
                                rowItems.forEach { image ->
                                    val isSelected = state.selectedImages.contains(image)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .bounceClick {
                                                if (state.isMultiSelectMode) {
                                                    viewModel.onMultiImageSelect(image)
                                                } else {
                                                    viewModel.onSingleImageSelect(image)
                                                }
                                            }
                                    ) {
                                        androidx.compose.foundation.Image(
                                            modifier = Modifier.fillMaxSize(),
                                            painter = rememberAsyncImagePainter(model = image.uri),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop
                                        )

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(Color.Black.copy(alpha = 0.2f))
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp)
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                    .padding(1.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                repeat(columns - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}