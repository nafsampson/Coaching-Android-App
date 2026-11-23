// Defines profile data and supported languages.

package com.formup.app.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector

data class SeasonStat(
    val label: String,
    val value: String,
    val icon: ImageVector
)

enum class ActivityTone { Match, Training }

data class ActivityItem(
    val title: String,
    val subtitle: String,
    val date: String,
    val tone: ActivityTone
) {
    val icon: ImageVector
        get() = when (tone) {
            ActivityTone.Match -> Icons.Filled.SportsSoccer
            ActivityTone.Training -> Icons.Filled.FitnessCenter
        }
}

data class TeamInfo(
    val teamName: String,
    val squad: String
)

data class LanguageOption(
    val code: String,
    val label: String,
    val nativeLabel: String
)

val SupportedLanguages = listOf(
    LanguageOption("en", "English", "English"),
    LanguageOption("af", "Afrikaans", "Afrikaans"),
    LanguageOption("xh", "isiXhosa", "isiXhosa")
)

data class ProfileUiState(
    val name: String,
    val seasonStats: List<SeasonStat>,
    val recentActivity: List<ActivityItem>,
    val teamInfo: TeamInfo,
    val language: LanguageOption,
    val matchReminders: Boolean,
    val weeklySummary: Boolean,
    val unreadNotifications: Boolean
)
