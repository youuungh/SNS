package com.ninezero.presentation.auth.signup

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.R
import com.ninezero.presentation.component.SNSButton
import com.ninezero.presentation.component.DetailScaffold
import com.ninezero.presentation.component.SNSTextField
import com.ninezero.presentation.theme.SNSTheme
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToBack: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SignUpSideEffect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(sideEffect.message) }
            SignUpSideEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    DetailScaffold(
        titleRes = R.string.sign_up,
        showBackButton = true,
        onBackClick = {
            focusManager.clearFocus()
            keyboardController?.hide()
            onNavigateToBack()
        },
        snackbarHostState = snackbarHostState,
        isLoading = state.isLoading,
        modifier = Modifier.fillMaxSize()
    ) {
        SignUpContent(
            id = state.id,
            username = state.username,
            password = state.password,
            passwordConfirm = state.passwordConfirm,
            isPasswordConfirmError = state.isPasswordConfirmError,
            isSignUpEnabled = state.isSignUpEnabled,
            onIdChange = viewModel::onIdChange,
            onUsernameChange = viewModel::onUsernameChange,
            onPasswordChange = viewModel::onPasswordChange,
            onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
            onSignUpClick = viewModel::onSignUpClick
        )
    }
}

@Composable
private fun SignUpContent(
    id: String,
    username: String,
    password: String,
    passwordConfirm: String,
    isPasswordConfirmError: Boolean,
    isSignUpEnabled: Boolean,
    onIdChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    SNSSurface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                },
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SNSTextField(
                    value = id,
                    onValueChange = onIdChange,
                    label = stringResource(R.string.id),
                    placeholder = stringResource(R.string.label_input_id)
                )

                SNSTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = stringResource(R.string.username),
                    placeholder = stringResource(R.string.label_input_username)
                )

                SNSTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.password),
                    placeholder = stringResource(R.string.label_input_password),
                    isPassword = true
                )

                SNSTextField(
                    value = passwordConfirm,
                    onValueChange = onPasswordConfirmChange,
                    label = stringResource(R.string.password_confirm),
                    placeholder = stringResource(R.string.label_input_password_confirm),
                    isPassword = true,
                    isError = isPasswordConfirmError,
                    errorMessage = if (isPasswordConfirmError) stringResource(R.string.error_password_mismatch) else null
                )
            }

            SNSButton(
                text = stringResource(R.string.sign_up),
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSignUpClick()
                },
                enabled = isSignUpEnabled,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignUpScreenPreview() {
    SNSTheme {
        DetailScaffold(
            titleRes = R.string.sign_up,
            showBackButton = true,
            onBackClick = {},
            snackbarHostState = remember { SnackbarHostState() },
            modifier = Modifier.fillMaxSize()
        ) {
            SignUpContent(
                id = "",
                username = "",
                password = "",
                passwordConfirm = "",
                isPasswordConfirmError = false,
                isSignUpEnabled = false,
                onIdChange = {},
                onUsernameChange = {},
                onPasswordChange = {},
                onPasswordConfirmChange = {},
                onSignUpClick = {}
            )
        }
    }
}