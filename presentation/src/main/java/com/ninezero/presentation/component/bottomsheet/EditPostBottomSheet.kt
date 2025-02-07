package com.ninezero.presentation.component.bottomsheet

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ninezero.presentation.component.SNSIconButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.model.PostCardModel
import kotlinx.coroutines.launch
import com.ninezero.presentation.R
import com.ninezero.presentation.component.LoadingDialog
import com.ninezero.presentation.component.PostToolbar
import com.ninezero.presentation.component.SNSImagePager
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun EditPostBottomSheet(
    showBottomSheet: Boolean,
    post: PostCardModel,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, List<String>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val richTextState = remember { RichTextState().apply { setHtml(post.richTextState.toHtml()) } }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0) }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .pointerInput(Unit) { detectDragGestures { _, _ -> } }
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = R.string.edit_post),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        navigationIcon = {
                            SNSIconButton(
                                onClick = {
                                    scope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                },
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "close",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        },
                        actions = {
                            SNSIconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onSave(richTextState.toHtml(), post.images)
                                },
                                drawableId = R.drawable.ic_check,
                                contentDescription = "save",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        windowInsets = WindowInsets(0)
                    )

                    SNSSurface(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
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
                                images = post.images
                            )

                            BasicRichTextEditor(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(3f),
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

                    SNSSurface(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 8.dp
                    ) {
                        PostToolbar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding(),
                            richTextState = richTextState
                        )
                    }
                }

                LoadingDialog(
                    isLoading = isEditing,
                    onDismissRequest = {}
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditPostBottomSheetPreview() {
    SNSTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_post),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    SNSIconButton(
                        onClick = { },
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "close",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    SNSIconButton(
                        onClick = { },
                        drawableId = R.drawable.ic_check,
                        contentDescription = "save",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            SNSSurface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
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

            SNSSurface(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp
            ) {
                PostToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    richTextState = RichTextState()
                )
            }
        }
    }
}