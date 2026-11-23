// Defines editable player statistics and fixture choices.

package com.formup.app.ui.stats

import androidx.compose.ui.graphics.Color
import com.formup.app.ui.theme.FormUpColors

data class PlayerStatEntry(
    val id: String,
    val name: String,
    val position: String,
    val goals: Int,
    val assists: Int,
    val minutesPlayed: Int = 0,
    val rating: Double = 0.0,
    val badgeColor: Color = FormUpColors.Primary,
    val badgeTextColor: Color = FormUpColors.Surface
)

data class FixtureOption(val id: String, val label: String)

data class StatsInputUiState(
    val fixtureId: String,
    val resultBadge: String,
    val matchDate: String,
    val opponent: String,
    val homeLabel: String,
    val awayLabel: String,
    val homeScore: Int,
    val awayScore: Int,
    val availableFixtures: List<FixtureOption> = emptyList(),
    val players: List<PlayerStatEntry>
)
