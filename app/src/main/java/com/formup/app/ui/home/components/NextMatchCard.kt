// Shows the next fixture and its match and lineup actions.

package com.formup.app.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.NextMatch
import com.formup.app.ui.theme.FormUpColors

@Composable
fun NextMatchCard(match: NextMatch, onMatchDetails: () -> Unit, onLineup: () -> Unit,
    modifier: Modifier = Modifier, onSchedule: () -> Unit = {}) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = FormUpColors.Ink) {
        Box {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(Color(0xFF35604C).copy(alpha = .25f), size.width * .5f, Offset(size.width, 0f), style = Stroke(32f))
                drawLine(Color.White.copy(alpha = .05f), Offset(size.width * .55f, 0f), Offset(size.width, size.height), 80f)
            }
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = FormUpColors.Lime, shape = RoundedCornerShape(50)) {
                        Text(stringResource(if (match.exists) R.string.next_match_badge else R.string.mod_matchday),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = FormUpColors.Ink, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = FormUpColors.Lime)
                }
                if (match.exists) {
                    Text(match.teamName.ifBlank { stringResource(R.string.mod_your_team) }, color = Color(0xFFB9CFCA), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.mod_vs_team, match.opponent.removePrefix("vs. ")),
                        color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Schedule, null, tint = FormUpColors.Lime, modifier = Modifier.size(20.dp))
                        Text("${match.date} · ${match.kickoff.removeSuffix(" Kickoff")}", color = Color.White,
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFB9CFCA), modifier = Modifier.size(20.dp))
                        Text(when (match.venue) { "Home" -> stringResource(R.string.event_form_home); "Away" -> stringResource(R.string.event_form_away); else -> match.venue },
                            color = Color(0xFFB9CFCA), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    BoxWithConstraints {
                        val large = LocalDensity.current.fontScale > 1.2f || maxWidth < 330.dp
                        if (large) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MatchAction(stringResource(R.string.mod_prepare_lineup), true, onLineup, Modifier.fillMaxWidth(), neon = true)
                            MatchAction(stringResource(R.string.match_details), false, onMatchDetails, Modifier.fillMaxWidth())
                        } else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MatchAction(stringResource(R.string.mod_prepare_lineup), true, onLineup, Modifier.weight(1f), neon = true)
                            MatchAction(stringResource(R.string.match_details), false, onMatchDetails, Modifier.weight(1f))
                        }
                    }
                } else {
                    Text(stringResource(R.string.mod_empty_fixture_title), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.mod_empty_fixture_body), color = Color(0xFFB9CFCA), style = MaterialTheme.typography.bodyMedium)
                    MatchAction(stringResource(R.string.mod_open_calendar), true, onSchedule, Modifier.fillMaxWidth())
                }
            }
        }
    }
}
@Composable
private fun MatchAction(label: String, primary: Boolean, onClick: () -> Unit, modifier: Modifier, neon: Boolean = false) {
    com.formup.app.ui.common.FormUpButton(onClick, modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (primary && neon) FormUpColors.Lime else if (primary) FormUpColors.ButtonFill else Color(0xFF294441),
            contentColor = if (primary && neon) FormUpColors.Ink else if (primary) FormUpColors.OnButton else Color.White), contentPadding = PaddingValues(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
