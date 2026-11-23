// Converts event form dates and times to and from API timestamps.

package com.formup.app.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
// Convert the local event time to a timestamp with an explicit offset.
internal fun createEventIsoDateTime(
    dateText: String,
    timeText: String
): String? {
    return try {

        val date = LocalDate.parse(
            dateText,
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        )

        val time = LocalTime.parse(
            timeText,
            DateTimeFormatter.ofPattern("HH:mm")
        )

        val dateTime = date.atTime(time)

        dateTime
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    } catch (_: DateTimeParseException) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun splitEventIso(iso: String): Pair<String, String> =
    runCatching {
        val local = OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault())
        local.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)) to
                local.format(DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrDefault("" to "")
