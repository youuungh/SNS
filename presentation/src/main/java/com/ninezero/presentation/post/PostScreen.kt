package com.ninezero.presentation.post

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.ninezero.presentation.component.DetailScaffold
import org.orbitmvi.orbit.compose.collectAsState
import com.ninezero.presentation.R
import com.ninezero.presentation.component.PostToolbar
import com.ninezero.presentation.component.SNSImagePager
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun PostScreen(
    viewModel: PostViewModel,
    onNavigateToBack: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val onFocusChange = remember(viewModel) {
        { focusState: FocusState ->
            viewModel.onKeyboardVisibilityChanged(focusState.isFocused)
            Unit
        }
    }

    DetailScaffold(
        title = "",
        showBackButton = true,
        onBackClick = onNavigateToBack,
        actions = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    viewModel.onPostClick()
                },
                enabled = state.selectedImages.isNotEmpty()
            ) {
                Text(
                    text = stringResource(R.string.posting),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            if (state.isKeyboardVisible) {
                PostToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    richTextState = state.richTextState
                )
            }
        },
        isLoading = state.isLoading,
        modifier = Modifier.fillMaxSize()
    ) {
        PostContent(
            richTextState = state.richTextState,
            images = state.selectedImages.map { it.uri },
            onFocusChange = onFocusChange,
            focusManager = focusManager,
            keyboardController = keyboardController
        )
    }
}

@Composable
private fun PostContent(
    richTextState: RichTextState,
    images: List<String>,
    onFocusChange: (FocusState) -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) {
    SNSSurface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SNSImagePager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                images = images
            )

            BasicRichTextEditor(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .onFocusEvent { onFocusChange(it) },
                state = richTextState,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (richTextState.annotatedString.isEmpty()) {
                        Text(
                            text = stringResource(R.string.label_hint_add_text),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PostScreenPreview() {
    SNSTheme {
        DetailScaffold(
            title = "",
            showBackButton = true,
            onBackClick = {},
            actions = {
                TextButton(
                    onClick = {},
                    enabled = true
                ) {
                    Text(
                        text = "게시",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            PostContent(
                richTextState = RichTextState(),
                images = emptyList(),
                onFocusChange = {},
                focusManager = LocalFocusManager.current,
                keyboardController = null
            )
        }
    }
}