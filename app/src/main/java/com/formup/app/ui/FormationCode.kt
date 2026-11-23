// Formats formation digits and validates the outfield player total.

package com.formup.app.ui

internal object FormationCode {
    fun digits(input: String) = input.filter { it in '0'..'9' }
    fun format(digits: String) = digits.map { it.toString() }.joinToString("-")
    // The goalkeeper is separate, so formation lines must total ten outfield players.
    fun valid(code: String): Boolean = code.matches(Regex("[1-9](?:-[1-9]){1,9}")) && code.split("-").sumOf { it.toInt() } == 10
    fun displayOffset(offset: Int, length: Int) = if (offset == 0) 0 else (offset * 2 - 1).coerceIn(0, (length * 2 - 1).coerceAtLeast(0))
    fun inputOffset(offset: Int, length: Int) = ((offset + 1) / 2).coerceIn(0, length)
}
