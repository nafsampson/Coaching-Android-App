// Loads season data and falls back to the saved copy when offline.

package com.formup.app.data

import android.util.Log
import com.formup.app.offline.isConnectivityFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class SeasonApi(private val transport: ApiTransport, private val cache: ResponseCache?) {
    suspend fun loadSeason(): SeasonSnapshot = withContext(Dispatchers.IO) {
        try {
            val coach = transport.request("GET", "/api/coach")
            val team = transport.request("GET", "/api/team")
            val season = transport.request("GET", "/api/season-data")

            // Cache all three responses together so offline screens use the same snapshot.
            cache?.put("season-snapshot", JSONObject().put("coach", JSONObject(coach))
                .put("team", JSONObject(team)).put("season", JSONObject(season)).toString())

            buildSnapshot(JSONObject(coach), JSONObject(team), JSONObject(season), fromCache = false)
        } catch (e: Exception) {

            // Do not use cached data to hide authentication or server rejections.
            if (!isConnectivityFailure(e)) throw e
            val cached = cache?.get("season-snapshot") ?: throw e
            val snapshot = JSONObject(cached)
            val coach = snapshot.getJSONObject("coach").toString()
            val team = snapshot.getJSONObject("team").toString()
            val season = snapshot.getJSONObject("season").toString()
            Log.w("FormUpApi", "Offline - showing last saved season data")
            buildSnapshot(JSONObject(coach), JSONObject(team), JSONObject(season), fromCache = true)
        }
    }

    suspend fun cachedSeason(): SeasonSnapshot? = withContext(Dispatchers.IO) {
        val raw = cache?.get("season-snapshot") ?: return@withContext null
        val saved = JSONObject(raw)
        buildSnapshot(saved.getJSONObject("coach"), saved.getJSONObject("team"), saved.getJSONObject("season"), true)
    }

    private fun buildSnapshot(coach: JSONObject, team: JSONObject, season: JSONObject, fromCache: Boolean) =
        SeasonSnapshot(
            coach = coach.toCoachProfile(),
            team = team.toTeamProfile(),
            players = season.optJSONArray("players").toPlayers(),
            fixtures = season.optJSONArray("matches").toFixtures(),
            sessions = season.optJSONArray("sessions").toSessions(),
            fromCache = fromCache
        )
}
