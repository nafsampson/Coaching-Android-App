// Filters the squad and lets the coach select starters and substitutes.

package com.formup.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.data.AvailabilityStatus
import com.formup.app.data.Player
import com.formup.app.ui.common.FormUpButton
import com.formup.app.ui.theme.FormUpColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LineupSelectionScreen(
    opponent: String, players: List<Player>, startingCount: Int, substituteCount: Int,
    selectionOf: (String) -> String, statusOf: (String) -> AvailabilityStatus,
    onBack: () -> Unit, onSelection: (String, String) -> Unit,
    onAvailability: (String, AvailabilityStatus) -> Unit,
    onMarkAllFit: () -> Unit, onClear: () -> Unit, onBuild: () -> Unit
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var roleFilter by rememberSaveable { mutableStateOf(0) }
    val roleValues = listOf("", ApiValues.SELECTION_STARTING, ApiValues.SELECTION_SUBSTITUTE, ApiValues.SELECTION_NOT_SELECTED)
    val filterLabels = listOf(stringResource(R.string.mod_all_players), stringResource(R.string.lineup_selection_starting),
        stringResource(R.string.lineup_selection_substitute), stringResource(R.string.lineup_selection_not_selected))
    val visiblePlayers = players.filter { player ->
        player.name.contains(query.trim(), ignoreCase = true) && (roleFilter == 0 || selectionOf(player.id) == roleValues[roleFilter])
    }
    Scaffold(modifier = Modifier.imePadding(), containerColor = FormUpColors.Background,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.lineup_title, opponent), color = FormUpColors.TextPrimary) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ui_back)) } }) },
        bottomBar = {
            Surface(color = FormUpColors.Surface) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.lineup_count_summary, startingCount, substituteCount),
                        style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
                    FormUpButton(onBuild, Modifier.fillMaxWidth(), enabled = startingCount == 11) {
                        Text(if (startingCount == 11) stringResource(R.string.mod_prepare_lineup)
                            else stringResource(R.string.lineup_select_more, 11 - startingCount))
                    }
                }
            }
        }
    ) { inner ->
        LazyColumn(Modifier.fillMaxSize().padding(inner), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(stringResource(R.string.lineup_starting_xi), style = MaterialTheme.typography.headlineMedium, color = FormUpColors.TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.preferred_lineup_help), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onMarkAllFit, Modifier.weight(1f).heightIn(min = 48.dp)) { Text(stringResource(R.string.lineup_mark_all_fit)) }
                    OutlinedButton(onClear, Modifier.weight(1f).heightIn(min = 48.dp)) { Text(stringResource(R.string.lineup_clear)) }
                }
            }
            item {
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    label = { Text(stringResource(R.string.team_search_players)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Default.Close, stringResource(R.string.mod_clear_search)) } })
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterLabels.size) { index ->
                        FilterChip(roleFilter == index, { roleFilter = index }, label = { Text(filterLabels[index]) },
                            modifier = Modifier.heightIn(min = 48.dp))
                    }
                }
            }
            if (visiblePlayers.isEmpty()) item {
                Text(stringResource(if (players.isEmpty()) R.string.team_no_players else R.string.team_no_search_results),
                    color = FormUpColors.TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
            }
            items(visiblePlayers, key = { it.id }) { player ->
                LineupPlayerChoices(player, selectionOf(player.id), statusOf(player.id), startingCount,
                    { focusManager.clearFocus(); onSelection(player.id, it) }, { onAvailability(player.id, it) })
            }
        }
    }
}
