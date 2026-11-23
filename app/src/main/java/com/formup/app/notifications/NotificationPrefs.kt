// Stores notification preferences and manages topic subscriptions.

package com.formup.app.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "FormUpPush"

object NotificationPrefs {
    private const val FILE = "formup_notification_prefs"
    private const val KEY_TOKEN = "fcm_token"
    private const val KEY_MATCH_REMINDERS = "match_reminders"
    private const val KEY_WEEKLY_SUMMARY = "weekly_summary"

    const val TOPIC_TEAM_UPDATES = "team_updates"
    const val TOPIC_MATCH_REMINDERS = "match_reminders"
    const val TOPIC_WEEKLY_SUMMARY = "weekly_summary"

    private fun prefs(context: Context) = context.getSharedPreferences(FILE + "_" + (com.formup.app.security.AccountSessions.current.value?.uid ?: "signed-out"), Context.MODE_PRIVATE)

    fun saveToken(context: Context, token: String) = prefs(context).edit().putString(KEY_TOKEN, token).apply()

    fun matchReminders(context: Context) = prefs(context).getBoolean(KEY_MATCH_REMINDERS, true)
    fun weeklySummary(context: Context) = prefs(context).getBoolean(KEY_WEEKLY_SUMMARY, false)

    fun setMatchReminders(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MATCH_REMINDERS, enabled).apply()

    }

    fun setWeeklySummary(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_WEEKLY_SUMMARY, enabled).apply()

    }

    fun syncSubscriptions(context: Context) {

        unsubscribeAll()
    }

    fun unsubscribeAll() {
        listOf(TOPIC_TEAM_UPDATES, TOPIC_MATCH_REMINDERS, TOPIC_WEEKLY_SUMMARY).forEach { setSubscribed(it, false) }
    }

    private fun setSubscribed(topic: String, subscribed: Boolean) {
        val messaging = FirebaseMessaging.getInstance()
        val task = if (subscribed) messaging.subscribeToTopic(topic) else messaging.unsubscribeFromTopic(topic)
        task.addOnCompleteListener {
            Log.i(TAG, "Topic '$topic' ${if (subscribed) "subscribe" else "unsubscribe"} " +
                    if (it.isSuccessful) "ok" else "failed: ${it.exception?.message}")
        }
    }
}
