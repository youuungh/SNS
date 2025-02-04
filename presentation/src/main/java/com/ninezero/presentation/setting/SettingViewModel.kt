package com.ninezero.presentation.setting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.ThemeUseCase
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val themeUseCase: ThemeUseCase
) : ViewModel(), ContainerHost<SettingState, SettingSideEffect> {
    override val container: Container<SettingState, SettingSideEffect> = container(initialState = SettingState())

    private val _isDarkTheme = MutableStateFlow(runBlocking { themeUseCase.getTheme().first() })
    val isDarkTheme = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            themeUseCase.getTheme().collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeUseCase.updateTheme(isDark)
        }
    }

    fun onSignOut() = intent {
        reduce { state.copy(isLoading = true) }

        when (val result = userUseCase.clearToken()) {
            is ApiResult.Success -> postSideEffect(SettingSideEffect.NavigateToLogin)
            is ApiResult.Error -> {
                reduce { state.copy(isLoading = false) }
                postSideEffect(SettingSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun showSignOutDialog() = intent {
        reduce { state.copy(dialog = SettingDialog.SignOut) }
    }

    fun hideDialog() = intent {
        reduce { state.copy(dialog = SettingDialog.Hidden) }
    }
}

@Immutable
data class SettingState(
    val isLoading: Boolean = false,
    val dialog: SettingDialog = SettingDialog.Hidden
)

sealed interface SettingDialog {
    data object Hidden : SettingDialog
    data object SignOut : SettingDialog
}

sealed interface SettingSideEffect {
    data class ShowSnackbar(val message: String) : SettingSideEffect
    object NavigateToLogin : SettingSideEffect
}