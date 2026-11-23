// Shows match information, squad availability and the saved lineup.

package com.formup.app.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.home.components.IconBubble
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.home.components.StatusPill
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    state: MatchDetailsUiState,
    onBack: () -> Unit = {},
    onExportLineup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val opponentName = state.title.removePrefix("Match vs. ")

    val translatedTitle = if (state.title.startsWith("Match vs. ")) {
        stringResource(R.string.details_match_title, opponentName)
    } else {
        state.title
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FormUpColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_details), color = FormUpColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_back), tint = FormUpColors.TextPrimary)
                    }
                }
            )
        }
    ) { inner ->
        val translatedVenue = when (state.venueName) {
            "Home" -> stringResource(R.string.event_form_home)
            "Away" -> stringResource(R.string.event_form_away)
            else -> state.venueName
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                com.formup.app.ui.common.ExportButton(stringResource(R.string.review_export_lineup), onExportLineup)
            }
            item {
                Column {
                    Text(translatedTitle, style = MaterialTheme.typography.headlineMedium, color = FormUpColors.Primary)
                }
            }

            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    InfoBlock(Icons.Filled.CalendarMonth, stringResource(R.string.details_date_time), state.dateLabel, state.timeLabel)
                    Spacer(Modifier.height(14.dp))
                    InfoBlock(Icons.Filled.LocationOn, stringResource(R.string.details_location), translatedVenue, state.venueDetail)
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = FormUpColors.Surface,
                    border = BorderStroke(1.5.dp, FormUpColors.Primary)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(stringResource(R.string.details_squad_availability), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(state.inCount.toString(), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = FormUpColors.Primary)
                            Text("/${state.squadSize}", fontSize = 18.sp, color = FormUpColors.TextSecondary)
                            Spacer(Modifier.weight(1f))
                            Text(stringResource(R.string.details_fit), fontFamily = Mono, fontSize = 12.sp, color = FormUpColors.TextSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        SquadAvailabilityBar(inCount = state.inCount, tbdCount = state.tbdCount, outCount = state.outCount)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AvailabilityStat(stringResource(R.string.details_fit), state.inCount, FormUpColors.Primary, Modifier.weight(1f))
                            AvailabilityStat(stringResource(R.string.details_out), state.outCount, FormUpColors.Danger, Modifier.weight(1f))
                            AvailabilityStat(stringResource(R.string.details_doubtful), state.tbdCount, FormUpColors.AmberIcon, Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.details_starting_xi), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary, modifier = Modifier.weight(1f))
                        val translatedFormation = when {
                            state.formation == "Starting XI" ->
                                stringResource(R.string.details_starting_xi)

                            state.formation == "No lineup selected" ->
                                stringResource(R.string.details_no_lineup)

                            state.formation.endsWith("/11 selected") ->
                                stringResource(
                                    R.string.details_players_selected,
                                    state.formation.substringBefore("/").toIntOrNull() ?: 0
                                )

                            else -> state.formation
                        }
                        StatusPill(
                            text = translatedFormation,
                            background = FormUpColors.NavIndicator,
                            contentColor = FormUpColors.TextSecondary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PitchView(players = state.startingXi)
                    Spacer(Modifier.height(16.dp))
                    state.startingXi.forEachIndexed { index, player ->
                        Text(player.name, style = MaterialTheme.typography.bodyMedium,
                            color = FormUpColors.TextPrimary, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }

            item {
                SectionCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)) {
                    Text(stringResource(R.string.details_substitutes), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp))
                    if (state.substitutes.isEmpty()) {
                        Text(
                            stringResource(R.string.details_no_substitutes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = FormUpColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                    state.substitutes.forEachIndexed { index, sub ->
                        if (index > 0) HorizontalDivider(color = FormUpColors.Hairline, modifier = Modifier.padding(horizontal = 14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sub.name, style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary, modifier = Modifier.weight(1f))
                            Text(sub.position, fontFamily = Mono, fontSize = 12.sp, color = FormUpColors.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, primary: String, secondary: String) {
    Row(verticalAlignment = Alignment.Top) {
        IconBubble(size = 36.dp, background = FormUpColors.PrimaryTint) {
            Icon(icon, contentDescription = null, tint = FormUpColors.Primary, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(primary, style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
            if (secondary.isNotBlank()) Text(secondary, style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)        }
    }
}

@Composable
private fun SquadAvailabilityBar(inCount: Int, tbdCount: Int, outCount: Int) {
    val total = (inCount + tbdCount + outCount).coerceAtLeast(1)
    Row(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(FormUpColors.Hairline)
    ) {
        Box(Modifier.weight(inCount.toFloat().coerceAtLeast(0.0001f)).fillMaxSize().background(FormUpColors.Primary))
        Box(Modifier.weight(tbdCount.toFloat().coerceAtLeast(0.0001f)).fillMaxSize().background(FormUpColors.AmberIcon))
        Box(Modifier.weight(outCount.toFloat().coerceAtLeast(0.0001f)).fillMaxSize().background(FormUpColors.Hairline))
    }
}

@Composable
private fun AvailabilityStat(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = FormUpColors.PrimaryTint.copy(alpha = 0.4f)) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontFamily = Mono, fontSize = 12.sp, letterSpacing = 0.5.sp, color = color)
            Spacer(Modifier.height(2.dp))
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary)
        }
    }
}

@Composable
private fun PitchView(players: List<LineupPlayer>) {
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(.72f)) {
        FootballPitch(Modifier.fillMaxSize())
        val showNames = true
        players.forEachIndexed { index, player ->
            JerseyMarker(player.name, player.isGoalkeeper, showName = showNames,
                modifier = Modifier.align(BiasAlignment((player.xPercent * 2 - 1).coerceIn(-.9f,.9f), (player.yPercent * 2 - 1).coerceIn(-.9f,.9f))))
        }
    }
}
