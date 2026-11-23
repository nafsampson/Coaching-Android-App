// Builds calendar dates and events from the current season.

package com.formup.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.data.ServerDates
import com.formup.app.ui.calendar.*
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class CalendarPresenter(
    private val state: SeasonState,
    private val application: Application,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    private var cursor by mutableStateOf(CalendarCursor(LocalDate.now(clock)))
    fun previousCalendarMonth() { cursor = cursor.moveMonths(-1) }
    fun nextCalendarMonth() { cursor = cursor.moveMonths(1) }
    fun selectCalendarDay(day: Int) { cursor = cursor.select(day) }
    fun resetCalendarDate() { cursor = CalendarCursor(LocalDate.now(clock)) }
    val calendarState: CalendarUiState
        get() {
            val month = cursor.month
            val today = LocalDate.now(clock)
            // Match the full month and year rather than just the displayed day number.
            val matches = state.fixtureList.filter { ServerDates.isInMonth(it.matchDateIso, month, clock.zone) }
            val sessions = state.sessionList.filter { ServerDates.isInMonth(it.sessionDateIso, month, clock.zone) }
            val events = matches.map { fixture -> CalendarEvent.Match(
                id = fixture.id, dateIso = fixture.matchDateIso, opponent = fixture.opponent,
                dateLabel = fixture.dateLabel, timeText = fixture.kickoff,
                location = fixture.venue.ifBlank { application.getString(R.string.loc_venue_unset) },
                formation = if (state.matchSquadSelections[fixture.id].orEmpty().count { it.value == ApiValues.SELECTION_STARTING } == 11) "Starting XI selected" else null
            ) } + sessions.map { session -> CalendarEvent.Training(
                id = session.id, dateIso = session.sessionDateIso,
                title = session.notes?.takeIf { it.isNotBlank() } ?: application.getString(R.string.loc_training_session),
                dateLabel = session.dateLabel, timeRange = session.time
            ) }
            val days = buildList {
                repeat(month.atDay(1).dayOfWeek.value % 7) { add(CalendarDay(0, "", false)) }
                for (day in 1..month.lengthOfMonth()) {
                    val match = matches.any { ServerDates.localDate(it.matchDateIso, clock.zone)?.dayOfMonth == day }
                    val training = sessions.any { ServerDates.localDate(it.sessionDateIso, clock.zone)?.dayOfMonth == day }
                    add(CalendarDay(day, "", marker = when {
                        match && training -> DayMarker.ACTION
                        match -> DayMarker.MATCH
                        training -> DayMarker.TRAINING
                        else -> null
                    }))
                }
            }
            return CalendarUiState(
                monthLabel = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                days = days, selectedDate = cursor.date.dayOfMonth,
                events = events.sortedBy { ServerDates.instant(it.dateIso) }, monthKey = month.toString()
            )
        }
}
