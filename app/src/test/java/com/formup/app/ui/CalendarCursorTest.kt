// Checks date selection, year changes and shorter months.

package com.formup.app.ui

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class CalendarCursorTest {
    @Test fun selectingDayKeepsTheBrowsedMonthAndYear() {
        assertEquals(LocalDate.of(2027, 2, 21), CalendarCursor(LocalDate.of(2027, 2, 1)).select(21).date)
    }
    @Test fun monthNavigationCrossesYearsWithoutLosingTheDay() {
        assertEquals(LocalDate.of(2027, 1, 21), CalendarCursor(LocalDate.of(2026, 12, 21)).moveMonths(1).date)
    }
    @Test fun shortMonthClampsToAValidDay() {
        assertEquals(LocalDate.of(2027, 2, 28), CalendarCursor(LocalDate.of(2027, 1, 31)).moveMonths(1).date)
    }
    @Test fun leapMonthAllowsTheTwentyNinth() {
        assertEquals(LocalDate.of(2028, 2, 29), CalendarCursor(LocalDate.of(2028, 3, 31)).moveMonths(-1).date)
    }
}
