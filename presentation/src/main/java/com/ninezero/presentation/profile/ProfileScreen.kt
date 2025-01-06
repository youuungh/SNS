package com.ninezero.presentation.profile

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninezero.presentation.component.EditUsernameDialog
import com.ninezero.presentation.component.SNSDialog
import com.ninezero.presentation.component.SNSOutlinedButton
import com.ninezero.presentation.component.SNSEditProfileImage
import com.ninezero.presentation.component.SNSSmallButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.ninezero.presentation.R
import com.ninezero.presentation.component.LoadingDialog
import com.ninezero.presentation.component.NetworkErrorScreen

@Composable
fun ProfileScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onProfileImageChange: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<ProfileDialog>(ProfileDialog.Hidden) }

    val visualMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.onImageChange(it) {
                    onProfileImageChange()
                }
            }
        }
    )

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(sideEffect.message) }
            is ProfileSideEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    when {
        state.hasError -> NetworkErrorScreen(onRetry = { viewModel.refresh() })
        else -> {
            ProfileContent(
                username = state.username,
                profileImageUrl = state.profileImageUrl,
                onImageChangeClick = {
                    visualMediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onUsernameChangeClick = { dialog = ProfileDialog.EditUsername(state.username) },
                onSignOutClick = { dialog = ProfileDialog.SignOut }
            )
        }
    }

    LoadingDialog(
        isLoading = state.isLoading,
        onDismissRequest = {}
    )

    ProfileDialogs(
        dialog = dialog,
        onDismiss = { dialog = ProfileDialog.Hidden },
        onConfirmUsername = viewModel::onUsernameChange,
        onConfirmSignOut = viewModel::onSignOut
    )
}

@Composable
private fun ProfileContent(
    username: String = "",
    profileImageUrl: String?,
    onImageChangeClick: () -> Unit,
    onUsernameChangeClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    SNSSurface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SNSEditProfileImage(
                    modifier = Modifier.size(100.dp),
                    imageUrl = profileImageUrl,
                    onClick = onImageChangeClick
                )

                Text(
                    text = username.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                SNSSmallButton(
                    text = stringResource(R.string.edit_username),
                    onClick = onUsernameChangeClick
                )
            }

            SNSOutlinedButton(
                text = stringResource(R.string.sign_out),
                onClick = onSignOutClick
            )
        }
    }
}

@Composable
private fun ProfileDialogs(
    dialog: ProfileDialog,
    onDismiss: () -> Unit,
    onConfirmUsername: (String) -> Unit,
    onConfirmSignOut: () -> Unit,
) {
    when (dialog) {
        is ProfileDialog.EditUsername -> {
            var username by remember(dialog) { mutableStateOf(dialog.initialUsername) }
            EditUsernameDialog(
                openDialog = true,
                currentUsername = username,
                onUsernameChange = { username = it },
                onDismiss = onDismiss,
                onConfirm = {
                    onConfirmUsername(username)
                    onDismiss()
                }
            )
        }
        ProfileDialog.SignOut -> {
            SNSDialog(
                openDialog = true,
                onDismiss = onDismiss,
                onConfirm = {
                    onConfirmSignOut()
                    onDismiss()
                }
            )
        }
        ProfileDialog.Hidden -> Unit
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfileScreenPreview() {
    SNSTheme {
        ProfileContent(
            username = "Username",
            profileImageUrl = null,
            onImageChangeClick = {},
            onUsernameChangeClick = {},
            onSignOutClick = {}
        )
    }
}