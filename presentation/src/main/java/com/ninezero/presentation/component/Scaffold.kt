package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.presentation.theme.SNSTheme

@Composable
fun DetailScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    @StringRes titleRes: Int? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    bottomBar: @Composable () -> Unit = {},
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val displayTitle = when {
        title != null -> title
        titleRes != null -> context.getString(titleRes)
        else -> null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (displayTitle != null) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    actions = {
                        Row(modifier = Modifier.padding(end = 8.dp)) {
                            actions()
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = bottomBar,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            content()

            snackbarHostState?.let {
                SNSSnackbar(
                    snackbarHostState = it,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            LoadingDialog(
                isLoading = isLoading,
                onDismissRequest = {}
            )
        }
    }
}

@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    @StringRes titleRes: Int? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    snackbarHostState: SnackbarHostState? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val displayTitle = when {
        title != null -> title
        titleRes != null -> context.getString(titleRes)
        else -> null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (displayTitle != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start
                        )
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = bottomBar,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            content()

            snackbarHostState?.let {
                SNSSnackbar(
                    snackbarHostState = it,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScaffoldWithActionsPreview() {
    SNSTheme {
        DetailScaffold(
            title = "Detail Title",
            showBackButton = true,
            onBackClick = {},
            actions = {
                TextButton(onClick = {}) {
                    Text(text = "Text")
                }
            },
            content = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScaffoldWithoutActionsPreview() {
    SNSTheme {
        DetailScaffold(
            title = "Detail Title",
            showBackButton = true,
            onBackClick = {},
            content = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScaffoldPreview() {
    SNSTheme {
        MainScaffold(
            title = "Main Title",
            content = {}
        )
    }
}