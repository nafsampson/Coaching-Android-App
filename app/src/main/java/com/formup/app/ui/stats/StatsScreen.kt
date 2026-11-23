// Shows season totals, leading players and match summaries.

package com.formup.app.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.components.*
import com.formup.app.ui.theme.FormUpColors

@Composable
fun StatsScreen(state: StatsUiState, onInputStats: () -> Unit = {}, onExportPdf: () -> Unit = {},
    onViewFullMatchReport: () -> Unit = {}, onNotifications: () -> Unit = {}, onSelectTab: (HomeTab) -> Unit = {}, modifier: Modifier = Modifier, hasUnread: Boolean = false) {
    var assists by rememberSaveable { mutableStateOf(false) }
    val fontScale = LocalDensity.current.fontScale
    Scaffold(modifier.fillMaxSize(), containerColor=FormUpColors.Background,
        topBar={FormUpTopBar(hasUnread,onNotifications)}, bottomBar={FormUpBottomBar(HomeTab.Stats,onSelectTab)}) { inner ->
        LazyColumn(Modifier.fillMaxSize().padding(inner), contentPadding=PaddingValues(20.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
            item {
                Text(stringResource(R.string.stats_team_analytics),style=MaterialTheme.typography.headlineLarge,color=FormUpColors.TextPrimary)
                Text(state.squadName.ifBlank { stringResource(R.string.stats_your_squad) },style=MaterialTheme.typography.bodyMedium,color=FormUpColors.TextSecondary)
            }
            item {
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    com.formup.app.ui.common.FormUpButton(onInputStats,Modifier.fillMaxWidth().heightIn(min=52.dp)) { Text(stringResource(R.string.mod_record_result)) }
                    com.formup.app.ui.common.ExportButton(stringResource(R.string.mod_season_export), onExportPdf)
                }
            }
            item {
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    state.summary.chunked(if (fontScale>1.3f) 1 else 2).forEach { metrics ->
                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { metrics.forEach { metric -> InsightMetric(metric,Modifier.weight(1f)) } }
                    }
                }
            }
            item {
                Text(stringResource(R.string.mod_player_performance),style=MaterialTheme.typography.titleLarge,color=FormUpColors.TextPrimary)
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    FilterChip(!assists,{assists=false},label={Text(stringResource(R.string.pdf_goals))},modifier=Modifier.heightIn(min=48.dp))
                    FilterChip(assists,{assists=true},label={Text(stringResource(R.string.pdf_assists))},modifier=Modifier.heightIn(min=48.dp))
                }
            }
            val leaders=if(assists) state.assistsLeaders else state.goalsLeaders
            if(leaders.isEmpty()) item { SectionCard { Text(stringResource(R.string.mod_no_performers),color=FormUpColors.TextSecondary) } }
            items(leaders.size) { index ->
                val player=leaders[index]
                SectionCard {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                        Text("${index+1}".padStart(2,'0'),style=MaterialTheme.typography.headlineMedium,color=FormUpColors.Primary)
                        Text(player.name,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium,color=FormUpColors.TextPrimary)
                        Text(player.statValue.substringBefore(' '),style=MaterialTheme.typography.headlineMedium,color=FormUpColors.TextPrimary)
                    }
                }
            }
            if (state.lastMatch.opponent != "—") item {
                SectionCard {
                    Text(stringResource(R.string.last_match),style=MaterialTheme.typography.labelLarge,color=FormUpColors.TextSecondary)
                    Text(state.lastMatch.opponent,style=MaterialTheme.typography.headlineMedium,color=FormUpColors.TextPrimary)
                    state.lastMatch.rows.forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                            Text(row.label,Modifier.weight(1f),color=FormUpColors.TextSecondary)
                            Text(row.valueText,color=FormUpColors.TextPrimary)
                        }
                    }
                    TextButton(onViewFullMatchReport,Modifier.heightIn(min=48.dp)) { Text(stringResource(R.string.mod_match_report)) }
                }
            }
        }
    }
}
@Composable
private fun InsightMetric(metric: SummaryStat,modifier:Modifier) {
    Surface(modifier,shape=RoundedCornerShape(24.dp),color=if(metric.highlighted) FormUpColors.PrimaryTint else FormUpColors.Surface) {
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(metric.value,style=MaterialTheme.typography.headlineLarge,color=FormUpColors.TextPrimary)
            Text(metric.label,style=MaterialTheme.typography.bodySmall,color=FormUpColors.TextSecondary)
            LinearProgressIndicator(progress={metric.progress.coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(4.dp),color=FormUpColors.Primary,trackColor=FormUpColors.Hairline)
        }
    }
}
