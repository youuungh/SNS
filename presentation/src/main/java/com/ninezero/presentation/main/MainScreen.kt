package com.ninezero.presentation.main

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ninezero.presentation.component.MainScaffold
import com.ninezero.presentation.component.SNSBottomBar
import com.ninezero.presentation.feed.FeedScreen
import com.ninezero.presentation.profile.ProfileScreen
import com.ninezero.presentation.theme.SNSTheme
import com.ninezero.presentation.R
import com.ninezero.presentation.component.SNSIconButton
import com.ninezero.presentation.message.MessageScreen
import com.ninezero.presentation.search.ExploreScreen
import com.ninezero.presentation.search.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToPost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    MainNavHost(
        viewModel = viewModel,
        onNavigateToPost = onNavigateToPost,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun MainContent(
    viewModel: MainViewModel,
    onNavigateToPost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToUser: (Long) -> Unit,
    onNavigateToChat: (otherUserId: Long, roomId: String?, otherUserLoginId: String, otherUserName: String, otherUserProfilePath: String?, myUserId: Long) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = remember(navBackStackEntry) {
        navBackStackEntry?.destination?.route ?: MainRoute.BottomNavItem.Feed.route
    }

    val searchViewModel: SearchViewModel = hiltViewModel()
    val isSearchMode by searchViewModel.isSearchMode.collectAsState()
    val searchState by searchViewModel.collectAsState()

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val currentScreenFocusRequester = if (currentRoute == MainRoute.BottomNavItem.Search.route) {
        focusRequester
    } else {
        remember { FocusRequester() }
    }

    val permissionHandler = remember(activity, context, scope, snackbarHostState, onNavigateToPost) {
        PermissionHandler(
            context = context,
            activity = activity,
            scope = scope,
            snackbarHostState = snackbarHostState,
            onPermissionGranted = onNavigateToPost
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = permissionHandler::handlePermissionResult
    )

    LaunchedEffect(permissionLauncher) {
        permissionHandler.setPermissionLauncher(permissionLauncher)
    }

    LaunchedEffect(isSearchMode, currentRoute) {
        if (isSearchMode && currentRoute == MainRoute.BottomNavItem.Search.route) {
            delay(100)
            currentScreenFocusRequester.requestFocus()
        }
    }

    BackHandler(enabled = isSearchMode) {
        keyboardController?.hide()
        focusManager.clearFocus()
        searchViewModel.clearSearch()
        searchViewModel.setSearchMode(false)
    }

    val title = when (currentRoute) {
        MainRoute.BottomNavItem.Feed.route -> stringResource(R.string.feed)
        MainRoute.BottomNavItem.Message.route -> stringResource(R.string.message)
        MainRoute.BottomNavItem.Profile.route -> stringResource(R.string.profile)
        else -> ""
    }

    val navigationCallback = remember(navController) {
        { route: String ->
            when (route) {
                MainRoute.BottomNavItem.Post.route ->
                    permissionHandler.checkAndRequestPermissions(permissionLauncher)
                else -> navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
    }

    MainScaffold(
        title = if (currentRoute != MainRoute.BottomNavItem.Search.route) title else null,
        isSearchRoute = currentRoute == MainRoute.BottomNavItem.Search.route,
        isSearchMode = isSearchMode,
        searchQuery = searchState.searchQuery,
        onSearchQueryChange = searchViewModel::onSearchQueryChange,
        onSearchFocus = { searchViewModel.setSearchMode(true) },
        onBackClick = {
            keyboardController?.hide()
            focusManager.clearFocus()
            searchViewModel.clearSearch()
            searchViewModel.setSearchMode(false)
        },
        focusRequester = currentScreenFocusRequester,
        snackbarHostState = snackbarHostState,
        actions = {
            if (currentRoute == MainRoute.BottomNavItem.Profile.route) {
                SNSIconButton(
                    onClick = onNavigateToSettings,
                    drawableId = R.drawable.ic_settings,
                    contentDescription = "settings",
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        },
        bottomBar = {
            SNSBottomBar(
                modifier = Modifier.fillMaxWidth(),
                currentRoute = currentRoute,
                imageUrl = uiState.profileImageUrl,
                navigateToRoute = navigationCallback
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = MainRoute.BottomNavItem.Feed.route,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(it),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(MainRoute.BottomNavItem.Feed.route) {
                FeedScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToProfile = { navController.navigate(MainRoute.BottomNavItem.Profile.route) },
                    onNavigateToUser = onNavigateToUser
                )
            }

            composable(MainRoute.BottomNavItem.Search.route) {
                ExploreScreen(
                    snackbarHostState = snackbarHostState,
                    searchViewModel = searchViewModel,
                    onNavigateToUser = onNavigateToUser
                )
            }

            composable(MainRoute.BottomNavItem.Message.route) {
                MessageScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToChat = onNavigateToChat
                )
            }

            composable(MainRoute.BottomNavItem.Profile.route) {
                ProfileScreen(
                    snackbarHostState = snackbarHostState,
                    onProfileImageChange = viewModel::load,
                    onNavigateToUser = onNavigateToUser
                )
            }
        }
    }
}

private class PermissionHandler(
    private val context: Context,
    private val activity: ComponentActivity?,
    private val scope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState,
    private val onPermissionGranted: () -> Unit
) {
    private val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.POST_NOTIFICATIONS
    } else null

    private val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private lateinit var permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    fun setPermissionLauncher(launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>) {
        permissionLauncher = launcher
    }

    private fun navigateToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private suspend fun showSettingsSnackbar() {
        snackbarHostState.showSnackbar(
            message = "설정에서 권한을 허용해주세요",
            actionLabel = "설정하기",
            withDismissAction = true,
            duration = SnackbarDuration.Short
        ).also { if (it == SnackbarResult.ActionPerformed) navigateToSettings() }
    }

    fun checkAndRequestPermissions(permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>) {
        if (activity == null) return

        val permissionsToRequest = mutableListOf<String>()

        // 알림 권한 체크
        if (notificationPermission != null && !isPermissionGranted(notificationPermission)) {
            permissionsToRequest.add(notificationPermission)
        }

        // 이미지 권한 체크
        if (!isPermissionGranted(imagePermission)) {
            permissionsToRequest.add(imagePermission)
        }

        when {
            permissionsToRequest.isEmpty() -> onPermissionGranted()
            !shouldShowRationale(imagePermission) -> {
                if (isFirstTimeRequest(imagePermission)) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                } else {
                    scope.launch { showSettingsSnackbar() }
                }
            }
            else -> permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    fun handlePermissionResult(permissionsResult: Map<String, Boolean>) {
        val allGranted = permissionsResult.all { it.value }

        if (allGranted) {
            onPermissionGranted()
        } else {
            // 어떤 권한이라도 영구적으로 거부되었는지 확인
            val anyPermanentlyDenied = permissionsResult.any { (permission, granted) ->
                !granted && !activity?.shouldShowRequestPermissionRationale(permission)!!
            }

            if (anyPermanentlyDenied) {
                scope.launch { showSettingsSnackbar() }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("앱 사용을 위해 모든 권한이 필요합니다")
                }
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED

    private fun shouldShowRationale(permission: String): Boolean =
        activity?.shouldShowRequestPermissionRationale(permission) == true

    private fun isFirstTimeRequest(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_DENIED
}

fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    SNSTheme {
        MainScreen(
            onNavigateToPost = {},
            onNavigateToSettings = {},
            onNavigateToLogin = {}
        )
    }
}