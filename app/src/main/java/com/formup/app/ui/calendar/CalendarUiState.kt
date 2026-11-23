// Defines calendar dates, event cards and selection state.

package com.formup.app.ui.calendar

data class CalendarDay(
    val date: Int,
    val dayLetter: String,
    val isInCurrentMonth: Boolean = true,
    val marker: DayMarker? = null
)

enum class DayMarker { MATCH, TRAINING, ACTION }

sealed class CalendarEvent {
    abstract val id: String
    abstract val dateIso: String

    data class Match(
        override val id: String,
        override val dateIso: String,
        val opponent: String,
        val dateLabel: String,
        val timeText: String,
        val location: String,
        val formation: String? = null
    ) : CalendarEvent()

    data class Training(
        override val id: String,
        override val dateIso: String,
        val title: String,
        val dateLabel: String,
        val timeRange: String
    ) : CalendarEvent()
}

data class CalendarUiState(
    val monthLabel: String,
    val weekdayLetters: List<String> = listOf("S", "M", "T", "W", "T", "F", "S"),
    val days: List<CalendarDay>,
    val selectedDate: Int,
    val events: List<CalendarEvent>,
    val monthKey: String = monthLabel
)
