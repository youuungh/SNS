package com.ninezero.presentation.post

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ninezero.domain.model.Image
import com.ninezero.presentation.component.DetailScaffold
import org.orbitmvi.orbit.compose.collectAsState
import com.ninezero.presentation.R
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.SNSTextButton
import com.ninezero.presentation.component.bounceClick
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun PostImageScreen(
    viewModel: PostViewModel,
    onNavigateToBack: () -> Unit,
    onNavigateToNext: () -> Unit
) {
    val state = viewModel.collectAsState().value

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
        PostImageContent(
            selectedImages = state.selectedImages,
            images = state.images,
            onItemClick = viewModel::onImageClick
        )
    }
}

@Composable
private fun PostImageContent(
    selectedImages: List<Image>,
    images: List<Image>,
    onItemClick: (Image) -> Unit
) {
    SNSSurface {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImages.isNotEmpty()) {
                    androidx.compose.foundation.Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = rememberAsyncImagePainter(model = selectedImages.lastOrNull()?.uri),
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

            LazyVerticalGrid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                columns = GridCells.Adaptive(110.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    count = images.size,
                    key = { images[it].uri }
                ) {
                    val image = images[it]
                    val isSelected = selectedImages.contains(image)

                    Box(
                        modifier = Modifier
                            .bounceClick {
                                if (!isSelected || selectedImages.size > 1) {
                                    onItemClick(image)
                                }
                            }
                    ) {
                        androidx.compose.foundation.Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
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
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PostImageScreenPreview() {
    SNSTheme {
        DetailScaffold(
            titleRes = R.string.new_post,
            showBackButton = true,
            onBackClick = {},
            actions = {
                SNSTextButton(
                    text = "다음",
                    onClick = {},
                    enabled = true
                )
            },
            snackbarHostState = remember { SnackbarHostState() },
            modifier = Modifier.fillMaxSize()
        ) {
            PostImageContent(
                selectedImages = emptyList(),
                images = emptyList(),
                onItemClick = {}
            )
        }
    }
}