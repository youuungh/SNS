package com.ninezero.presentation.auth

sealed class AuthRoute(val route: String) {
    object Onboarding : AuthRoute("onboarding")
    object Login : AuthRoute("login")
    object SignUp : AuthRoute("signup")
}