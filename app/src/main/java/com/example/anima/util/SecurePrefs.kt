package com.example.anima.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit

class SecurePrefs(context: Context) {
    private val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs = EncryptedSharedPreferences.create(
        "anima_secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_THEME_MODE = "theme_mode" // 0=system,1=light,2=dark
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        private const val KEY_REMINDER_MSG = "reminder_msg"
        private const val KEY_PIN = "pin"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCK_UNTIL = "lock_until"
        private const val KEY_LAST_REMINDER_FIRED = "last_reminder_fired"
    }

    fun getThemeMode(): Int = prefs.getInt(KEY_THEME_MODE, 0)
    fun setThemeMode(mode: Int) = prefs.edit { putInt(KEY_THEME_MODE, mode) }

    fun isReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_ENABLED, false)
    fun setReminderEnabled(enabled: Boolean) = prefs.edit { putBoolean(KEY_REMINDER_ENABLED, enabled) }

    fun getReminderHour(): Int = prefs.getInt(KEY_REMINDER_HOUR, 20)
    fun setReminderHour(h: Int) = prefs.edit { putInt(KEY_REMINDER_HOUR, h) }

    fun getReminderMinute(): Int = prefs.getInt(KEY_REMINDER_MINUTE, 0)
    fun setReminderMinute(m: Int) = prefs.edit { putInt(KEY_REMINDER_MINUTE, m) }

    fun getReminderMessage(): String = prefs.getString(KEY_REMINDER_MSG, "¿Cómo te sentís hoy?") ?: "¿Cómo te sentís hoy?"
    fun setReminderMessage(msg: String) = prefs.edit { putString(KEY_REMINDER_MSG, msg) }

    fun getPin(): String? = prefs.getString(KEY_PIN, null)
    fun setPin(pin: String) = prefs.edit { putString(KEY_PIN, pin) }

    fun getFailedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
    fun incrementFailedAttempts(): Int {
        val next = getFailedAttempts() + 1
        prefs.edit { putInt(KEY_FAILED_ATTEMPTS, next) }
        return next
    }

    fun resetFailedAttempts() = prefs.edit { putInt(KEY_FAILED_ATTEMPTS, 0) }

    fun getLockUntil(): Long = prefs.getLong(KEY_LOCK_UNTIL, 0L)
    fun setLockUntil(untilMs: Long) = prefs.edit { putLong(KEY_LOCK_UNTIL, untilMs) }

    // Record when the last reminder notification was fired (epoch millis)
    fun setLastReminderFired(ts: Long) = prefs.edit { putLong(KEY_LAST_REMINDER_FIRED, ts) }
    fun getLastReminderFired(): Long = prefs.getLong(KEY_LAST_REMINDER_FIRED, 0L)
}
