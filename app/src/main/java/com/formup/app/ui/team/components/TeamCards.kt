// Shows the squad summary and individual player rows.

package com.formup.app.ui.team.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.components.*
import com.formup.app.ui.team.*
import com.formup.app.ui.theme.FormUpColors

@Composable
fun SquadOverviewCard(total: Int, modifier: Modifier = Modifier) {
    SectionCard(modifier) { Text(stringResource(R.string.team_players_count, total), style = MaterialTheme.typography.titleLarge) }
}
@Composable
fun PlayerRow(player: SquadPlayer, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val (label, color) = when (player.status) {
        PlayerStatus.FIT -> stringResource(R.string.fit) to FormUpColors.Primary
        PlayerStatus.DOUBT -> stringResource(R.string.doubtful) to FormUpColors.AmberIcon
        PlayerStatus.OUT -> stringResource(R.string.out) to FormUpColors.Danger
    }
    val position = when (player.position) {
        "Goalkeeper" -> stringResource(R.string.player_position_goalkeeper); "Defender" -> stringResource(R.string.player_position_defender)
        "Midfielder" -> stringResource(R.string.player_position_midfielder); "Forward" -> stringResource(R.string.player_position_forward); else -> player.position
    }
    Surface(onClick, modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = FormUpColors.Surface) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBubble(size = 44.dp) { Text(player.fullName.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }, color = FormUpColors.PrimaryDeep) }
            Column(Modifier.weight(1f)) {
                Text(player.fullName, style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
                Text(position, style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
                Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            }
            Icon(Icons.Default.ChevronRight, null, tint = FormUpColors.TextSecondary)
        }
    }
}
