package com.ninezero.presentation.component.bottomsheet

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.presentation.component.DefaultButton
import kotlinx.coroutines.launch
import com.ninezero.presentation.R
import com.ninezero.presentation.component.SNSIconButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun OptionsBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

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
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0) }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.option),
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    windowInsets = WindowInsets(0)
                )

                SNSSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DefaultButton(
                            text = stringResource(R.string.edit),
                            onClick = onEdit,
                            icon = R.drawable.ic_edit
                        )
                        DefaultButton(
                            text = stringResource(R.string.delete),
                            onClick = onDelete,
                            icon = R.drawable.ic_delete
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OptionsBottomSheetContentPreview() {
    SNSTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "옵션",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            SNSSurface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DefaultButton(
                        text = "수정하기",
                        onClick = { },
                        icon = R.drawable.ic_edit
                    )
                    DefaultButton(
                        text = "삭제하기",
                        onClick = { },
                        icon = R.drawable.ic_delete
                    )
                }
            }
        }
    }
}