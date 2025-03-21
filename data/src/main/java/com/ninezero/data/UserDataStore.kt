package com.ninezero.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ninezero.domain.usecase.NotificationType
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
        private val LOGIN_TYPE = stringPreferencesKey("login_type")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val USER_ID = longPreferencesKey("user_id")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val FOLLOW_NOTIFICATIONS = booleanPreferencesKey("follow_notifications")
        private val LIKE_NOTIFICATIONS = booleanPreferencesKey("like_notifications")
        private val COMMENT_NOTIFICATIONS = booleanPreferencesKey("comment_notifications")
        private val REPLY_NOTIFICATIONS = booleanPreferencesKey("reply_notifications")
    }

    suspend fun updateOnboardingStatus(isCompleted: Boolean) =
        context.dataStore.edit { it[ONBOARDING_STATUS] = isCompleted }
    suspend fun hasCompletedOnboarding(): Boolean =
        context.dataStore.data.map { it[ONBOARDING_STATUS] == true }.first()

    suspend fun setToken(token: String) = context.dataStore.edit { it[TOKEN] = token }
    suspend fun getToken(): String? = context.dataStore.data.map { it[TOKEN] }.first()

    suspend fun setLoginType(type: String) = context.dataStore.edit { it[LOGIN_TYPE] = type }
    suspend fun getLoginType(): String? = context.dataStore.data.map { it[LOGIN_TYPE] }.first()

    suspend fun setDarkMode(isDarkMode: Boolean) = context.dataStore.edit { it[DARK_MODE] = isDarkMode }
    fun getDarkMode() = context.dataStore.data.map { it[DARK_MODE] == true }

    suspend fun setUserId(userId: Long) = context.dataStore.edit { it[USER_ID] = userId }
    suspend fun getUserId(): Long = context.dataStore.data.map { it[USER_ID] ?: -1L }.first()

    suspend fun setFcmToken(token: String) = context.dataStore.edit { it[FCM_TOKEN] = token }
    suspend fun getFcmToken(): String? = context.dataStore.data.map { it[FCM_TOKEN] }.first()

    suspend fun setNotificationsEnabled(enabled: Boolean) = context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    fun getNotificationsEnabled() = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] != false }

    suspend fun setNotificationType(type: NotificationType, enabled: Boolean) {
        val key = when (type) {
            is NotificationType.FOLLOW -> FOLLOW_NOTIFICATIONS
            is NotificationType.LIKE -> LIKE_NOTIFICATIONS
            is NotificationType.COMMENT -> COMMENT_NOTIFICATIONS
            is NotificationType.REPLY -> REPLY_NOTIFICATIONS
        }
        context.dataStore.edit { it[key] = enabled }
    }
    fun getNotificationType(type: NotificationType) = context.dataStore.data.map {
        val key = when (type) {
            is NotificationType.FOLLOW -> FOLLOW_NOTIFICATIONS
            is NotificationType.LIKE -> LIKE_NOTIFICATIONS
            is NotificationType.COMMENT -> COMMENT_NOTIFICATIONS
            is NotificationType.REPLY -> REPLY_NOTIFICATIONS
        }
        it[key] != false
    }

    suspend fun clear() = context.dataStore.edit {
        it.remove(LOGIN_TYPE)
        it.remove(TOKEN)
        it.remove(USER_ID)
    }
}