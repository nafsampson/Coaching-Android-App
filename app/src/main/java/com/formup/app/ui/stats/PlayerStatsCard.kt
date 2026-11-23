// Shows one player and their recorded match statistics.

package com.formup.app.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.theme.FormUpColors
import java.util.Locale

@Composable
internal fun PlayerStatsCard(player: PlayerStatEntry, expanded: Boolean, onExpand: () -> Unit, onChange: (String, Double) -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = FormUpColors.Surface) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            TextButton(onExpand, Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(player.name, style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
                    Text(stringResource(R.string.mod_player_summary, player.goals, player.assists, player.minutesPlayed), style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    stringResource(if (expanded) R.string.mod_collapse_stats else R.string.mod_expand_stats, player.name))
            }
            if (expanded) {
                StatMetricRow(stringResource(R.string.stats_goals_scored), player.goals.toString(), { onChange("goals", -1.0) }, { onChange("goals", 1.0) }, player.name)
                StatMetricRow(stringResource(R.string.assists), player.assists.toString(), { onChange("assists", -1.0) }, { onChange("assists", 1.0) }, player.name)
                StatMetricRow(stringResource(R.string.mod_minutes), player.minutesPlayed.toString(), { onChange("minutes", -5.0) }, { onChange("minutes", 5.0) }, player.name)
                StatMetricRow(stringResource(R.string.mod_rating), String.format(Locale.getDefault(), "%.1f", player.rating), { onChange("rating", -.5) }, { onChange("rating", .5) }, player.name)
            }
        }
    }
}
@Composable
internal fun StatMetricRow(label: String, value: String, onDecrease: () -> Unit, onIncrease: () -> Unit, player: String = "") {
    val target = if (player.isBlank()) label else "$label · $player"
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextPrimary)
        IconButton(onDecrease, Modifier.size(48.dp)) { Icon(Icons.Default.Remove, stringResource(R.string.mod_decrease, target), tint = FormUpColors.TextSecondary) }
        Text(value, Modifier.widthIn(min = 32.dp).semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.titleMedium, color = FormUpColors.Primary)
        IconButton(onIncrease, Modifier.size(48.dp)) { Icon(Icons.Default.Add, stringResource(R.string.mod_increase, target), tint = FormUpColors.Primary) }
    }
}
