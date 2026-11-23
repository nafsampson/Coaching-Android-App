// Shows the latest match result and report action.

package com.formup.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.LastMatch
import com.formup.app.ui.theme.FormUpColors

@Composable
fun LastMatchCard(match: LastMatch, onViewAll: () -> Unit, modifier: Modifier = Modifier) {
    SectionCard(modifier.fillMaxWidth(), PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.last_match), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary)
            if (match.exists) TextButton(onViewAll, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.mod_match_report)) }
        }
        if (!match.exists) Text(stringResource(R.string.no_match_played), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
        else {
            Spacer(Modifier.height(12.dp))
            Surface(color = FormUpColors.SurfaceRaised, shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(match.homeTeam, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text("${match.homeScore} : ${match.awayScore}", style = MaterialTheme.typography.headlineLarge, color = FormUpColors.TextPrimary)
                        Text(stringResource(R.string.mod_full_time), style = MaterialTheme.typography.labelSmall, color = FormUpColors.TextSecondary)
                    }
                    Text(match.awayTeam, Modifier.weight(1f), textAlign = TextAlign.End, style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
                }
            }
        }
    }
}
