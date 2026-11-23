// Provides quick starter, substitute and availability choices.

package com.formup.app.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.data.AvailabilityStatus
import com.formup.app.data.Player
import com.formup.app.ui.theme.FormUpColors

@Composable
internal fun LineupPlayerChoices(player: Player, selection: String, status: AvailabilityStatus, startingCount: Int,
    onSelection: (String) -> Unit, onAvailability: (AvailabilityStatus) -> Unit) {
    val starting = selection == ApiValues.SELECTION_STARTING
    val substitute = selection == ApiValues.SELECTION_SUBSTITUTE
    val canStart = starting || (status != AvailabilityStatus.Out && startingCount < 11)
    val position = when (player.position) {
        "Goalkeeper" -> stringResource(R.string.player_position_goalkeeper)
        "Defender" -> stringResource(R.string.player_position_defender)
        "Midfielder" -> stringResource(R.string.player_position_midfielder)
        "Forward" -> stringResource(R.string.player_position_forward)
        else -> player.position
    }
    Surface(shape = MaterialTheme.shapes.medium, color = FormUpColors.Surface,
        border = BorderStroke(1.dp, if (starting || substitute) FormUpColors.Primary else FormUpColors.Hairline)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .toggleable(starting, enabled = canStart, role = Role.Checkbox,
                    onValueChange = { onSelection(if (it) ApiValues.SELECTION_STARTING else ApiValues.SELECTION_NOT_SELECTED) }),
                verticalAlignment = Alignment.CenterVertically) {
                Checkbox(starting, onCheckedChange = null, enabled = canStart)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                    Text(player.name, style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
                    Text("$position · ${stringResource(if (starting) R.string.lineup_selection_starting else if (substitute) R.string.lineup_selection_substitute else R.string.lineup_selection_not_selected)}",
                        style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val stacked = maxWidth < 260.dp || LocalDensity.current.fontScale > 1.2f
                if (stacked) {
                    Column {
                        AvailabilityChoice(player.name, status, onAvailability, Modifier.fillMaxWidth())
                        SubstituteChoice(substitute, status, onSelection, Modifier.fillMaxWidth())
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        AvailabilityChoice(player.name, status, onAvailability, Modifier.weight(1f))
                        SubstituteChoice(substitute, status, onSelection, Modifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubstituteChoice(selected: Boolean, status: AvailabilityStatus, onSelection: (String) -> Unit, modifier: Modifier) {
    FilterChip(selected = selected,
        onClick = { onSelection(if (selected) ApiValues.SELECTION_NOT_SELECTED else ApiValues.SELECTION_SUBSTITUTE) },
        enabled = selected || status != AvailabilityStatus.Out,
        label = { Text(stringResource(R.string.lineup_selection_substitute)) }, modifier = modifier.heightIn(min = 48.dp))
}

@Composable
private fun AvailabilityChoice(playerName: String, status: AvailabilityStatus, onAvailability: (AvailabilityStatus) -> Unit, modifier: Modifier) {
    val statuses = listOf(AvailabilityStatus.Fit, AvailabilityStatus.Doubtful, AvailabilityStatus.Out)
    val labels = listOf(stringResource(R.string.lineup_status_fit), stringResource(R.string.lineup_status_doubtful), stringResource(R.string.lineup_status_out))
    val availabilityLabel = stringResource(R.string.preferred_availability)
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        TextButton({ expanded = true }, Modifier.heightIn(min = 48.dp).semantics {
            contentDescription = "$playerName, $availabilityLabel: ${labels[statuses.indexOf(status)]}"
        }) {
            Text(labels[statuses.indexOf(status)], color = when (status) {
                AvailabilityStatus.Fit -> FormUpColors.Primary
                AvailabilityStatus.Doubtful -> FormUpColors.Amber
                AvailabilityStatus.Out -> FormUpColors.Danger
            })
            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded, { expanded = false }) {
            statuses.forEachIndexed { index, value ->
                DropdownMenuItem(text = { Text(labels[index]) }, onClick = { expanded = false; onAvailability(value) })
            }
        }
    }
}
