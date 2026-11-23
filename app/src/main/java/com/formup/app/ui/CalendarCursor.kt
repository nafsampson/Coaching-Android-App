// Keeps the selected calendar date valid when changing months.

package com.formup.app.ui

import java.time.LocalDate
import java.time.YearMonth

internal data class CalendarCursor(val date: LocalDate) {
    val month: YearMonth get() = YearMonth.from(date)
    fun select(day: Int) = copy(date = month.atDay(day))
    fun moveMonths(delta: Long) = copy(date = date.plusMonths(delta))
}
