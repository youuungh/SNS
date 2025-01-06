package com.ninezero.data.usecase

import com.ninezero.data.UserDataStore
import com.ninezero.domain.usecase.ThemeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeUseCaseImpl @Inject constructor(
    private val userDataStore: UserDataStore
) : ThemeUseCase {
    override suspend fun updateTheme(isDark: Boolean) {
        userDataStore.setDarkMode(isDark)
    }

    override fun getTheme(): Flow<Boolean> {
        return userDataStore.getDarkMode().map { it == true }
    }
}