// Builds and reads the JSON used for queued changes.

package com.formup.app.offline

import com.formup.app.data.SquadUpdate
import com.formup.app.data.StatUpdate
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

object WriteKind {
    const val REMINDERS = "reminders"
    const val MATCH_REPORT = "match_report"
    const val MATCH_LINEUP = "match_lineup"
    const val PROFILE = "profile"
}

object WritePayloads {
    private val gson = Gson()
    fun report(teamScore: Int, opponentScore: Int, stats: List<StatUpdate>): String =
        JSONObject().put("teamScore", teamScore).put("opponentScore", opponentScore)
            .put("stats", JSONArray(gson.toJson(stats))).toString()

    fun lineup(squad: List<SquadUpdate>, name: String?, code: String?): String =
        JSONObject().put("squad", JSONArray(gson.toJson(squad)))
            .put("formationName", name).put("formationCode", code).toString()

    fun profile(name: String, team: String, age: Int, language: String): String =
        JSONObject().put("displayName", name).put("teamName", team).put("ageGroup", age).put("language", language).toString()

    fun squad(json: JSONObject): List<SquadUpdate> =
        gson.fromJson(json.getJSONArray("squad").toString(), Array<SquadUpdate>::class.java).toList()
    fun stats(json: JSONObject): List<StatUpdate> =
        gson.fromJson(json.getJSONArray("stats").toString(), Array<StatUpdate>::class.java).toList()
}
