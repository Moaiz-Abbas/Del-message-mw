package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vault_private_prefs", Context.MODE_PRIVATE)

    private val _whatsAppEnabled = MutableStateFlow(prefs.getBoolean(KEY_WHATSAPP_ENABLED, true))
    val whatsAppEnabled: StateFlow<Boolean> = _whatsAppEnabled.asStateFlow()

    private val _messengerEnabled = MutableStateFlow(prefs.getBoolean(KEY_MESSENGER_ENABLED, true))
    val messengerEnabled: StateFlow<Boolean> = _messengerEnabled.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK_ENABLED, false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _hidePreviews = MutableStateFlow(prefs.getBoolean(KEY_HIDE_PREVIEWS, false))
    val hidePreviews: StateFlow<Boolean> = _hidePreviews.asStateFlow()

    private val _retentionDays = MutableStateFlow(prefs.getInt(KEY_RETENTION_DAYS, 0))
    val retentionDays: StateFlow<Int> = _retentionDays.asStateFlow()

    private val _hasCompletedOnboarding =
        MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    fun setWhatsAppEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WHATSAPP_ENABLED, enabled).apply()
        _whatsAppEnabled.value = enabled
    }

    fun setMessengerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MESSENGER_ENABLED, enabled).apply()
        _messengerEnabled.value = enabled
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
        _appLockEnabled.value = enabled
    }

    fun setHidePreviews(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_PREVIEWS, enabled).apply()
        _hidePreviews.value = enabled
    }

    fun setRetentionDays(days: Int) {
        prefs.edit().putInt(KEY_RETENTION_DAYS, days).apply()
        _retentionDays.value = days
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _whatsAppEnabled.value = true
        _messengerEnabled.value = true
        _appLockEnabled.value = false
        _hidePreviews.value = false
        _retentionDays.value = 0
        _hasCompletedOnboarding.value = false
    }

    companion object {
        private const val KEY_WHATSAPP_ENABLED = "pref_whatsapp_enabled"
        private const val KEY_MESSENGER_ENABLED = "pref_messenger_enabled"
        private const val KEY_APP_LOCK_ENABLED = "pref_app_lock_enabled"
        private const val KEY_HIDE_PREVIEWS = "pref_hide_previews"
        private const val KEY_RETENTION_DAYS = "pref_retention_days"
        private const val KEY_ONBOARDING_COMPLETED = "pref_onboarding_completed"
    }
}
