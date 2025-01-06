package com.ninezero.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ThemeUseCase {
    suspend fun updateTheme(isDark: Boolean)
    fun getTheme(): Flow<Boolean>
}