package com.ninezero.presentation.auth

import androidx.lifecycle.ViewModel
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    fun checkInitRoute(): String = runBlocking {
        when {
            !userUseCase.hasCompletedOnboarding() -> AuthRoute.Onboarding.route
            else -> AuthRoute.Login.route
        }
    }

    fun isLoggedIn(): Boolean = runBlocking {
        userUseCase.getToken() != null && userUseCase.hasCompletedOnboarding()
    }

    suspend fun completeOnboarding() {
        userUseCase.updateOnboardingStatus(isCompleted = true)
    }
}