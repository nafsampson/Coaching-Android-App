// Validates squad selections and checks whether a lineup can be exported.

package com.formup.app.ui

import com.formup.app.data.ApiValues
import com.formup.app.data.AvailabilityStatus

internal enum class SelectionProblem { OUT, FULL }

internal fun selectionProblem(current: String, requested: String, availability: AvailabilityStatus, startingCount: Int): SelectionProblem? {
    require(requested in listOf(ApiValues.SELECTION_STARTING, ApiValues.SELECTION_SUBSTITUTE, ApiValues.SELECTION_NOT_SELECTED))
    // Out players cannot be selected, but they can always be removed.
    if (availability == AvailabilityStatus.Out && requested != ApiValues.SELECTION_NOT_SELECTED) return SelectionProblem.OUT
    if (requested == ApiValues.SELECTION_STARTING && current != requested && startingCount >= 11) return SelectionProblem.FULL
    return null
}

internal fun hasLineupSelection(squad: Collection<com.formup.app.data.SquadUpdate>, playerIds: Set<String>) =
    squad.any { it.playerId in playerIds && it.selection in listOf(ApiValues.SELECTION_STARTING, ApiValues.SELECTION_SUBSTITUTE) }
