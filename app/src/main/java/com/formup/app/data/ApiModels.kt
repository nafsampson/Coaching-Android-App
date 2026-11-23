// Defines API responses and the values sent when saving changes.

package com.formup.app.data

data class ApiException(
    val code: Int,
    override val message: String
) : Exception(message)

data class SeasonSnapshot(
    val coach: CoachProfile,
    val team: TeamProfile,
    val players: List<Player>,
    val fixtures: List<Fixture>,
    val sessions: List<TrainingSession> = emptyList(),
    val fromCache: Boolean = false
)

data class SquadUpdate(
    val playerId: String,
    val status: String,
    val selection: String,
    val assignedPosition: String = "",
    val pitchX: Double? = null,
    val pitchY: Double? = null
)

data class FormationSlot(
    val slotId: String,
    val label: String,
    val x: Double,
    val y: Double
)

data class SavedFormation(
    val id: String,
    val name: String,
    val code: String,
    val positions: List<FormationSlot>
)

data class AttendanceRecord(
    val playerId: String,
    val status: String
)

data class StatUpdate(
    val playerId: String,
    val goals: Int,
    val assists: Int,
    val cleansheet: Boolean,
    val minutesPlayed: Int,
    val rating: Double
)
