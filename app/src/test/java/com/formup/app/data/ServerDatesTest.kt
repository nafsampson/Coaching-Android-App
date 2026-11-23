// Checks timestamp parsing, time zones and calendar month boundaries.

package com.formup.app.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class ServerDatesTest {
    private val zone = ZoneId.of("Africa/Johannesburg")
    @Test fun sameDayAndMonthInAnotherYearDoesNotMatch() {
        assertFalse(ServerDates.isInMonth("2026-11-17T10:00:00Z", YearMonth.of(2027, 11), zone))
        assertTrue(ServerDates.isInMonth("2027-11-17T10:00:00Z", YearMonth.of(2027, 11), zone))
    }
    @Test fun utcTimestampCrossesMonthInDeviceTimeZone() {
        assertEquals(LocalDate.of(2027, 1, 1), ServerDates.localDate("2026-12-31T23:00:00Z", zone))
        assertTrue(ServerDates.isInMonth("2026-12-31T23:00:00Z", YearMonth.of(2027, 1), zone))
    }
    @Test fun fractionalSecondsAndExplicitOffsetsRepresentSameInstant() {
        assertEquals(ServerDates.instant("2027-01-01T10:00:00.1234567Z"), ServerDates.instant("2027-01-01T12:00:00.1234567+02:00"))
    }
    @Test fun offsetFreeBackendDatesHaveExplicitUtcMeaning() {
        assertEquals(ServerDates.instant("2027-01-01T10:00:00Z"), ServerDates.instant("2027-01-01T10:00:00"))
    }
    @Test fun invalidDateDoesNotBecomeJanuary1970() {
        assertNull(ServerDates.instant("not-a-date"))
        assertNull(ServerDates.instant("2027-02-30T12:00:00Z"))
        assertEquals("—" to "—", ServerDates.labels("invalid"))
    }
}
