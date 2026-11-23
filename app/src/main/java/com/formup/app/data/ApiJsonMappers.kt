// Converts API JSON into players, fixtures, sessions and profiles.

package com.formup.app.data

import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.optNullableDouble(key: String): Double? {
    if (!has(key) || isNull(key)) return null

    return optDouble(key).takeIf { it.isFinite() }
}

internal fun JSONArray?.toPlayers(): List<Player> {

    if (this == null) {
        return emptyList()
    }

    return (0 until length()).map { index ->

        val player = getJSONObject(index)

        Player(
            id = player.getString("id"),
            name = player.optString("name"),
            position = player.optString("position"),
            dateOfBirth =
                if (player.isNull("dateOfBirth")) null
                else player.optString("dateOfBirth")
        )
    }
}

internal fun JSONArray?.toFixtures(): List<Fixture> {

    if (this == null) {
        return emptyList()
    }

    return (0 until length()).map { index ->
        getJSONObject(index).toFixture()
    }
}

internal fun JSONArray?.toSessions(): List<TrainingSession> {
    if (this == null) return emptyList()

    return (0 until length()).map { index ->
        val session = getJSONObject(index)
        val iso = session.optString("sessionDate")
        val date = parseDate(iso)

        TrainingSession(
            id = session.getString("id"),
            dateLabel = date.first,
            time = date.second,
            notes = if (session.isNull("notes")) null else session.optString("notes"),
            sessionDateIso = iso
        )
    }
}

internal fun JSONObject.toFixture(): Fixture {

    val matchDate = optString("matchDate")

    val date = parseDate(matchDate)

    val teamScore =
        if (isNull("teamScore")) {
            0
        } else {
            optInt("teamScore")
        }

    val opponentScore =
        if (isNull("opponentScore")) {
            0
        } else {
            optInt("opponentScore")
        }

    val statsObject =
        optJSONObject("stats") ?: optJSONObject("matchStats")

    val playerStats =
        buildMap<String, MatchLine> {

            if (statsObject != null) {

                statsObject.keys().forEach { playerId ->

                    val stat =
                        statsObject.optJSONObject(playerId)
                            ?: return@forEach

                    put(
                        playerId,
                        MatchLine(
                            goals = stat.optInt("goals"),
                            assists = stat.optInt("assists"),
                            minutes = stat.optInt("minutesPlayed"),
                            rating = stat.optDouble("rating", 0.0),
                            cleansheet = stat.optBoolean("cleansheet")
                        )
                    )
                }
            }
        }

    val squadObject = optJSONObject("squad")

    val squad = buildMap<String, SquadUpdate> {
        if (squadObject != null) {
            squadObject.keys().forEach { playerId ->
                val entry = squadObject.optJSONObject(playerId)
                    ?: return@forEach

                put(
                    playerId,
                    SquadUpdate(
                        playerId = playerId,
                        status = entry.optString("status", "Available"),
                        selection = entry.optString("selection", "NotSelected"),
                        assignedPosition = entry.optString("assignedPosition", ""),
                        pitchX = entry.optNullableDouble("pitchX"),
                        pitchY = entry.optNullableDouble("pitchY")
                    )
                )
            }
        }
    }
    return Fixture(
        id = getString("id"),
        opponent = optString("opponent"),
        dateLabel = date.first,
        kickoff = date.second,
        venue = optString("venue"),
        isHome = optString("venue").equals(ApiValues.VENUE_HOME, ignoreCase = true),
        played =
            !isNull("teamScore") &&
                    !isNull("opponentScore"),
        teamScore = teamScore,
        opponentScore = opponentScore,
        playerStats = playerStats,
        matchDateIso = matchDate,
        squad = squad,
        formationName = optString("formationName"),
        formationCode = optString("formationCode")
    )
}

internal fun JSONObject.toCoachProfile(): CoachProfile {

    return CoachProfile(
        name =
            optString("displayName")
                .ifBlank {
                    optString(
                        "email",
                        "Coach"
                    )
                },
        languageCode =
            optString(
                "preferredLanguage",
                "en"
            ),
        matchReminders = false,
        weeklySummary = false
    )
}

internal fun JSONObject.toTeamProfile(): TeamProfile {

    val name =
        if (isNull("teamName")) "" else optString("teamName")

    val ageGroup =
        if (isNull("ageGroup")) {
            null
        } else {
            optInt("ageGroup")
        }

    return TeamProfile(
        name = name,
        squad = ageGroup?.let { "U$it" } ?: "",
        ageGroup = ageGroup
    )
}

internal fun parseDate(value: String): Pair<String, String> = ServerDates.labels(value)
