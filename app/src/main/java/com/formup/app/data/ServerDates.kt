// Converts server timestamps into local dates and display labels.

package com.formup.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object ServerDates {
    fun instant(value: String): Instant? {
        if (value.isBlank()) return null
        return runCatching { Instant.parse(value) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
            // Treat timestamps without an offset as UTC, matching the API.
            ?: runCatching { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC) }.getOrNull()
    }
    fun localDate(value: String, zone: ZoneId = ZoneId.systemDefault()): LocalDate? = instant(value)?.atZone(zone)?.toLocalDate()
    fun isInMonth(value: String, month: YearMonth, zone: ZoneId = ZoneId.systemDefault()): Boolean =
        localDate(value, zone)?.let { YearMonth.from(it) == month } == true
    fun labels(value: String, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): Pair<String, String> {
        val date = instant(value)?.atZone(zone) ?: return "—" to "—"
        return date.format(DateTimeFormatter.ofPattern("MMM dd", locale)) to date.format(DateTimeFormatter.ofPattern("HH:mm", locale))
    }
}
