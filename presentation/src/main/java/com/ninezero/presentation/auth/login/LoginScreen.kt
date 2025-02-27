package com.ninezero.presentation.auth.login

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninezero.presentation.R
import com.ninezero.presentation.component.SNSButton
import com.ninezero.presentation.component.DetailScaffold
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.SNSTextField
import com.ninezero.presentation.theme.SNSTheme
import com.ninezero.presentation.theme.snsDefault
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToSignUp: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    val state = viewModel.collectAsState().value

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LoginSideEffect.ShowSnackbar -> onShowSnackbar(sideEffect.message)
            LoginSideEffect.NavigateToFeed -> onNavigateToFeed()
        }
    }

    DetailScaffold(
        isLoading = state.isLoading,
        modifier = Modifier.fillMaxSize()
    ) {
        LoginContent(
            id = state.id,
            password = state.password,
            isPasswordError = state.isPasswordError,
            isLoginEnabled = state.isLoginEnabled,
            onIdChange = viewModel::onIdChange,
            onPasswordChange = viewModel::onPasswordChange,
            onNavigateToSignUp = onNavigateToSignUp,
            onLoginClick = viewModel::onLogin
        )
    }
}

@Composable
private fun LoginContent(
    id: String,
    password: String,
    isPasswordError: Boolean,
    isLoginEnabled: Boolean,
    onIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onLoginClick: () -> Unit,
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
                }
        ) {
            Text(
                text = stringResource(R.string.login),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 96.dp, bottom = 48.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SNSTextField(
                        value = id,
                        onValueChange = onIdChange,
                        label = stringResource(R.string.id),
                        placeholder = stringResource(R.string.label_input_id)
                    )

                    SNSTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = stringResource(R.string.password),
                        placeholder = stringResource(R.string.label_input_password),
                        isPassword = true,
                        isError = isPasswordError,
                        errorMessage = if (isPasswordError) stringResource(R.string.error_invalid_credentials) else null
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SNSButton(
                        text = stringResource(R.string.login),
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onLoginClick()
                        },
                        enabled = isLoginEnabled
                    )

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.label_no_account),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.outline
                            ),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.sign_up),
                            color = snsDefault,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onNavigateToSignUp()
                                }
                                .padding(4.dp)
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
fun LoginScreenPreview() {
    SNSTheme {
        LoginContent(
            id = "",
            password = "",
            isPasswordError = false,
            isLoginEnabled = false,
            onIdChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToSignUp = {}
        )
    }
}