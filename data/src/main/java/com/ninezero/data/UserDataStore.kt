package com.ninezero.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "user_datastore")

class UserDataStore @Inject constructor(
    private val context: Context
) {
    companion object {
        private val ONBOARDING_STATUS = booleanPreferencesKey("onboarding_completed")
        private val TOKEN = stringPreferencesKey("token")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val USER_ID = longPreferencesKey("user_id")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    suspend fun updateOnboardingStatus(isCompleted: Boolean) =
        context.dataStore.edit { it[ONBOARDING_STATUS] = isCompleted }
    suspend fun hasCompletedOnboarding(): Boolean =
        context.dataStore.data.map { it[ONBOARDING_STATUS] == true }.first()

    suspend fun setToken(token: String) = context.dataStore.edit { it[TOKEN] = token }
    suspend fun getToken(): String? = context.dataStore.data.map { it[TOKEN] }.first()

    suspend fun setDarkMode(isDarkMode: Boolean) = context.dataStore.edit { it[DARK_MODE] = isDarkMode }
    fun getDarkMode() = context.dataStore.data.map { it[DARK_MODE] == true }

    suspend fun setUserId(userId: Long) = context.dataStore.edit { it[USER_ID] = userId }
    suspend fun getUserId(): Long = context.dataStore.data.map { it[USER_ID] ?: -1L }.first()

    suspend fun setFcmToken(token: String) = context.dataStore.edit { it[FCM_TOKEN] = token }
    suspend fun getFcmToken(): String? = context.dataStore.data.map { it[FCM_TOKEN] }.first()

    suspend fun clear() = context.dataStore.edit {
        it.remove(TOKEN)
        it.remove(USER_ID)
    }
}