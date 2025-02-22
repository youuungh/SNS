package com.ninezero.presentation.main

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ninezero.presentation.chat.ChatScreen
import com.ninezero.presentation.user.UserScreen

private const val ANIM_DURATION = 300

@Composable
fun MainNavHost(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToPost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoute.Main.route,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        composable(
            route = MainRoute.Main.route,
            enterTransition = {
                when (initialState.destination.route) {
                    "${MainRoute.User.route}/{userId}" -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIM_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIM_DURATION))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "${MainRoute.User.route}/{userId}",
                    "${MainRoute.Chat.route}/{otherUserId}/{roomId}/{otherUserLoginId}/{otherUserName}/{otherUserProfilePath}/{myUserId}" -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIM_DURATION)
                    ) + fadeOut(animationSpec = tween(ANIM_DURATION))
                    else -> null
                }
            }
        ) {
            MainContent(
                viewModel = viewModel,
                onNavigateToPost = onNavigateToPost,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToUser = { userId ->
                    navController.navigate("${MainRoute.User.route}/$userId")
                },
                onNavigateToChat = { otherUserId, roomId, otherUserLoginId, otherUserName, otherUserProfilePath, myUserId ->
                    navController.navigate(
                        MainRoute.Chat.navigate(
                            otherUserId = otherUserId,
                            roomId = roomId,
                            otherUserLoginId = otherUserLoginId,
                            otherUserName = otherUserName,
                            otherUserProfilePath = otherUserProfilePath,
                            myUserId = myUserId
                        )
                    )
                }
            )
        }

        composable(
            route = "${MainRoute.User.route}/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(animationSpec = tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(ANIM_DURATION))
            }
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable

            UserScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { otherUserId, roomId, otherUserLoginId, otherUserName, otherUserProfilePath, myUserId ->
                    navController.navigate(
                        MainRoute.Chat.navigate(
                            otherUserId = otherUserId,
                            roomId = roomId,
                            otherUserLoginId = otherUserLoginId,
                            otherUserName = otherUserName,
                            otherUserProfilePath = otherUserProfilePath,
                            myUserId = myUserId
                        )
                    )
                }
            )
        }

        composable(
            route = "${MainRoute.Chat.route}/{otherUserId}/{roomId}/{otherUserLoginId}/{otherUserName}/{otherUserProfilePath}/{myUserId}",
            arguments = listOf(
                navArgument("otherUserId") { type = NavType.LongType },
                navArgument("roomId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("otherUserLoginId") { type = NavType.StringType },
                navArgument("otherUserName") { type = NavType.StringType },
                navArgument("otherUserProfilePath") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("myUserId") { type = NavType.LongType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeIn(animationSpec = tween(ANIM_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(ANIM_DURATION)
                ) + fadeOut(animationSpec = tween(ANIM_DURATION))
            }
        ) { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getLong("otherUserId") ?: return@composable
            val roomId = backStackEntry.arguments?.getString("roomId")
            val otherUserLoginId = backStackEntry.arguments?.getString("otherUserLoginId") ?: return@composable
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: return@composable
            val otherUserProfilePath = backStackEntry.arguments?.getString("otherUserProfilePath")?.let {
                Uri.decode(it).takeIf { it.isNotEmpty() }
            }
            val myUserId = backStackEntry.arguments?.getLong("myUserId") ?: return@composable

            ChatScreen(
                otherUserId = otherUserId,
                myUserId = myUserId,
                roomId = roomId,
                otherUserLoginId = otherUserLoginId,
                otherUserName = otherUserName,
                otherUserProfilePath = otherUserProfilePath,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}