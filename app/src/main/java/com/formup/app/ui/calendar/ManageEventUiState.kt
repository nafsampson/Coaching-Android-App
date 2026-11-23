// Defines the editable values for match and training forms.

package com.formup.app.ui.calendar

import java.time.LocalTime

internal object EventTimeDefaults {
    const val TRAINING = "17:30"
    const val MATCH = "12:30"

    fun forKind(kind: EventKind): String = if (kind == EventKind.TRAINING) TRAINING else MATCH

    fun pickerTime(kind: EventKind, savedTime: String): LocalTime =
        runCatching { LocalTime.parse(savedTime) }.getOrElse { LocalTime.parse(forKind(kind)) }
}

enum class EventKind {
    TRAINING,
    MATCH
}

data class TrainingFormState(
    val date: String = "",
    val time: String = EventTimeDefaults.TRAINING,
    val notes: String = "",
    val dateError: String? = null,
    val timeError: String? = null
)

data class MatchFormState(
    val opponent: String = "",
    val venue: String = "Home",
    val date: String = "",
    val time: String = EventTimeDefaults.MATCH,
    val opponentError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null
)
