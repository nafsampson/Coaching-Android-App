// Displays formation digits with automatic hyphens and correct cursor positions.

package com.formup.app.ui.calendar

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.formup.app.ui.FormationCode

internal object FormationCodeTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val length = text.length
        // Hyphens are display-only; editing and cursor positions use the original digits.
        return TransformedText(AnnotatedString(FormationCode.format(text.text)), object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = FormationCode.displayOffset(offset, length)
            override fun transformedToOriginal(offset: Int) = FormationCode.inputOffset(offset, length)
        })
    }
}
