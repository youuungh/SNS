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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToPost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = remember(navBackStackEntry) {
        navBackStackEntry?.destination?.route ?: MainRoute.BottomNavItem.Feed.route
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

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

    val title = when (currentRoute) {
        MainRoute.BottomNavItem.Feed.route -> stringResource(R.string.feed)
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
        title = title,
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
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(MainRoute.BottomNavItem.Feed.route) {
                FeedScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToLogin = onNavigateToLogin
                )
            }
            composable(MainRoute.BottomNavItem.Profile.route) {
                ProfileScreen(
                    snackbarHostState = snackbarHostState,
                    onProfileImageChange = viewModel::load
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
    private val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
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

        when {
            isPermissionGranted() -> onPermissionGranted()
            !shouldShowRationale() -> {
                if (isFirstTimeRequest()) {
                    permissionLauncher.launch(arrayOf(requiredPermission))
                } else {
                    scope.launch { showSettingsSnackbar() }
                }
            }
            else -> permissionLauncher.launch(arrayOf(requiredPermission))
        }
    }

    fun handlePermissionResult(permissionsResult: Map<String, Boolean>) {
        val isGranted = permissionsResult[requiredPermission] == true
        if (isGranted) {
            onPermissionGranted()
        } else {
            val isPermanentlyDenied = !activity?.shouldShowRequestPermissionRationale(requiredPermission)!!
            if (isPermanentlyDenied) {
                scope.launch { showSettingsSnackbar() }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("이미지를 불러오기 위해 권한이 필요합니다")
                }
            }
        }
    }

    private fun isPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, requiredPermission) ==
                PackageManager.PERMISSION_GRANTED

    private fun shouldShowRationale(): Boolean =
        activity?.shouldShowRequestPermissionRationale(requiredPermission) == true

    private fun isFirstTimeRequest(): Boolean =
        ContextCompat.checkSelfPermission(context, requiredPermission) ==
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