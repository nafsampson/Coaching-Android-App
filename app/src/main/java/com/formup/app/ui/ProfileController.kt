// Queues profile changes and saves notification preferences.

package com.formup.app.ui

import android.app.Application
import android.util.Log
import com.formup.app.R
import com.formup.app.data.FormUpApi
import com.formup.app.notifications.NotificationPrefs
import com.formup.app.offline.PendingWrites
import com.formup.app.offline.SyncScheduler
import com.formup.app.offline.WriteKind
import com.formup.app.offline.WritePayloads
import com.formup.app.security.SessionLease
import com.formup.app.security.runCatchingCancellable
import com.formup.app.ui.profile.LanguageOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class ProfileController(
    private val state: SeasonState,
    private val application: Application,
    private val api: FormUpApi,
    private val scope: CoroutineScope,
    private val messages: MessageSink,
    private val back: () -> Boolean,
    private val writes: PendingWrites?,
    private val session: SessionLease?
) {
    fun setLanguage(option: LanguageOption) {
        state.coach = state.coach.copy(languageCode = option.code)
        val payload = WritePayloads.profile(state.coach.name, state.team.name, state.team.ageGroup ?: 0, state.coach.languageCode)
        scope.launch {
            runCatchingCancellable {
                (writes ?: error("Sign in to save")).put(WriteKind.PROFILE, "profile", payload)
                SyncScheduler.schedule(application, session?.uid ?: error("Sign in to save"))
            }.onSuccess { messages.notify(R.string.msg_saved_pending_sync) }
                .onFailure { messages.notify(R.string.msg_language_update_failed) }
        }
    }

    fun setMatchReminders(enabled: Boolean) {
        state.coach = state.coach.copy(matchReminders = enabled)
        NotificationPrefs.setMatchReminders(application, enabled)
        scope.launch {
            runCatchingCancellable {
                (writes ?: error("Sign in to save")).put(WriteKind.REMINDERS, "reminders", org.json.JSONObject().put("enabled", enabled).toString())
                SyncScheduler.schedule(application, session?.uid ?: error("Sign in to save"))
            }.onFailure {
                Log.w("FormUpFCM", "Reminder preference update failed", it)
                messages.notify(R.string.offline_save_failed)
            }
        }
        messages.notify(if (enabled) R.string.loc_reminders_on else R.string.loc_reminders_off)
    }

    // Weekly summaries are a phone setting and Firebase topic preference.
    fun setWeeklySummary(enabled: Boolean) {
        state.coach = state.coach.copy(weeklySummary = enabled)
        NotificationPrefs.setWeeklySummary(application, enabled)
        messages.notify(if (enabled) R.string.loc_weekly_on else R.string.loc_weekly_off)
    }

    fun updateProfile(name: String) {
        state.coach = state.coach.copy(name = name.ifBlank { state.coach.name })
    }

    fun updateTeam(name: String, squad: String) {
        val age = squad.filter { it.isDigit() }.toIntOrNull() ?: state.team.ageGroup
        state.team = state.team.copy(
            name = name.ifBlank { state.team.name },
            squad = age?.let { "U$it" } ?: state.team.squad,
            ageGroup = age
        )
    }

    fun saveProfileEdits() {
        val payload = WritePayloads.profile(state.coach.name, state.team.name, state.team.ageGroup ?: 0, state.coach.languageCode)
        scope.launch {
            runCatchingCancellable {
                (writes ?: error("Sign in to save")).put(WriteKind.PROFILE, "profile", payload)
                SyncScheduler.schedule(application, session?.uid ?: error("Sign in to save"))
            }.onSuccess {
                messages.notify(R.string.msg_saved_pending_sync)
                back()
            }.onFailure {
                messages.notify(R.string.msg_profile_update_failed)
            }
        }
    }

}
