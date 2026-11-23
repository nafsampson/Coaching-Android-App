// Collects coach and team profile changes.

package com.formup.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.common.BackHeader
import com.formup.app.ui.home.components.FormUpBottomBar
import com.formup.app.ui.home.components.FormUpTopBar
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@Composable
fun EditProfileScreen(
    coachName: String,
    teamName: String,
    squad: String,
    hasUnread: Boolean,
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onSave: (coachName: String, teamName: String, squad: String) -> Unit,
    onSelectTab: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(coachName) }
    var team by remember { mutableStateOf(teamName) }
    var squadName by remember { mutableStateOf(squad.filter { it.isDigit() }.toIntOrNull()?.let { "U$it" }.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var teamError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = FormUpColors.Background,
        topBar = { FormUpTopBar(hasUnread = hasUnread, onNotifications = onNotifications) },
        bottomBar = { FormUpBottomBar(selected = HomeTab.Profile, onSelect = onSelectTab) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BackHeader(title = stringResource(R.string.loc_edit_profile), onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.coach),
                            style = MaterialTheme.typography.titleMedium,
                            color = FormUpColors.TextPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        FormField(stringResource(R.string.full_name), name, nameError) { name = it }
                    }
                }

                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.team_information),
                            style = MaterialTheme.typography.titleMedium,
                            color = FormUpColors.TextPrimary
                        )
                        Spacer(Modifier.height(10.dp))

                        FormField(stringResource(R.string.team_name), team, teamError) { team = it }

                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(R.string.age_group_label), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        com.formup.app.ui.auth.OptionDropdown(squadName, stringResource(R.string.loc_age_group_select),
                            com.formup.app.ui.auth.AgeGroupOptions, { squadName = it; ageError = null }, ageError)
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, FormUpColors.Hairline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FormUpColors.TextPrimary)
                        ) {
                            Text(stringResource(R.string.ui_cancel), fontFamily = Mono, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        val context = LocalContext.current
                        com.formup.app.ui.common.FormUpButton(
                            onClick = {
                                nameError = if (name.isBlank()) context.getString(R.string.error_enter_name) else null
                                teamError = if (team.isBlank()) context.getString(R.string.error_enter_team_name) else null
                                ageError = if (squadName !in com.formup.app.ui.auth.AgeGroupOptions) context.getString(R.string.error_enter_age_group) else null
                                if (nameError == null && teamError == null && ageError == null) {
                                    onSave(name.trim(), team.trim(), squadName.trim())
                                }
                            },                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FormUpColors.ButtonFill,
                                contentColor = FormUpColors.OnButton
                            )
                        ) {
                            Text(stringResource(R.string.loc_save), fontFamily = Mono, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    error: String? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label, fontFamily = Mono, fontSize = 12.sp, color = FormUpColors.TextSecondary)
        },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error, fontSize = 12.sp, color = FormUpColors.Danger) }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FormUpColors.Primary,
            unfocusedBorderColor = FormUpColors.Hairline,
            focusedTextColor = FormUpColors.TextPrimary,
            unfocusedTextColor = FormUpColors.TextPrimary,
            cursorColor = FormUpColors.Primary,
            focusedContainerColor = FormUpColors.Surface,
            unfocusedContainerColor = FormUpColors.Surface
        )
    )
}
