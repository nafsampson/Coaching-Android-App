// Loads and updates matches, squads, formations and statistics.

package com.formup.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal class MatchApi(private val transport: ApiTransport) {
    suspend fun updateMatchScores(
        matchId: String,
        teamScore: Int,
        opponentScore: Int
    ) = withContext(Dispatchers.IO) {
        transport.request(
            method = "PATCH",
            path = "/api/match/$matchId",
            body = JSONObject()
                .put("teamScore", teamScore)
                .put("opponentScore", opponentScore)
        )
    }

    suspend fun getSquad(matchId: String): List<SquadUpdate> =
        withContext(Dispatchers.IO) {

            val response = transport.request(
                method = "GET",
                path = "/api/match/$matchId/squad"
            )

            val array = JSONArray(response)

            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)

                    add(
                        SquadUpdate(
                            playerId = item.optString("playerId"),
                            status = item.optString("status", "Available"),
                            selection = item.optString("selection", "NotSelected"),
                            assignedPosition = item.optString("assignedPosition", ""),
                            pitchX = item.optNullableDouble("pitchX"),
                            pitchY = item.optNullableDouble("pitchY")
                        )
                    )
                }
            }
        }

    suspend fun saveSquad(
        matchId: String,
        players: List<SquadUpdate>
    ) = withContext(Dispatchers.IO) {

        val array = JSONArray()

        players.forEach { player ->
            array.put(
                JSONObject()
                    .put("playerId", player.playerId)
                    .put("status", player.status)
                    .put("selection", player.selection)
                    .put("assignedPosition", player.assignedPosition)
                    .put("pitchX", player.pitchX ?: JSONObject.NULL)
                    .put("pitchY", player.pitchY ?: JSONObject.NULL)
            )
        }

        transport.request(
            method = "PUT",
            path = "/api/match/$matchId/squad",
            body = array
        )
    }

    suspend fun saveMatchFormation(
        matchId: String,
        formationName: String,
        formationCode: String
    ) = withContext(Dispatchers.IO) {

        require(formationName.isNotBlank()) {
            "Formation name cannot be empty"
        }

        require(formationCode.isNotBlank()) {
            "Formation code cannot be empty"
        }

        transport.request(
            method = "PUT",
            path = "/api/match/$matchId/formation",
            body = JSONObject()
                .put("formationName", formationName)
                .put("formationCode", formationCode)
        )
    }

    suspend fun saveStats(
        matchId: String,
        stats: List<StatUpdate>
    ) = withContext(Dispatchers.IO) {
        val array = JSONArray()

        stats.forEach {
            array.put(
                JSONObject()
                    .put("playerId", it.playerId)
                    .put("goals", it.goals)
                    .put("assists", it.assists)
                    .put("cleansheet", it.cleansheet)
                    .put("minutesPlayed", it.minutesPlayed)
                    .put("rating", it.rating)
            )
        }

        transport.request(
            method = "PUT",
            path = "/api/match/$matchId/stats",
            body = array
        )
    }

    suspend fun createMatch(
        opponent: String,
        matchDateIso: String,
        venue: String,
        clientRequestId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val response = transport.request(
            method = "POST",
            path = "/api/match",
            body = JSONObject()
                .put("clientRequestId", clientRequestId ?: JSONObject.NULL)
                .put("opponent", opponent)
                .put("matchDate", matchDateIso)
                .put("venue", venue)
        )
        JSONObject(response).optString("id")
    }

    suspend fun updateMatch(
        matchId: String,
        opponent: String,
        matchDateIso: String,
        venue: String
    ) = withContext(Dispatchers.IO) {
        transport.request(
            method = "PATCH",
            path = "/api/match/$matchId",
            body = JSONObject()
                .put("opponent", opponent)
                .put("matchDate", matchDateIso)
                .put("venue", venue)
        )
    }

    suspend fun deleteMatch(id: String) = withContext(Dispatchers.IO) {
        transport.request(method = "DELETE", path = "/api/match/$id")
    }

}
