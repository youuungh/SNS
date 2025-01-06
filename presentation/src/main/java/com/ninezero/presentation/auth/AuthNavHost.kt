package com.ninezero.presentation.auth

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.ninezero.presentation.auth.login.LoginScreen
import com.ninezero.presentation.auth.login.OnboardingScreen
import com.ninezero.presentation.auth.signup.SignUpScreen
import kotlinx.coroutines.launch

private const val ANIM_DURATION = 600

@Composable
fun AuthNavHost(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    val navController = rememberNavController()
    val startDestination = viewModel.checkInitRoute()
    val scope = rememberCoroutineScope()

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        navController = navController,
        startDestination = startDestination,
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
        composable(route = AuthRoute.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    scope.launch {
                        viewModel.completeOnboarding()
                        navController.navigate(route = AuthRoute.Login.route) {
                            popUpTo(AuthRoute.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = AuthRoute.Login.route) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(AuthRoute.SignUp.route) },
                onNavigateToFeed = onAuthSuccess
            )
        }

        composable(route = AuthRoute.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigate(
                        route = AuthRoute.Login.route,
                        navOptions = navOptions { popUpTo(AuthRoute.Onboarding.route) }
                    )
                },
                onNavigateToBack = { navController.popBackStack() }
            )
        }
    }
}