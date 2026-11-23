// Defines dashboard data for the coach and their fixtures.

package com.formup.app.ui.home

data class NextMatch(
    val opponent: String,
    val date: String,
    val kickoff: String,
    val venue: String,
    val teamName: String = ""
) {

    val exists: Boolean get() = date != "—"
}

data class Availability(
    val fit: Int,
    val doubtful: Int,
    val out: Int
) {
    val total: Int get() = fit + doubtful + out
}

data class LastMatch(
    val homeTeam: String,
    val homeScore: Int,
    val awayTeam: String,
    val awayScore: Int,
    val status: String,
    val stats: List<Pair<String, String>>
) {

    val exists: Boolean get() = status != "—"
}

data class HomeUiState(
    val coachName: String,
    val prompt: String,
    val nextMatch: NextMatch,
    val availability: Availability,
    val lastMatch: LastMatch,
    val unreadNotifications: Boolean
)
