// Defines attendance totals, roster rows and editing state.

package com.formup.app.ui.calendar

enum class RosterStatus(val label: String) {
    NO_REPLY("NOT RECORDED"),
    ABSENT("ABSENT"),
    ATTENDING("PRESENT")
}

enum class RosterRowAction { MARK_PRESENT, NONE }

data class RosterEntry(
    val id: String,
    val name: String,
    val subtitle: String,
    val statusBadge: String? = null,
    val status: RosterStatus,
    val action: RosterRowAction = RosterRowAction.NONE
)

data class AttendanceUiState(
    val dateTimeLabel: String,
    val eventTitle: String,
    val squadSize: Int,
    val attending: Int,
    val absentCount: Int,
    val noReplyCount: Int,
    val roster: List<RosterEntry>,
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    val canEdit: Boolean = true,
    val notice: String? = null
)
