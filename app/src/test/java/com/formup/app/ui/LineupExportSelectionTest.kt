// Checks which saved player selections allow a lineup export.

package com.formup.app.ui

import com.formup.app.data.SquadUpdate
import com.formup.app.data.ApiValues
import org.junit.Assert.*
import org.junit.Test

class LineupExportSelectionTest {
    @Test fun emptyLineupHasNoExport() { assertFalse(hasLineupSelection(emptyList(), setOf("a"))) }
    @Test fun startersCanExportWithoutAPlayedMatch() { assertTrue(hasLineupSelection(listOf(SquadUpdate("a", "Available", ApiValues.SELECTION_STARTING)), setOf("a"))) }
    @Test fun benchOnlySelectionIsStillASelection() { assertTrue(hasLineupSelection(listOf(SquadUpdate("a", "Available", ApiValues.SELECTION_SUBSTITUTE)), setOf("a"))) }
    @Test fun deletedPlayersAndUnselectedRowsDoNotCreateAnEmptyExport() {
        assertFalse(hasLineupSelection(listOf(SquadUpdate("deleted", "Available", ApiValues.SELECTION_STARTING), SquadUpdate("a", "Available", ApiValues.SELECTION_NOT_SELECTED)), setOf("a")))
    }
}
