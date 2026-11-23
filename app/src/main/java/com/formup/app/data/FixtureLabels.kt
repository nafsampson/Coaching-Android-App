// Formats fixture labels for the current language.

package com.formup.app.data

internal val Fixture.resultWord: String
    get() = when (resultLetter) {
        "W" -> "Won"
        "L" -> "Lost"
        else -> "Drew"
    }
