// Checks that attendance remains a draft until confirmation.

package com.formup.app.ui

import org.junit.Assert.*
import org.junit.Test

class AttendanceDraftTest {
    @Test fun markingIsOnlyADraftAndDoesNotMutateConfirmedValues() {
        val confirmed = mapOf("a" to "Absent")
        val draft = AttendanceDraft().stage(confirmed, mapOf("a" to "Present"))
        assertEquals("Absent", confirmed["a"])
        assertEquals("Present", draft.resolve(confirmed)["a"])
        assertTrue(draft.hasChanges)
    }
    @Test fun revertingAMarkRemovesUnnecessaryConfirmation() {
        val base = mapOf("a" to "Present")
        val draft = AttendanceDraft().stage(base, mapOf("a" to "Absent")).stage(base, mapOf("a" to "Present"))
        assertFalse(draft.hasChanges)
    }
    @Test fun manyMarksProduceOneCompleteBatch() {
        val base = mapOf("c" to "Present")
        val draft = AttendanceDraft().stage(base, mapOf("a" to "Present")).stage(base, mapOf("b" to "Absent"))
        assertEquals(mapOf("a" to "Present", "b" to "Absent", "c" to "Present"), draft.resolve(base))
    }
    @Test fun confirmingUsesLatestUneditedValues() {
        val draft = AttendanceDraft().stage(emptyMap(), mapOf("a" to "Present"))
        assertEquals(mapOf("a" to "Present", "b" to "Absent"), draft.resolve(mapOf("b" to "Absent")))
    }
    @Test fun discardingLeavesConfirmedRecordsUntouched() {
        val base = mapOf("a" to "Absent")
        AttendanceDraft().stage(base, mapOf("a" to "Present"))
        assertEquals(base, AttendanceDraft().resolve(base))
    }
}
