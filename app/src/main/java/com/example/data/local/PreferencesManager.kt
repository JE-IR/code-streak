package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("codestreak_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_SHAKE_SENSITIVITY = "shake_sensitivity" // "LOW", "MEDIUM", "HIGH"
        private const val KEY_LAST_SYNC_QUOTE = "last_sync_quote"
        private const val KEY_THEME_MODE = "theme_mode" // "SYSTEM", "DARK", "LIGHT"
        private const val KEY_IS_ADMIN = "is_admin"
    }

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var isAdmin: Boolean
        get() = prefs.getBoolean(KEY_IS_ADMIN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ADMIN, value).apply()

    var currentUserId: Long
        get() = prefs.getLong(KEY_CURRENT_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_CURRENT_USER_ID, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var shakeSensitivity: String
        get() = prefs.getString(KEY_SHAKE_SENSITIVITY, "MEDIUM") ?: "MEDIUM"
        set(value) = prefs.edit().putString(KEY_SHAKE_SENSITIVITY, value).apply()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_CURRENT_USER_ID)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_IS_ADMIN, false)
            .apply()
    }
}
