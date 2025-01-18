package com.ninezero.presentation.auth

import androidx.lifecycle.ViewModel
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()

    fun checkInitRoute(): String = runBlocking {
        when {
            !userUseCase.hasCompletedOnboarding() -> AuthRoute.Onboarding.route
            else -> AuthRoute.Login.route
        }
    }

    fun isLoggedIn(): Boolean = runBlocking {
        userUseCase.getToken() != null && userUseCase.hasCompletedOnboarding()
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }

    suspend fun completeOnboarding() = userUseCase.updateOnboardingStatus(isCompleted = true)
}