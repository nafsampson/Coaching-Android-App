// Shows the coach dashboard, upcoming match and squad summary.

package com.formup.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.components.AvailabilityCard
import com.formup.app.ui.home.components.FormUpBottomBar
import com.formup.app.ui.home.components.FormUpTopBar
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.home.components.LastMatchCard
import com.formup.app.ui.home.components.NextMatchCard
import com.formup.app.ui.home.components.QuickActionsRow
import com.formup.app.ui.theme.FormUpColors

@Composable

fun HomeScreen(
    state: HomeUiState,
    onNotifications: () -> Unit = {},
    onStats: () -> Unit = {},
    onMatchDetails: () -> Unit = {},
    onLineup: () -> Unit = {},
    onAttendance: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onSelectTab: (HomeTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAvailabilityInfo by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FormUpColors.Background,
        topBar = {
            FormUpTopBar(
                hasUnread = state.unreadNotifications,
                onNotifications = onNotifications
            )
        },
        bottomBar = {
            FormUpBottomBar(selected = HomeTab.Home, onSelect = onSelectTab)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                val availability = state.availability

                val mood = if (availability.doubtful + availability.out <= 2) {
                    stringResource(R.string.availability_healthy)
                } else {
                    stringResource(R.string.availability_thin)
                }

                val prompt = if (state.nextMatch.exists) {
                    stringResource(
                        R.string.home_prompt_next,
                        state.nextMatch.opponent.removePrefix("vs. "),
                        state.nextMatch.date,
                        mood
                    )
                } else {
                    stringResource(R.string.home_prompt_empty)
                }

                Greeting(name = state.coachName, prompt = prompt)
            }
            item {
                NextMatchCard(
                    match = state.nextMatch,
                    onMatchDetails = onMatchDetails,
                    onLineup = onLineup,
                    onSchedule = { onSelectTab(HomeTab.Calendar) }
                )
            }
            item {
                QuickActionsRow(
                    onAttendance = onAttendance,
                    onSelection = onLineup,
                    onStats = onStats
                )
            }
            item {
                AvailabilityCard(
                    availability = state.availability,
                    onInfo = { showAvailabilityInfo = true }
                )
            }
            item {
                LastMatchCard(
                    match = state.lastMatch,
                    onViewAll = onViewResults
                )
            }
        }
    }
    if (showAvailabilityInfo) {
        val availability = state.availability
        AlertDialog(
            onDismissRequest = { showAvailabilityInfo = false },
            confirmButton = {
                TextButton(onClick = {
                    showAvailabilityInfo = false
                    onSelectTab(HomeTab.Team)
                }) {
                    Text(stringResource(R.string.view_squad), color = FormUpColors.PrimaryDeep)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvailabilityInfo = false }) {
                    Text(stringResource(R.string.close), color = FormUpColors.TextSecondary)
                }
            },
            containerColor = FormUpColors.Surface,
            title = {
                Text(
                    stringResource(R.string.squad_availability_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.squad_availability_message,
                        availability.fit,
                        availability.doubtful,
                        availability.out,
                        availability.total
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FormUpColors.TextSecondary
                )
            }
        )
    }
}

@Composable
private fun Greeting(name: String, prompt: String) {
    androidx.compose.foundation.layout.Column {
        Text(stringResource(R.string.mod_workspace), style = MaterialTheme.typography.labelLarge, color = FormUpColors.Primary)
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.welcome_back, name),
            style = MaterialTheme.typography.headlineMedium,
            color = FormUpColors.TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = FormUpColors.TextSecondary
        )
    }
}
