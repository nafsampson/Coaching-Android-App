// Shows the selected calendar date and its matches and training sessions.

package com.formup.app.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.components.FormUpBottomBar
import com.formup.app.ui.home.components.FormUpTopBar
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.theme.FormUpColors

@Composable
fun CalendarScreen(
    state: CalendarUiState,
    hasUnread: Boolean = false,
    onSelectDate: (Int) -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onManageTrainingOrLineup: () -> Unit = {},
    onViewMatchDetails: (String) -> Unit = {},
    onEditLineup: (String) -> Unit = {},
    onEditMatch: (String) -> Unit = {},
    onDeleteMatch: (String) -> Unit = {},
    onExportLineup: (String) -> Unit = {},
    onEditSession: (String) -> Unit = {},
    onDeleteSession: (String) -> Unit = {},
    onViewTeamAttendance: (CalendarEvent.Training) -> Unit = {},
    onNotifications: () -> Unit = {},
    onSelectTab: (HomeTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedDate = state.selectedDate

    val selectedEvents = state.events.filter { com.formup.app.data.ServerDates.localDate(it.dateIso)?.dayOfMonth == selectedDate }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FormUpColors.Background,
        topBar = { FormUpTopBar(hasUnread = hasUnread, onNotifications = onNotifications) },        bottomBar = { FormUpBottomBar(selected = HomeTab.Calendar, onSelect = onSelectTab) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.monthLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = FormUpColors.Primary,
                        modifier = Modifier.weight(1f)
                    )
                    CalendarMonthNavButton(
                        Icons.Filled.ChevronLeft,
                        stringResource(R.string.calendar_previous_month),
                        onPreviousMonth
                    )
                    Spacer(Modifier.size(8.dp))
                    CalendarMonthNavButton(
                        Icons.Filled.ChevronRight,
                        stringResource(R.string.calendar_next_month),
                        onNextMonth
                    )
                }
            }

            item {
                CalendarMonthGrid(
                    weekdayLetters = listOf(
                        stringResource(R.string.calendar_sunday),
                        stringResource(R.string.calendar_monday),
                        stringResource(R.string.calendar_tuesday),
                        stringResource(R.string.calendar_wednesday),
                        stringResource(R.string.calendar_thursday),
                        stringResource(R.string.calendar_friday),
                        stringResource(R.string.calendar_saturday)
                    ),
                    days = state.days,
                    selectedDate = selectedDate,
                    onSelectDate = onSelectDate
                )
            }

            item { CalendarManageEventPill(onClick = onManageTrainingOrLineup) }

            item {
                Text(
                    stringResource(R.string.calendar_events_for_day, selectedDate),
                    style = MaterialTheme.typography.headlineMedium,
                    color = FormUpColors.TextPrimary
                )
            }

            if (selectedEvents.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.calendar_empty),
                        color = FormUpColors.TextSecondary
                    )
                }
            }

            items(selectedEvents, key = { it.id }) { event ->
                when (event) {
                    is CalendarEvent.Match -> MatchEventCard(
                        event = event,
                        onViewDetails = { onViewMatchDetails(event.id) },
                        onEditLineup = { onEditLineup(event.id) },
                        onEdit = { onEditMatch(event.id) },
                        onDelete = { onDeleteMatch(event.id) },
                        onExportPdf = { onExportLineup(event.id) }
                    )

                    is CalendarEvent.Training -> TrainingEventCard(
                        event = event,
                        onViewTeamAttendance = { onViewTeamAttendance(event) },
                        onEdit = { onEditSession(event.id) },
                        onDelete = { onDeleteSession(event.id) }
                    )
                }
            }
        }
    }
}
