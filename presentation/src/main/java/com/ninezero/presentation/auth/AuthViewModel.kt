package com.ninezero.presentation.auth

import androidx.lifecycle.ViewModel
import com.ninezero.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    fun checkInitRoute(): String = runBlocking {
        when {
            !authUseCase.hasCompletedOnboarding() -> AuthRoute.Onboarding.route
            else -> AuthRoute.Login.route
        }
    }

    fun isLoggedIn(): Boolean = runBlocking {
        authUseCase.getToken() != null && authUseCase.hasCompletedOnboarding()
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }

    suspend fun completeOnboarding() = authUseCase.updateOnboardingStatus(isCompleted = true)
}