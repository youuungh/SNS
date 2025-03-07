package com.ninezero.presentation.component.bottomsheet

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.presentation.component.DefaultButton
import com.ninezero.presentation.component.SNSSurface
import kotlinx.coroutines.launch
import com.ninezero.presentation.R
import com.ninezero.presentation.component.DefaultAccentButton
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun ActionBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
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
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0) }
        ) {
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
                    content()
                }
            }
        }
    }
}

@Composable
fun LeaveRoomBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onLeave: () -> Unit
) {
    ActionBottomSheet(
        showBottomSheet = showBottomSheet,
        onDismiss = onDismiss
    ) {
        DefaultButton(
            text = stringResource(R.string.leave_room),
            onClick = onLeave,
            icon = R.drawable.ic_leave_room
        )
    }
}

@Composable
fun DeleteNotificationBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    ActionBottomSheet(
        showBottomSheet = showBottomSheet,
        onDismiss = onDismiss
    ) {
        DefaultAccentButton(
            text = stringResource(R.string.delete_notification),
            onClick = onDelete,
            icon = R.drawable.ic_delete,
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActionBottomSheetPreview() {
    SNSTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        text = stringResource(R.string.leave_room),
                        onClick = { },
                        icon = R.drawable.ic_leave_room
                    )
                    DefaultAccentButton(
                        text = stringResource(R.string.delete_notification),
                        onClick = { },
                        icon = R.drawable.ic_delete,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}