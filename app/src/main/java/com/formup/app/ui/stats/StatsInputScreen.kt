// Collects match scores and expandable player statistics.

package com.formup.app.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.components.*
import com.formup.app.ui.theme.FormUpColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsInputScreen(state: StatsInputUiState, onBack: () -> Unit = {}, onSelectFixture: (String) -> Unit = {},
    onSave: (List<PlayerStatEntry>, Int, Int) -> Unit = { _, _, _ -> }, onNotifications: () -> Unit = {},
    onSelectTab: (HomeTab) -> Unit = {}, modifier: Modifier = Modifier, hasUnread: Boolean = false) {
    val rows = remember(state.fixtureId, state.players) { mutableStateListOf<PlayerStatEntry>().apply { addAll(state.players) } }
    var teamScore by rememberSaveable(state.fixtureId, state.homeScore) { mutableIntStateOf(state.homeScore) }
    var opponentScore by rememberSaveable(state.fixtureId, state.awayScore) { mutableIntStateOf(state.awayScore) }
    var expanded by rememberSaveable(state.fixtureId) { mutableStateOf<String?>(null) }
    var picker by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    Scaffold(modifier.fillMaxSize().imePadding(), containerColor = FormUpColors.Background,
        topBar = { FormUpTopBar(hasUnread, onNotifications) }, bottomBar = {
            Column {
                Surface(color = FormUpColors.Surface) {
                    com.formup.app.ui.common.FormUpButton({ focus.clearFocus(); onSave(rows.toList(), teamScore, opponentScore) },
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).heightIn(min = 52.dp), enabled = state.availableFixtures.isNotEmpty()) {
                        Text(stringResource(R.string.stats_save_statistics))
                    }
                }
                FormUpBottomBar(HomeTab.Stats, onSelectTab)
            }
        }) { inner ->
        LazyColumn(Modifier.fillMaxSize().padding(inner), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item {
                TextButton(onBack) { Text(stringResource(R.string.ui_back)) }
                Text(stringResource(R.string.mod_match_centre), style = MaterialTheme.typography.headlineLarge, color = FormUpColors.TextPrimary)
                Text(stringResource(R.string.mod_stats_intro), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
            }
            item {
                ExposedDropdownMenuBox(picker, { picker = it }) {
                    OutlinedTextField(state.availableFixtures.firstOrNull { it.id == state.fixtureId }?.label ?: state.opponent,
                        {}, readOnly = true, label = { Text(stringResource(R.string.mod_choose_fixture)) }, modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(picker) })
                    ExposedDropdownMenu(picker, { picker = false }) {
                        state.availableFixtures.forEach { fixture -> DropdownMenuItem(text = { Text(fixture.label) },
                            onClick = { picker = false; onSelectFixture(fixture.id) }) }
                    }
                }
            }
            if (state.availableFixtures.isEmpty()) item { SectionCard { Text(stringResource(R.string.msg_no_fixtures_to_log), color = FormUpColors.TextSecondary) } }
            else {
                item {
                    SectionCard(contentPadding = PaddingValues(16.dp)) {
                        StatMetricRow(state.homeLabel, teamScore.toString(), { teamScore = (teamScore - 1).coerceAtLeast(0) }, { teamScore = (teamScore + 1).coerceAtMost(99) })
                        HorizontalDivider(color = FormUpColors.Hairline)
                        StatMetricRow(state.awayLabel, opponentScore.toString(), { opponentScore = (opponentScore - 1).coerceAtLeast(0) }, { opponentScore = (opponentScore + 1).coerceAtMost(99) })
                    }
                    Text(stringResource(R.string.mod_score_hint), style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary, modifier = Modifier.padding(top = 8.dp))
                }
                item { Text(stringResource(R.string.mod_player_performance), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary) }
                items(rows.size, key = { rows[it].id }) { index ->
                    val player = rows[index]
                    PlayerStatsCard(player, expanded == player.id, { expanded = if (expanded == player.id) null else player.id }) { field, delta ->
                        rows[index] = when (field) {
                            "goals" -> { val goals = (player.goals + delta.toInt()).coerceAtLeast(0); teamScore = (teamScore + goals - player.goals).coerceIn(0, 99); player.copy(goals = goals) }
                            "assists" -> player.copy(assists = (player.assists + delta.toInt()).coerceAtLeast(0))
                            "minutes" -> player.copy(minutesPlayed = (player.minutesPlayed + delta.toInt()).coerceIn(0, 120))
                            else -> player.copy(rating = (player.rating + delta).coerceIn(0.0, 10.0))
                        }
                    }
                }
            }
        }
    }
}
