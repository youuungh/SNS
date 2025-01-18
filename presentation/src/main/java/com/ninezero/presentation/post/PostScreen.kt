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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
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
import com.ninezero.presentation.component.SNSTextButton
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun PostScreen(
    viewModel: PostViewModel,
    onNavigateToBack: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    DetailScaffold(
        title = "",
        showBackButton = true,
        onBackClick = onNavigateToBack,
        actions = {
            SNSTextButton(
                text = stringResource(R.string.posting),
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    viewModel.onPostClick()
                },
                enabled = state.selectedImages.isNotEmpty()
            )
        },
        bottomBar = {
            PostToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                richTextState = state.richTextState
            )
        },
        isLoading = state.isLoading,
        modifier = Modifier.fillMaxSize()
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
                    images = state.selectedImages.map { it.uri }
                )

                BasicRichTextEditor(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f),
                    state = state.richTextState,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (state.richTextState.annotatedString.isEmpty()) {
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
                SNSTextButton(
                    text = "게시",
                    onClick = {},
                    enabled = true
                )
            },
            bottomBar = {
                PostToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    richTextState = RichTextState()
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            SNSSurface(
                modifier = Modifier
                    .fillMaxSize()
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
                        images = emptyList()
                    )

                    BasicRichTextEditor(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(3f),
                        state = RichTextState(),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (RichTextState().annotatedString.isEmpty()) {
                                Text(
                                    text = "내용을 입력하세요",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }
    }
}