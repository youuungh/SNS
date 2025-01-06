package com.ninezero.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.domain.usecase.ThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val themeUseCase: ThemeUseCase
) : ViewModel() {
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
}