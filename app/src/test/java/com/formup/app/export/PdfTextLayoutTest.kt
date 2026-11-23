// Checks that PDF text wraps without clipping or losing names.

package com.formup.app.export

import org.junit.Assert.*
import org.junit.Test

class PdfTextLayoutTest {
    private val measure: (String) -> Float = { it.length.toFloat() }

    @Test fun longNamesWrapWithoutLosingWords() {
        val name = "Thandolwethu Alexandra Ndlovu-Mokoena"
        val lines = PdfTextLayout.wrap(name, 14f, measure)
        assertTrue(lines.all { it.length <= 14 })
        assertEquals(name.replace(" ", ""), lines.joinToString("").replace(" ", ""))
    }

    @Test fun unbrokenLongNameIsNotClipped() {
        val name = "Ndlovu".repeat(70)
        val lines = PdfTextLayout.wrap(name, 18f, measure)
        assertTrue(lines.all { it.length <= 18 })
        assertEquals(name, lines.joinToString(""))
    }

    @Test fun explicitParagraphBreaksSurviveWrapping() {
        assertEquals(listOf("First", "", "Second"), PdfTextLayout.wrap("First\n\nSecond", 20f, measure))
    }

    @Test fun invalidColumnWidthFailsInsteadOfLooping() {
        try { PdfTextLayout.wrap("Name", 0f, measure); fail("Expected invalid width") }
        catch (_: IllegalArgumentException) { }
    }
}
