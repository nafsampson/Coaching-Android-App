// Defines the colours used in light and dark mode.

package com.formup.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

object FormUpColors {
    var darkMode by mutableStateOf(false)
        private set
    fun updateDarkMode(enabled: Boolean) { darkMode = enabled }
    val Ink = Color(0xFF102326)
    val Lime = Color(0xFFD5F66A)
    val LightAction = Color(0xFF294441)
    val ButtonFill get() = if (darkMode) Lime else LightAction
    val OnButton get() = if (darkMode) Ink else Color.White
    val Pitch = Color(0xFF123E35)
    val OnPitch = Color(0xFFF4FFF5)
    val Background get() = if (darkMode) Color(0xFF0B171B) else Color(0xFFF3F6F5)
    val Surface get() = if (darkMode) Color(0xFF14262B) else Color.White
    val SurfaceRaised get() = if (darkMode) Color(0xFF1C3339) else Color(0xFFE8EFEC)
    val Primary get() = if (darkMode) Lime else LightAction
    val PrimaryDeep get() = if (darkMode) Lime else LightAction
    val PrimaryTint get() = if (darkMode) Color(0xFF233D2D) else Color(0xFFE3EFDA)
    val Mint get() = if (darkMode) Color(0xFF234736) else Color(0xFFE1F1E8)
    val Amber get() = if (darkMode) Color(0xFFF7C969) else Color(0xFF946010)
    val AmberIcon get() = if (darkMode) Color(0xFFF7C969) else Color(0xFF875409)
    val AmberTint get() = if (darkMode) Color(0xFF44351D) else Color(0xFFFFF0CE)
    val Danger get() = if (darkMode) Color(0xFFFFA597) else Color(0xFFAF3028)
    val DangerTint get() = if (darkMode) Color(0xFF482D2A) else Color(0xFFFFECE8)
    val TextPrimary get() = if (darkMode) Color(0xFFF1F7F5) else Color(0xFF142C30)
    val TextSecondary get() = if (darkMode) Color(0xFFB5C9C8) else Color(0xFF53696C)
    val Hairline get() = if (darkMode) Color(0xFF30474D) else Color(0xFFD9E4E1)
    val NavIndicator get() = if (darkMode) Color(0xFF31472C) else Color(0xFFE5EFDD)
}
