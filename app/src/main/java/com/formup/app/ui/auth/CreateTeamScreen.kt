// Collects and validates the team name and age group.

package com.formup.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.auth.components.AuthFieldLabel
import com.formup.app.ui.auth.components.AuthTextField
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onTeamCreated: (CreatedTeam) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var form by remember { mutableStateOf(TeamFormState()) }

    fun create() {
        val nameError = if (form.teamName.isBlank()) context.getString(R.string.loc_team_name_required) else null
        val ageError = if (form.ageGroup.isBlank()) context.getString(R.string.loc_age_group_required) else null
        if (nameError != null || ageError != null) {
            form = form.copy(nameError = nameError, ageError = ageError)
            return
        }
        onTeamCreated(
            CreatedTeam(
                teamName = form.teamName.trim(),
                ageGroup = form.ageGroup.filter { it.isDigit() }.toInt()
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = FormUpColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.loc_create_team), style = MaterialTheme.typography.titleLarge, color = FormUpColors.Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_back), tint = FormUpColors.TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = ::create) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.loc_create_team), tint = FormUpColors.Primary)
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SectionCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = FormUpColors.Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.loc_your_team), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.width(48.dp).height(3.dp).background(FormUpColors.Primary, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.height(16.dp))

                    AuthFieldLabel(stringResource(R.string.loc_team_name))
                    AuthTextField(
                        value = form.teamName,
                        onValueChange = { form = form.copy(teamName = it, nameError = null) },
                        placeholder = stringResource(R.string.loc_team_name_hint),
                        error = form.nameError
                    )

                    Spacer(Modifier.height(14.dp))
                    AuthFieldLabel(stringResource(R.string.loc_age_group))
                    OptionDropdown(
                        selected = form.ageGroup,
                        placeholder = stringResource(R.string.loc_age_group_select),
                        options = AgeGroupOptions,
                        onSelect = { form = form.copy(ageGroup = it, ageError = null) },
                        error = form.ageError
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, FormUpColors.Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FormUpColors.Primary),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Text(stringResource(R.string.ui_cancel), style = MaterialTheme.typography.titleMedium)
                }
            }

            item {
                com.formup.app.ui.common.FormUpButton(
                    onClick = ::create,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Icon(Icons.Filled.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.loc_create_team), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
