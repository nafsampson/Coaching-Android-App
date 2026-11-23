// Stores the biometric setting separately for each account.

package com.formup.app.security

import android.content.Context

object BiometricPreferences {

    private const val PREFERENCES_NAME = "formup_security_preferences"
    private const val BIOMETRIC_ENABLED_PREFIX = "biometric_enabled_"

    fun isEnabled(context: Context, userId: String): Boolean {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        return preferences.getBoolean(
            BIOMETRIC_ENABLED_PREFIX + userId,
            false
        )
    }

    fun setEnabled(
        context: Context,
        userId: String,
        enabled: Boolean
    ) {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        preferences.edit()
            .putBoolean(BIOMETRIC_ENABLED_PREFIX + userId, enabled)
            .apply()
    }
}
