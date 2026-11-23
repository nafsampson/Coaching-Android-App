// Shows coach details, season totals and app settings.

package com.formup.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.home.components.FormUpBottomBar
import com.formup.app.ui.home.components.FormUpTopBar
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    darkTheme: Boolean = false,
    onDarkThemeChange: (Boolean) -> Unit = {},
    biometricEnabled: Boolean = false,
    onBiometricToggle: (Boolean) -> Unit = {},
    onNotifications: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onEditTeamInfo: () -> Unit = {},
    onLanguageSelected: (LanguageOption) -> Unit = {},
    onMatchRemindersChange: (Boolean) -> Unit = {},
    onWeeklySummaryChange: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
    onSelectTab: (HomeTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var language by remember(state.language) { mutableStateOf(state.language) }
    var matchReminders by remember(state.matchReminders) { mutableStateOf(state.matchReminders) }
    var weeklySummary by remember(state.weeklySummary) { mutableStateOf(state.weeklySummary) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FormUpColors.Background,
        topBar = {
            FormUpTopBar(
                hasUnread = state.unreadNotifications,
                onNotifications = onNotifications
            )
        },
        bottomBar = {
            FormUpBottomBar(selected = HomeTab.Profile, onSelect = onSelectTab)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {
                ProfileHeaderCard(
                    name = state.name,
                    onEditProfile = onEditProfile
                )
            }

            item { ProfileSectionHeading(  stringResource(R.string.season_stats)) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.seasonStats.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { stat ->
                                ProfileStatTile(stat = stat, modifier = Modifier.weight(1f))
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            item { ProfileSectionHeading(stringResource(R.string.recent_activity)) }

            item {
                SectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    if (state.recentActivity.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_matches_played),
                            style = MaterialTheme.typography.bodyMedium,
                            color = FormUpColors.TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    state.recentActivity.forEachIndexed { index, activity ->
                        ProfileActivityRow(activity)
                        if (index != state.recentActivity.lastIndex) {
                            HorizontalDivider(
                                color = FormUpColors.Hairline,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }

            item { ProfileSectionHeading(stringResource(R.string.preferences)) }

            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileLanguageSelector(
                        selected = language,
                        onSelected = {
                            language = it
                            onLanguageSelected(it)
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(color = FormUpColors.Hairline)
                    Spacer(Modifier.height(4.dp))
                    ProfileToggleRow(
                        title = stringResource(R.string.match_reminders),
                        subtitle =stringResource(R.string.match_reminders_description),
                        checked = matchReminders,
                        onCheckedChange = {
                            matchReminders = it
                            onMatchRemindersChange(it)
                        }
                    )
                    ProfileToggleRow(
                        title = stringResource(R.string.weekly_summary),
                        subtitle = stringResource(R.string.weekly_summary_description),
                        checked = weeklySummary,
                        onCheckedChange = {
                            weeklySummary = it
                            onWeeklySummaryChange(it)
                        }
                    )
                    ProfileToggleRow(
                        title = stringResource(R.string.dark_mode),
                        subtitle = stringResource(R.string.dark_mode_description),
                        checked = darkTheme,
                        onCheckedChange = onDarkThemeChange
                    )
                    ProfileToggleRow(
                        title = stringResource(R.string.loc_biometric_login),
                        subtitle = stringResource(R.string.loc_biometric_description),
                        checked = biometricEnabled,
                        onCheckedChange = onBiometricToggle
                    )
                }
            }

            item { ProfileSectionHeading(stringResource(R.string.team_information)) }

            item {
                TeamInfoCard(info = state.teamInfo, onEdit = onEditTeamInfo)
            }

            item {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, FormUpColors.Hairline)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = FormUpColors.Danger,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.sign_out),
                        color = FormUpColors.Danger,
                        fontFamily = Mono,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
