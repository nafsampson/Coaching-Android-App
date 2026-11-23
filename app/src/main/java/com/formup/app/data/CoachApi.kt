// Updates coach details, team details and reminder preferences.

package com.formup.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class CoachApi(private val transport: ApiTransport) {
    suspend fun registerDeviceToken(token: String) = withContext(Dispatchers.IO) {
        transport.request(
            method = "POST",
            path = "/api/coach/device-token",
            body = JSONObject().put("token", token).put("platform", "android")
        )
    }

    suspend fun updateReminderPreference(enabled: Boolean) =
        withContext(Dispatchers.IO) {
            transport.request(
                method = "PUT",
                path = "/api/coach/reminders",
                body = JSONObject().put("enabled", enabled)
            )
        }

    suspend fun updateCoach(
        displayName: String,
        preferredLanguage: String? = null
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("displayName", displayName)

        if (preferredLanguage != null) {
            body.put("preferredLanguage", preferredLanguage)
        }

        transport.request(
            method = "PATCH",
            path = "/api/coach",
            body = body
        )
    }

    suspend fun updateTeam(
        teamName: String,
        ageGroup: Int
    ) = withContext(Dispatchers.IO) {
        transport.request(
            method = "PUT",
            path = "/api/team",
            body = JSONObject()
                .put("teamName", teamName)
                .put("ageGroup", ageGroup)
        )
    }
}
