package com.ninezero.presentation.main

import android.net.Uri
import androidx.annotation.DrawableRes
import com.ninezero.presentation.R

sealed class MainRoute(val route: String) {
    object Main : MainRoute("main")
    object User : MainRoute("user")
    object Chat : MainRoute("chat") {
        fun navigate(
            otherUserId: Long,
            roomId: String?,
            otherUserLoginId: String,
            otherUserName: String,
            otherUserProfilePath: String?,
            myUserId: Long
        ) = "chat/$otherUserId/$roomId/$otherUserLoginId/$otherUserName/${Uri.encode(otherUserProfilePath ?: "")}/$myUserId"
    }

    sealed class BottomNavItem(
        route: String,
        @DrawableRes val defaultIconRes: Int,
        @DrawableRes val selectedIconRes: Int = defaultIconRes,
        val contentDescription: String,
        val isProfile: Boolean = false
    ) : MainRoute(route) {
        object Feed : BottomNavItem(
            route = "feed",
            defaultIconRes = R.drawable.ic_home,
            selectedIconRes = R.drawable.ic_home_bold,
            contentDescription = "Feed"
        )

        object Search : BottomNavItem(
            route = "search",
            defaultIconRes = R.drawable.ic_search,
            contentDescription = "Search"
        )

        object Post : BottomNavItem(
            route = "post",
            defaultIconRes = R.drawable.ic_add,
            contentDescription = "Post"
        )

        object Message : BottomNavItem(
            route = "message",
            defaultIconRes = R.drawable.ic_message,
            contentDescription = "Message"
        )

        object Profile : BottomNavItem(
            route = "profile",
            defaultIconRes = R.drawable.user_placeholder,
            contentDescription = "Profile",
            isProfile = true
        )

        companion object {
            fun bottomNavItems() = listOf(Feed, Search, Post, Message, Profile)
        }
    }
}