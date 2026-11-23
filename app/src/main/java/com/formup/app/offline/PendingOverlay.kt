// Applies unsynced profile, report and lineup changes to season data.

package com.formup.app.offline

import com.formup.app.data.MatchLine
import com.formup.app.data.SeasonSnapshot
import org.json.JSONObject

object PendingOverlay {
    fun apply(snapshot: SeasonSnapshot, pending: List<PendingSyncEntity>): SeasonSnapshot {
        var result = snapshot
        for (item in pending) {

            val body = runCatching { JSONObject(item.payloadJson) }.getOrNull() ?: continue
            result = runCatching { when (item.kind) {
                WriteKind.PROFILE -> result.copy(
                    coach = result.coach.copy(name = body.getString("displayName"), languageCode = body.optString("language", result.coach.languageCode)),
                    team = result.team.copy(name = body.getString("teamName"), ageGroup = body.getInt("ageGroup"), squad = "U${body.getInt("ageGroup")}")
                )
                WriteKind.MATCH_REPORT -> result.copy(fixtures = result.fixtures.map { fixture ->
                    if (fixture.id != item.targetId) fixture else fixture.copy(
                        teamScore = body.getInt("teamScore"), opponentScore = body.getInt("opponentScore"), played = true,
                        playerStats = WritePayloads.stats(body).associate { stat -> stat.playerId to MatchLine(stat.goals, stat.assists, stat.minutesPlayed, stat.rating, stat.cleansheet) }
                    )
                })
                WriteKind.MATCH_LINEUP -> result.copy(fixtures = result.fixtures.map { fixture ->
                    if (fixture.id != item.targetId) fixture else fixture.copy(
                        squad = WritePayloads.squad(body).associateBy { it.playerId },
                        formationName = body.optString("formationName", fixture.formationName),
                        formationCode = body.optString("formationCode", fixture.formationCode)
                    )
                })
                else -> result
            } }.getOrDefault(result)
        }
        return result
    }
}
