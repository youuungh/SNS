package com.ninezero.presentation.setting

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    DetailScaffold(
        title = stringResource(R.string.settings),
        showBackButton = true,
        onBackClick = onNavigateToBack
    ) {
        SNSSurface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AdditionalButton(
                    text = stringResource(R.string.dark_mode),
                    isEnabled = isDarkTheme,
                    onClick = { viewModel.updateTheme(isDark = !isDarkTheme) }
                )
            }
        }
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AdditionalButton(
                        text = "어두운 모드",
                        isEnabled = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}