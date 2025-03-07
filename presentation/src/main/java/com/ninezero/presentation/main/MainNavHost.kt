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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ninezero.presentation.chat.ChatScreen
import com.ninezero.presentation.detail.PostDetailScreen
import com.ninezero.presentation.notification.NotificationScreen
import com.ninezero.presentation.user.UserScreen
import timber.log.Timber

private const val ANIM_DURATION = 300

@Composable
fun MainNavHost(
    deepLink: Uri? = null,
    chatNotificationData: ChatNotificationData? = null,
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToPost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()

    LaunchedEffect(deepLink, chatNotificationData) {
        deepLink?.let { uri ->
            Timber.d("딥링크 처리: $uri")
            when {
                uri.toString().startsWith("ninezero://user/") -> {
                    val segments = uri.pathSegments
                    val userId = segments[0].toLongOrNull()
                    userId?.let {
                        navController.navigate("${MainRoute.User.route}/$userId")
                    } ?: Timber.e("유저 딥링크 오류: 유효하지 않은 userId")
                }
                uri.toString().startsWith("ninezero://post_detail/") -> {
                    val segments = uri.pathSegments
                    if (segments.size >= 2) {
                        val userId = segments[0].toLongOrNull()
                        val postId = segments[1].toLongOrNull()
                        val showComments = uri.getQueryParameter("showComments") == "true"
                        val commentId = uri.getQueryParameter("commentId")?.toLongOrNull()

                        if (userId != null && postId != null) {
                            navController.navigate(
                                MainRoute.PostDetail.navigate(
                                    userId = userId,
                                    postId = postId,
                                    showComments = showComments,
                                    commentId = commentId
                                )
                            )
                        } else {
                            Timber.e("게시물 딥링크 오류: 유효하지 않은 userId 또는 postId")
                        }
                    } else {
                        Timber.e("게시물 딥링크 오류: 세그먼트 부족 (${segments.size})")
                    }
                }
                else -> {
                    Timber.e("지원하지 않는 딥링크: $uri")
                }
            }
        }

        chatNotificationData?.let { data ->
            navController.navigate(
                MainRoute.Chat.navigate(
                    otherUserId = data.otherUserId,
                    roomId = data.roomId,
                    otherUserLoginId = data.otherUserLoginId,
                    otherUserName = data.otherUserName,
                    otherUserProfilePath = data.otherUserProfilePath,
                    myUserId = data.myUserId
                )
            )
        }
    }

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
                    "${MainRoute.User.route}/{userId}",
                    MainRoute.Notification.route,
                    "${MainRoute.Chat.route}/{otherUserId}/{roomId}/{otherUserLoginId}/{otherUserName}/{otherUserProfilePath}/{myUserId}" -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIM_DURATION)
                    ) + fadeIn(animationSpec = tween(ANIM_DURATION))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "${MainRoute.User.route}/{userId}",
                    MainRoute.Notification.route,
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
                onNavigateToNotification = {
                    navController.navigate(MainRoute.Notification.route)
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
                },
                onNavigateToPostDetail = { userId, postId ->
                    navController.navigate(
                        MainRoute.PostDetail.navigate(
                            userId = userId,
                            postId = postId
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
                onNavigateToPostDetail = { userId, postId ->
                    navController.navigate(
                        MainRoute.PostDetail.navigate(
                            userId = userId,
                            postId = postId
                        )
                    )
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
            route = MainRoute.Notification.route,
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
        ) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { userId ->
                    navController.navigate("${MainRoute.User.route}/$userId")
                },
                onNavigateToPost = { userId, postId, showComments ->
                    navController.navigate(
                        MainRoute.PostDetail.navigate(
                            userId = userId,
                            postId = postId,
                            showComments = showComments
                        )
                    )
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

        composable(
            route = "${MainRoute.PostDetail.route}?showComments={showComments}&commentId={commentId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("postId") { type = NavType.LongType },
                navArgument("showComments") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("commentId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
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
            val postId = backStackEntry.arguments?.getLong("postId") ?: return@composable
            val showComments = backStackEntry.arguments?.getBoolean("showComments") == true
            val commentIdStr = backStackEntry.arguments?.getString("commentId")
            val commentId = commentIdStr?.toLongOrNull()

            PostDetailScreen(
                userId = userId,
                postId = postId,
                showComments = showComments,
                commentId = commentId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { navigateUserId ->
                    navController.navigate("${MainRoute.User.route}/$navigateUserId")
                }
            )
        }
    }
}