package com.ninezero.presentation.component

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
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

    var hasBottomBar by remember { mutableStateOf(false) }

    val wrappedBottomBar: @Composable () -> Unit = {
        Box(
            modifier = Modifier.onGloballyPositioned { hasBottomBar = true }
        ) {
            bottomBar()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            displayTitle?.let {
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
        bottomBar = wrappedBottomBar,
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(
                            if (hasBottomBar) {
                                Modifier.padding(bottom = 64.dp)
                            } else {
                                Modifier
                            }
                        )
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
fun LeftAlignedDetailScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    @StringRes titleRes: Int? = null,
    subtitle: String? = null,
    profileImageUrl: String? = null,
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
            displayTitle?.let {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            profileImageUrl?.let {
                                SNSProfileImage(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(36.dp),
                                    imageUrl = profileImageUrl
                                )
                            }

                            Column {
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                subtitle?.let {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

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
        snackbarHost = {
            snackbarHostState?.let { state ->
                SNSSnackbar(
                    snackbarHostState = state,
                    modifier = Modifier
                )
            }
        },
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
    isSearchRoute: Boolean = false,
    isSearchMode: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocus: () -> Unit = {},
    onBackClick: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    focusRequester: FocusRequester,
    snackbarHostState: SnackbarHostState? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
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
            if (displayTitle != null || isSearchRoute) {
                TopAppBar(
                    title = {
                        if (isSearchRoute) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AnimatedVisibility(
                                            visible = isSearchMode,
                                            enter = fadeIn(animationSpec = tween(150)) + expandHorizontally(animationSpec = tween(150)),
                                            exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(animationSpec = tween(150))
                                        ) {
                                            IconButton(
                                                onClick = onBackClick,
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Back"
                                                )
                                            }
                                        }

                                        SearchTextField(
                                            value = searchQuery,
                                            onValueChange = onSearchQueryChange,
                                            onFocus = onSearchFocus,
                                            placeholder = "검색",
                                            focusRequester = focusRequester,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 16.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = displayTitle ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )
                        }
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
        ) {
            content(paddingValues)

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
private fun LeftAlignedDetailScaffoldPreview() {
    SNSTheme {
        LeftAlignedDetailScaffold(
            title = "Detail Title",
            subtitle = "Detail Subtitle",
            profileImageUrl = null,
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
            focusRequester = remember { FocusRequester() },
            content = {}
        )
    }
}