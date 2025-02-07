package com.ninezero.presentation.post

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ninezero.presentation.component.SNSSnackbar
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectSideEffect

private const val ANIM_DURATION = 600

@Composable
fun PostNavHost(
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    val sharedViewModel: PostViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    sharedViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PostSideEffect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(sideEffect.message) }
            PostSideEffect.Finish -> onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        NavHost(
            navController = navController,
            startDestination = PostRoute.PostImage.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(animationSpec = tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(ANIM_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(animationSpec = tween(ANIM_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(ANIM_DURATION))
            }
        ) {
            composable(route = PostRoute.PostImage.route) {
                PostImageScreen(
                    viewModel = sharedViewModel,
                    onNavigateToBack = onFinish,
                    onNavigateToNext = { navController.navigate(PostRoute.Post.route) }
                )
            }

            composable(route = PostRoute.Post.route) {
                PostScreen(
                    viewModel = sharedViewModel,
                    onNavigateToBack = { navController.navigateUp() }
                )
            }
        }

        SNSSnackbar(
            snackbarHostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
        )
    }
}