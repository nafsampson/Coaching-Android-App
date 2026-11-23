// Wraps PDF text so long names fit without losing characters.

package com.formup.app.export

import java.text.BreakIterator
import java.util.Locale

object PdfTextLayout {
    fun wrap(text: String, width: Float, measure: (String) -> Float): List<String> {
        // Reject unusable widths before wrapping text.
        require(width > 0)
        val result = mutableListOf<String>()
        for (paragraph in text.replace("\r", "").split("\n")) {
            if (paragraph.isBlank()) { result += ""; continue }
            var line = ""
            for (word in paragraph.trim().split(Regex("\\s+"))) {
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (measure(candidate) <= width) { line = candidate; continue }
                if (line.isNotEmpty()) { result += line; line = "" }
                if (measure(word) <= width) { line = word; continue }
                val iterator = BreakIterator.getCharacterInstance(Locale.ROOT).apply { setText(word) }
                var start = iterator.first()
                var end = iterator.next()
                while (end != BreakIterator.DONE) {
                    val grapheme = word.substring(start, end)
                    if (line.isNotEmpty() && measure(line + grapheme) > width) { result += line; line = "" }
                    line += grapheme
                    start = end; end = iterator.next()
                }
            }
            if (line.isNotEmpty()) result += line
        }
        return result.ifEmpty { listOf("") }
    }
}
