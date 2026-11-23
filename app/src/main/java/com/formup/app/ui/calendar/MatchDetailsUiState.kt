// Defines match details and the players shown on the pitch and bench.

package com.formup.app.ui.calendar

data class LineupPlayer(
    val name: String,
    val xPercent: Float,
    val yPercent: Float,
    val isGoalkeeper: Boolean = false
)

data class SubPlayer(val name: String, val position: String)

data class MatchDetailsUiState(
    val title: String,
    val dateLabel: String,
    val timeLabel: String,
    val venueName: String,
    val venueDetail: String,
    val inCount: Int,
    val outCount: Int,
    val tbdCount: Int,
    val squadSize: Int,
    val formation: String,
    val startingXi: List<LineupPlayer>,
    val substitutes: List<SubPlayer>
)
