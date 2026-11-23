// Moves selected players on the pitch using drag controls or sliders.

package com.formup.app.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import com.formup.app.R
import com.formup.app.data.Player
import com.formup.app.data.SquadUpdate
import com.formup.app.ui.builtInFormations
import com.formup.app.ui.theme.FormUpColors
import kotlin.math.roundToInt

@Composable
internal fun InteractiveLineupPitch(players: List<Player>, entryFor: (String) -> SquadUpdate?, onPositionChange: (String, String, Double, Double) -> Unit) {
    var selectedId by remember(players.map { it.id }) { mutableStateOf(players.firstOrNull()?.id) }
    val fallback = builtInFormations.first().positions
    val density = LocalDensity.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(.72f)) {
            FootballPitch(Modifier.fillMaxSize())
            // Convert between screen pixels and saved percentage coordinates.
            val width = with(density) { maxWidth.toPx() }; val height = with(density) { maxHeight.toPx() }
            val showNames = true
            val tokenWidth = with(density) { (if (showNames) 76.dp else 48.dp).toPx() }
            val tokenHeight = with(density) { (if (showNames) 88.dp else 48.dp).toPx() }
            players.forEachIndexed { index, player ->
                val entry = entryFor(player.id)
                val x = entry?.pitchX ?: fallback[index % fallback.size].x
                val y = entry?.pitchY ?: fallback[index % fallback.size].y
                val position = entry?.assignedPosition?.ifBlank { player.position } ?: player.position
                val currentX by rememberUpdatedState(x); val currentY by rememberUpdatedState(y)
                val description = stringResource(R.string.mod_move_player, player.name)
                val leftLabel = stringResource(R.string.mod_move_left); val rightLabel = stringResource(R.string.mod_move_right)
                val upLabel = stringResource(R.string.mod_move_up); val downLabel = stringResource(R.string.mod_move_down)
                fun move(dx: Double, dy: Double) = onPositionChange(player.id, position, (x + dx).coerceIn(5.0,95.0), (y + dy).coerceIn(5.0,95.0))
                JerseyMarker(player.name, player.position.equals("Goalkeeper", true) || position == "GK", player.id == selectedId, showName = showNames,
                    modifier = Modifier.offset { IntOffset((width*x.toFloat()/100 - tokenWidth/2).coerceIn(0f, (width-tokenWidth).coerceAtLeast(0f)).roundToInt(),
                        (height*y.toFloat()/100 - tokenHeight/2).coerceIn(0f,(height-tokenHeight).coerceAtLeast(0f)).roundToInt()) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = description
                            customActions = listOf(
                                CustomAccessibilityAction(leftLabel) { move(-5.0,0.0); true }, CustomAccessibilityAction(rightLabel) { move(5.0,0.0); true },
                                CustomAccessibilityAction(upLabel) { move(0.0,-5.0); true }, CustomAccessibilityAction(downLabel) { move(0.0,5.0); true })
                        }.clickable { selectedId = player.id }.pointerInput(player.id,width,height) {
                            var startX=0.0; var startY=0.0; var dx=0f; var dy=0f
                            detectDragGestures(onDragStart = { selectedId=player.id; startX=currentX; startY=currentY; dx=0f; dy=0f },
                                onDrag = { change, delta -> change.consume(); dx+=delta.x; dy+=delta.y
                                    onPositionChange(player.id,position,(startX+dx/width*100).coerceIn(5.0,95.0),(startY+dy/height*100).coerceIn(5.0,95.0)) })
                        })
            }
        }
        Text(stringResource(R.string.mod_pitch_help), style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players.size) { index -> val player=players[index]
                FilterChip(player.id==selectedId,{selectedId=player.id},label={Text(player.name)},modifier=Modifier.heightIn(min=48.dp)) }
        }
        players.firstOrNull { it.id == selectedId }?.let { player ->
            val index = players.indexOf(player); val entry = entryFor(player.id)
            val x = entry?.pitchX ?: fallback[index % fallback.size].x
            val y = entry?.pitchY ?: fallback[index % fallback.size].y
            val position = entry?.assignedPosition?.ifBlank { player.position } ?: player.position
            Text(player.name, style=MaterialTheme.typography.titleMedium,color=FormUpColors.TextPrimary)
            Text(stringResource(R.string.mod_pitch_horizontal),style=MaterialTheme.typography.bodySmall,color=FormUpColors.TextSecondary)
            val horizontalLabel = stringResource(R.string.mod_horizontal_for, player.name)
            val verticalLabel = stringResource(R.string.mod_vertical_for, player.name)
            Slider(x.toFloat(),{onPositionChange(player.id,position,it.toDouble(),y)},valueRange=5f..95f,
                modifier=Modifier.semantics { contentDescription=horizontalLabel })
            Text(stringResource(R.string.mod_pitch_vertical),style=MaterialTheme.typography.bodySmall,color=FormUpColors.TextSecondary)
            Slider(y.toFloat(),{onPositionChange(player.id,position,x,it.toDouble())},valueRange=5f..95f,
                modifier=Modifier.semantics { contentDescription=verticalLabel })
        }
    }
}
