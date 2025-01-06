package com.ninezero.presentation.main

import androidx.annotation.DrawableRes
import com.ninezero.presentation.R

sealed class MainRoute(val route: String) {
    object Main: MainRoute("main")

    sealed class BottomNavItem(
        route: String,
        @DrawableRes val defaultIconRes: Int,
        @DrawableRes val selectedIconRes: Int = defaultIconRes,
        val contentDescription: String,
        val isProfile: Boolean = false
    ) : MainRoute(route) {
        object Feed: BottomNavItem(
            route = "feed",
            defaultIconRes = R.drawable.ic_home,
            selectedIconRes = R.drawable.ic_home_bold,
            contentDescription = "Feed"
        )
        object Post: BottomNavItem(
            route = "post",
            defaultIconRes = R.drawable.ic_add,
            contentDescription = "Post"
        )
        object Profile: BottomNavItem(
            route = "profile",
            defaultIconRes = R.drawable.user_placeholder,
            contentDescription = "Profile",
            isProfile = true
        )

        companion object {
            fun bottomNavItems() = listOf(Feed, Post, Profile)
        }
    }
}