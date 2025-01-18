package com.ninezero.presentation.main

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userUseCase: UserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        when (val result = userUseCase.getMyUser()) {
            is ApiResult.Success -> {
                _uiState.update {
                    it.copy(
                        profileImageUrl = result.data.profileImagePath
                    )
                }
            }
            else -> Unit
        }
    }
}

@Immutable
data class MainUiState(
    val profileImageUrl: String? = null
)