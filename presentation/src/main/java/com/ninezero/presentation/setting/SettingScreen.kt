package com.ninezero.presentation.setting

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninezero.presentation.component.DetailScaffold
import com.ninezero.presentation.R
import com.ninezero.presentation.component.AdditionalButton
import com.ninezero.presentation.component.DefaultAccentButton
import com.ninezero.presentation.component.LoadingDialog
import com.ninezero.presentation.component.SignOutDialog
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val followNotifications by viewModel.followNotifications.collectAsState()
    val likeNotifications by viewModel.likeNotifications.collectAsState()
    val commentNotifications by viewModel.commentNotifications.collectAsState()
    val replyNotifications by viewModel.replyNotifications.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SettingSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
            SettingSideEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    DetailScaffold(
        title = stringResource(R.string.settings),
        showBackButton = true,
        onBackClick = onNavigateToBack,
        snackbarHostState = snackbarHostState
    ) {
        SNSSurface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.theme_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )
                AdditionalButton(
                    text = stringResource(R.string.dark_mode),
                    isEnabled = isDarkTheme,
                    onClick = { viewModel.updateTheme(isDark = !isDarkTheme) }
                )
                Text(
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                AdditionalButton(
                    text = stringResource(R.string.enable_notifications),
                    isEnabled = notificationsEnabled,
                    onClick = { viewModel.toggleNotifications() }
                )
                AdditionalButton(
                    text = stringResource(R.string.follow_notifications),
                    isEnabled = followNotifications,
                    onClick = { viewModel.toggleFollowNotifications() },
                    enabled = notificationsEnabled
                )
                AdditionalButton(
                    text = stringResource(R.string.like_notifications),
                    isEnabled = likeNotifications,
                    onClick = { viewModel.toggleLikeNotifications() },
                    enabled = notificationsEnabled
                )
                AdditionalButton(
                    text = stringResource(R.string.comment_notifications),
                    isEnabled = commentNotifications,
                    onClick = { viewModel.toggleCommentNotifications() },
                    enabled = notificationsEnabled
                )
                AdditionalButton(
                    text = stringResource(R.string.reply_notifications),
                    isEnabled = replyNotifications,
                    onClick = { viewModel.toggleReplyNotifications() },
                    enabled = notificationsEnabled
                )
                Text(
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                DefaultAccentButton(
                    text = stringResource(R.string.sign_out),
                    icon = R.drawable.ic_sign_out,
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = { viewModel.showSignOutDialog() }
                )
            }
        }

        LoadingDialog(
            isLoading = state.isLoading,
            onDismissRequest = {}
        )
    }

    when (state.dialog) {
        is SettingDialog.SignOut -> {
            SignOutDialog(
                openDialog = true,
                onDismiss = viewModel::hideDialog,
                onConfirm = {
                    viewModel.onSignOut()
                    viewModel.hideDialog()
                }
            )
        }
        SettingDialog.Hidden -> Unit
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingScreenPreview() {
    SNSTheme {
        DetailScaffold(
            title = "설정",
            showBackButton = true,
            onBackClick = {}
        ) {
            SNSSurface {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "테마 설정",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    AdditionalButton(
                        text = "어두운 모드",
                        isEnabled = true,
                        onClick = {}
                    )

                    Text(
                        text = "알림",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 16.dp, bottom = 8.dp)
                    )

                    AdditionalButton(
                        text = "알림 활성화",
                        isEnabled = true,
                        onClick = {}
                    )

                    AdditionalButton(
                        text = "팔로우 알림",
                        isEnabled = true,
                        onClick = {}
                    )

                    AdditionalButton(
                        text = "좋아요 알림",
                        isEnabled = true,
                        onClick = {}
                    )

                    AdditionalButton(
                        text = "댓글 알림",
                        isEnabled = true,
                        onClick = {}
                    )

                    AdditionalButton(
                        text = "답글 알림",
                        isEnabled = true,
                        onClick = {}
                    )

                    Text(
                        text = "계정",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 16.dp, bottom = 8.dp)
                    )

                    DefaultAccentButton(
                        text = "로그아웃",
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = {}
                    )
                }
            }
        }
    }
}