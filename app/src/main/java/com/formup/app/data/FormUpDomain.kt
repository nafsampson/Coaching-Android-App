// Defines players, fixtures, profiles and match statistics.

package com.formup.app.data

enum class AvailabilityStatus(val label: String, val apiValue: String) {
    Fit("Fit", "Available"),
    Doubtful("Doubtful", "Knock"),
    Out("Out", "Injured");

    companion object {

        fun fromApi(value: String?): AvailabilityStatus =
            values().firstOrNull {
                it.apiValue.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true)
            } ?: Fit
    }
}

data class MatchLine(
    val goals: Int = 0,
    val assists: Int = 0,
    val minutes: Int = 0,
    val rating: Double = 0.0,
    val cleansheet: Boolean = false
) {

    val participated: Boolean
        get() = minutes > 0 || goals > 0 || assists > 0 || rating > 0.0
}

data class Player(
    val id: String,
    val name: String,
    val position: String,
    val dateOfBirth: String? = null,
    val status: AvailabilityStatus = AvailabilityStatus.Fit,
)

data class Fixture(
    val id: String,
    val opponent: String,
    val dateLabel: String,
    val kickoff: String,
    val venue: String,
    val isHome: Boolean = true,
    val played: Boolean = false,
    val teamScore: Int = 0,
    val opponentScore: Int = 0,
    val playerStats: Map<String, MatchLine> = emptyMap(),
    val squad: Map<String, SquadUpdate> = emptyMap(),
    val matchDateIso: String = "",
    val formationName: String = "",
    val formationCode: String = ""
) {

    val resultLetter: String
        get() = when {
            !played -> "-"
            teamScore > opponentScore -> "W"
            teamScore < opponentScore -> "L"
            else -> "D"
        }

    val scoreLine: String
        get() = "$teamScore-$opponentScore"

    val resultText: String
        get() =
            if (played) {
                "$resultLetter $scoreLine"
            } else {
                kickoff
            }
}

data class ActivityEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: String,
    val isTraining: Boolean
)

enum class NotificationKind {
    Attendance,
    Schedule,
    Fitness,
    Stats
}

data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val timestamp: String,
    val body: String,
    val isRead: Boolean = false,
    val actionLabel: String? = null,
    val fixtureId: String? = null
)

data class CoachProfile(
    val name: String,
    val languageCode: String,
    val matchReminders: Boolean,
    val weeklySummary: Boolean
)

data class TeamProfile(
    val name: String,
    val squad: String,
    val ageGroup: Int? = null
)

object ApiValues {
    const val VENUE_HOME = "Home"
    const val VENUE_AWAY = "Away"
    const val SELECTION_STARTING = "Starting"
    const val SELECTION_SUBSTITUTE = "Substitute"
    const val SELECTION_NOT_SELECTED = "NotSelected"
    const val NOT_RECORDED = "NotRecorded"
    const val ATTENDANCE_PRESENT = "Present"
    const val ATTENDANCE_ABSENT = "Absent"
}

data class TrainingSession(
    val id: String,
    val dateLabel: String,
    val time: String,
    val notes: String?,
    val sessionDateIso: String
)
