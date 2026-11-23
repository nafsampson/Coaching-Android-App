// Shows the roster with search, availability filters and player actions.

package com.formup.app.ui.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.components.*
import com.formup.app.ui.team.components.PlayerRow
import com.formup.app.ui.theme.FormUpColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(state: TeamUiState, onNotifications: () -> Unit = {}, onAddPlayer: () -> Unit = {},
    onEditPlayer: (String) -> Unit = {}, onDeletePlayer: (String) -> Unit = {}, onSelectTab: (HomeTab) -> Unit = {}, modifier: Modifier = Modifier, hasUnread: Boolean = false) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<SquadPlayer?>(null) }
    var pendingDelete by remember { mutableStateOf<SquadPlayer?>(null) }
    val focus = LocalFocusManager.current
    val labels = listOf(stringResource(R.string.mod_all_players), stringResource(R.string.fit), stringResource(R.string.doubtful), stringResource(R.string.out))
    val visible = state.players.filter { player ->
        (query.isBlank() || player.fullName.contains(query.trim(), ignoreCase = true)) &&
            (filter == 0 || player.status == listOf(PlayerStatus.FIT, PlayerStatus.DOUBT, PlayerStatus.OUT)[filter - 1])
    }
    Scaffold(modifier.fillMaxSize().imePadding(), containerColor = FormUpColors.Background,
        topBar = { FormUpTopBar(hasUnread, onNotifications) }, bottomBar = { FormUpBottomBar(HomeTab.Team, onSelectTab) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onAddPlayer, containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton,
            icon = { Icon(Icons.Default.PersonAdd, null) }, text = { Text(stringResource(R.string.team_add_player)) }) }) { inner ->
        LazyColumn(Modifier.fillMaxSize().padding(inner), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(stringResource(R.string.mod_squad_title), style = MaterialTheme.typography.headlineLarge, color = FormUpColors.TextPrimary)
                Text(stringResource(R.string.mod_squad_subtitle), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SquadNumber(state.total, stringResource(R.string.mod_registered), Modifier.weight(1f))
                    SquadNumber(state.fit, stringResource(R.string.fit), Modifier.weight(1f))
                    SquadNumber(state.out, stringResource(R.string.out), Modifier.weight(1f))
                }
            }
            item {
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text(stringResource(R.string.team_search_players)) }, leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Default.Close, stringResource(R.string.mod_clear_search)) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }))
            }
            item {
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(labels.size) { index -> FilterChip(filter == index, { filter = index }, label = { Text(labels[index]) }, modifier = Modifier.heightIn(min = 48.dp)) }
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.mod_next_fixture_status), style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
            }
            if (visible.isEmpty()) item {
                SectionCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(if (state.players.isEmpty()) R.string.team_no_players else R.string.team_no_search_results), style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
                    TextButton(if (state.players.isEmpty()) onAddPlayer else { { query = ""; filter = 0 } }) {
                        Text(stringResource(if (state.players.isEmpty()) R.string.team_add_player else R.string.mod_reset_filters))
                    }
                }
            }
            items(visible, key = { it.id }) { player -> PlayerRow(player, onClick = { selected = player }) }
        }
    }
    selected?.let { player ->
        ModalBottomSheet(onDismissRequest = { selected = null }, containerColor = FormUpColors.Surface) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(player.fullName, style = MaterialTheme.typography.headlineMedium, color = FormUpColors.TextPrimary)
                Text(stringResource(R.string.team_edit_remove_hint), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
                com.formup.app.ui.common.FormUpButton({ selected = null; onEditPlayer(player.id) }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(stringResource(R.string.team_edit)) }
                OutlinedButton({ pendingDelete = player; selected = null }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(stringResource(R.string.team_remove), color = FormUpColors.Danger) }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
    pendingDelete?.let { player -> AlertDialog(onDismissRequest = { pendingDelete = null },
        title = { Text(stringResource(R.string.team_remove_player_title, player.fullName)) }, text = { Text(stringResource(R.string.team_remove_player_message)) },
        confirmButton = { TextButton({ pendingDelete = null; onDeletePlayer(player.id) }) { Text(stringResource(R.string.team_remove), color = FormUpColors.Danger) } },
        dismissButton = { TextButton({ pendingDelete = null }) { Text(stringResource(R.string.ui_cancel)) } }) }
}
@Composable
private fun SquadNumber(number: Int, label: String, modifier: Modifier) {
    SectionCard(modifier, PaddingValues(16.dp)) {
        Text(number.toString(), style = MaterialTheme.typography.headlineLarge, color = FormUpColors.Primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
    }
}
