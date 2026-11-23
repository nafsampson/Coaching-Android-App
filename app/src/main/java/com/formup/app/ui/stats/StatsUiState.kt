// Defines statistics cards and match analysis.

package com.formup.app.ui.stats

import androidx.compose.ui.graphics.Color
import com.formup.app.ui.theme.FormUpColors

data class SummaryStat(
    val label: String,
    val value: String,
    val delta: String? = null,
    val progress: Float,
    val barColor: Color = FormUpColors.Primary,
    val highlighted: Boolean = false
)

data class TopPerformer(
    val name: String,
    val position: String,
    val matches: Int,
    val statValue: String,
    val badgeColor: Color = FormUpColors.NavIndicator,
    val badgeTextColor: Color = FormUpColors.TextPrimary
)

data class MatchStatRow(
    val label: String,
    val valueText: String,
    val progress: Float,
    val barColor: Color = FormUpColors.Primary
)

data class LastMatchAnalysis(
    val opponent: String,
    val resultText: String,
    val rows: List<MatchStatRow>
)

data class StatsUiState(
    val squadName: String,
    val summary: List<SummaryStat>,
    val goalsLeaders: List<TopPerformer>,
    val assistsLeaders: List<TopPerformer>,
    val lastMatch: LastMatchAnalysis
)
