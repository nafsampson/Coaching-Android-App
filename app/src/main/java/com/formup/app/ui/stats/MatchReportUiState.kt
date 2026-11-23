// Defines match report scores, performance summaries and player ratings.

package com.formup.app.ui.stats

data class TeamScoreInfo(
    val abbreviation: String,
    val name: String,
    val score: Int,
    val isFormUp: Boolean = false
)

data class PerformanceSummary(
    val rating: String,
    val ratingLabel: String,
    val minutes: String,
    val goals: String,
    val assists: String
)

data class PlayerRating(
    val rank: Int,
    val name: String,
    val note: String,
    val rating: String,
    val isStandout: Boolean = false
)

data class MatchReportUiState(
    val statusLine: String,
    val awayTeam: TeamScoreInfo,
    val homeTeam: TeamScoreInfo,
    val overview: String,
    val myPerformance: PerformanceSummary,
    val ratings: List<PlayerRating>
)
