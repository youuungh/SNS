package com.ninezero.presentation.auth.login

import android.content.res.Configuration
import com.ninezero.presentation.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.presentation.component.SNSButton
import com.ninezero.presentation.component.OnboardingLottieAnim
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit
) {
    SNSSurface {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            OnboardingLottieAnim(modifier = Modifier.weight(1f))

            SNSButton(
                text = stringResource(id = R.string.start),
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            )
        }
    }
}

@Preview()
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingScreenPreview() {
    SNSTheme {
        OnboardingScreen(onNavigateToLogin = {})
    }
}