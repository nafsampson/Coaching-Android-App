// Checks starter limits and availability restrictions.

package com.formup.app.ui

import com.formup.app.data.ApiValues
import com.formup.app.data.AvailabilityStatus
import org.junit.Assert.*
import org.junit.Test

class LineupRulesTest {
    private val none = ApiValues.SELECTION_NOT_SELECTED
    private val start = ApiValues.SELECTION_STARTING
    private val sub = ApiValues.SELECTION_SUBSTITUTE

    @Test fun twelfthStarterIsBlocked() {
        assertEquals(SelectionProblem.FULL, selectionProblem(sub, start, AvailabilityStatus.Fit, 11))
    }
    @Test fun substituteCanBeSelectedWhenElevenStartersExist() {
        assertNull(selectionProblem(none, sub, AvailabilityStatus.Fit, 11))
    }
    @Test fun unavailablePlayerCannotStartOrSubstitute() {
        assertEquals(SelectionProblem.OUT, selectionProblem(none, start, AvailabilityStatus.Out, 3))
        assertEquals(SelectionProblem.OUT, selectionProblem(none, sub, AvailabilityStatus.Out, 11))
    }
    @Test fun unavailablePlayerCanAlwaysBeRemoved() {
        assertNull(selectionProblem(start, none, AvailabilityStatus.Out, 11))
    }
    @Test fun doubtfulPlayerIsStillCoachesChoice() {
        assertNull(selectionProblem(none, start, AvailabilityStatus.Doubtful, 10))
    }
    @Test fun keepingAnExistingStarterDoesNotCountAsTwelfthPlayer() {
        assertNull(selectionProblem(start, start, AvailabilityStatus.Fit, 11))
    }
}
