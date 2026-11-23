// Checks formation input, automatic hyphens and cursor positions.

package com.formup.app.ui

import org.junit.Assert.*
import org.junit.Test

class FormationCodeTest {
    @Test fun digitEntryIsAutomaticallySeparated() { assertEquals("4-3-3", FormationCode.format(FormationCode.digits("433"))) }
    @Test fun pastedSeparatorsAndLettersAreNotEditableInput() { assertEquals("433", FormationCode.digits("4-a3 /3")) }
    @Test fun supportedTacticalLinesTotalTenOutfieldPlayers() {
        listOf("4-3-3", "4-4-2", "4-2-3-1", "3-5-2").forEach { assertTrue(it, FormationCode.valid(it)) }
    }
    @Test fun zeroAndIncorrectTotalsCannotBeSaved() {
        listOf("", "433", "4-0-6", "4-3-2", "4-4-4", "4--3-3", "4-3-3abc").forEach { assertFalse(it, FormationCode.valid(it)) }
    }
    @Test fun eachDigitCaretPositionMapsBackToTheSameInputPosition() {
        for (length in 0..10) for (offset in 0..length) {
            val shown = FormationCode.displayOffset(offset, length)
            assertTrue(shown in 0..(length * 2 - 1).coerceAtLeast(0))
            assertEquals(offset, FormationCode.inputOffset(shown, length))
        }
    }
    @Test fun separatorCursorPositionsMapToRealDigitsWithoutOutOfBounds() {
        assertEquals(listOf(0,1,1,2,2,3), (0..5).map { FormationCode.inputOffset(it, 3) })
        assertEquals("4-3", FormationCode.format(FormationCode.digits("43")))
    }
}
