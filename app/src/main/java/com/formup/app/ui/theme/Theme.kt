// Applies the app colour scheme and typography.

package com.formup.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun FormUpTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {

    FormUpColors.updateDarkMode(darkTheme)
    val colors = if (darkTheme) darkColorScheme(
        primary = FormUpColors.Primary, onPrimary = if (darkTheme) FormUpColors.Ink else androidx.compose.ui.graphics.Color.White,
        primaryContainer = FormUpColors.PrimaryTint, onPrimaryContainer = FormUpColors.TextPrimary,
        secondary = FormUpColors.Lime, onSecondary = FormUpColors.Ink,
        background = FormUpColors.Background, onBackground = FormUpColors.TextPrimary,
        surface = FormUpColors.Surface, onSurface = FormUpColors.TextPrimary,
        surfaceVariant = FormUpColors.SurfaceRaised, onSurfaceVariant = FormUpColors.TextSecondary,
        error = FormUpColors.Danger, onError = FormUpColors.Ink, outline = FormUpColors.Hairline
    ) else lightColorScheme(
        primary = FormUpColors.Primary, onPrimary = if (darkTheme) FormUpColors.Ink else androidx.compose.ui.graphics.Color.White,
        primaryContainer = FormUpColors.PrimaryTint, onPrimaryContainer = FormUpColors.PrimaryDeep,
        secondary = FormUpColors.PrimaryDeep, onSecondary = androidx.compose.ui.graphics.Color.White,
        background = FormUpColors.Background, onBackground = FormUpColors.TextPrimary,
        surface = FormUpColors.Surface, onSurface = FormUpColors.TextPrimary,
        surfaceVariant = FormUpColors.SurfaceRaised, onSurfaceVariant = FormUpColors.TextSecondary,
        error = FormUpColors.Danger, outline = FormUpColors.Hairline
    )
    MaterialTheme(colorScheme = colors, typography = FormUpTypography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content)
}
