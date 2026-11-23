// Lets the coach choose a formation and arrange players on the pitch.

package com.formup.app.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.data.FormationSlot
import com.formup.app.data.Player
import com.formup.app.data.SavedFormation
import com.formup.app.data.SquadUpdate
import com.formup.app.ui.theme.FormUpColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineupBuilderScreen(
    opponent: String,
    starters: List<Player>,
    substitutes: List<Player>,
    formations: List<SavedFormation>,
    selectedFormation: SavedFormation?,
    entryFor: (String) -> SquadUpdate?,
    onApplyFormation: (SavedFormation) -> Unit,
    onPositionChange: (String, String, Double, Double) -> Unit,
    onSaveTemplate: (String?, String, String, List<FormationSlot>) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onSaveLineup: () -> Unit,
    onBack: () -> Unit
) {
    var formationMenuExpanded by remember { mutableStateOf(false) }
    var saveDialogVisible by remember { mutableStateOf(false) }
    var deleteDialogVisible by remember { mutableStateOf(false) }

    var templateName by remember(selectedFormation?.id) {
        mutableStateOf(selectedFormation?.name.orEmpty())
    }
    var templateCode by remember(selectedFormation?.id) {
        mutableStateOf(com.formup.app.ui.FormationCode.digits(selectedFormation?.code.orEmpty()))
    }

    Scaffold(
        containerColor = FormUpColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.loc_lineup_vs, opponent),
                        color = FormUpColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(stringResource(R.string.ui_back), color = FormUpColors.TextPrimary)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = FormUpColors.Background,
                shadowElevation = 8.dp
            ) {
                com.formup.app.ui.common.FormUpButton(
                    onClick = onSaveLineup,
                    enabled = starters.size == 11,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton)
                ) {
                    Text(stringResource(R.string.loc_save_match_lineup))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                stringResource(R.string.loc_starters_count, starters.size),
                color = FormUpColors.TextSecondary
            )

            Box {
                OutlinedButton(
                    onClick = { formationMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedFormation?.let {
                            "${formationLabel(it)} (${it.code})"
                        } ?: stringResource(R.string.loc_choose_formation)
                    )
                }

                DropdownMenu(
                    expanded = formationMenuExpanded,
                    onDismissRequest = {
                        formationMenuExpanded = false
                    }
                ) {
                    if (formations.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.loc_no_formations)) },
                            onClick = { formationMenuExpanded = false },
                            enabled = false
                        )
                    }

                    formations.forEach { formation ->
                        DropdownMenuItem(
                            text = {
                                Text("${formationLabel(formation)} (${formation.code})")
                            },
                            onClick = {
                                formationMenuExpanded = false
                                onApplyFormation(formation)
                            }
                        )
                    }
                }
            }

            Text(
                stringResource(R.string.lineup_starting_xi),
                style = MaterialTheme.typography.titleLarge,
                color = FormUpColors.TextPrimary
            )

            if (starters.size != 11) {
                Text(
                    stringResource(R.string.loc_select_11_starters),
                    color = FormUpColors.AmberIcon
                )
            } else if (selectedFormation == null &&
                starters.any {
                    entryFor(it.id)?.pitchX == null ||
                            entryFor(it.id)?.pitchY == null
                }
            ) {
                Text(
                    stringResource(R.string.loc_choose_formation_hint),
                    color = FormUpColors.TextSecondary
                )
            }

            InteractiveLineupPitch(
                players = starters,
                entryFor = entryFor,
                onPositionChange = onPositionChange
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { saveDialogVisible = true },
                    enabled = starters.size == 11 &&
                            starters.all {
                                entryFor(it.id)?.pitchX != null &&
                                        entryFor(it.id)?.pitchY != null
                            },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (selectedFormation == null)
                            stringResource(R.string.loc_save_as_formation)
                        else
                            stringResource(R.string.loc_save_formation_changes)
                    )
                }

                if (selectedFormation != null &&
                    !selectedFormation.id.startsWith("builtin-")) {
                    OutlinedButton(
                        onClick = { deleteDialogVisible = true }
                    ) {
                        Text(stringResource(R.string.ui_delete))
                    }
                }
            }

            Text(
                stringResource(R.string.loc_substitutes_count, substitutes.size),
                style = MaterialTheme.typography.titleLarge,
                color = FormUpColors.TextPrimary
            )

            if (substitutes.isEmpty()) {
                Text(
                    stringResource(R.string.loc_no_substitutes),
                    color = FormUpColors.TextSecondary
                )
            } else {
                substitutes.forEach { player ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = FormUpColors.Surface,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                player.name,
                                modifier = Modifier.weight(1f),
                                color = FormUpColors.TextPrimary
                            )
                            Text(
                                player.position,
                                color = FormUpColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (saveDialogVisible) {
        AlertDialog(
            onDismissRequest = { saveDialogVisible = false },
            title = {
                Text(
                    if (selectedFormation == null)
                        stringResource(R.string.loc_save_formation)
                    else
                        stringResource(R.string.loc_update_formation)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text(stringResource(R.string.loc_formation_name)) },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = templateCode,
                        onValueChange = { templateCode = com.formup.app.ui.FormationCode.digits(it) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        visualTransformation = FormationCodeTransformation,
                        isError = templateCode.contains('0') || templateCode.sumOf { it.digitToInt() } > 10,
                        supportingText = { Text(stringResource(R.string.final_formation_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.loc_formation_code)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val positions = starters.map { player ->
                            val entry = entryFor(player.id)

                            FormationSlot(
                                slotId = player.id,
                                label = entry?.assignedPosition
                                    ?.takeIf { it.isNotBlank() }
                                    ?: player.position,
                                x = entry?.pitchX ?: 50.0,
                                y = entry?.pitchY ?: 50.0
                            )
                        }

                        onSaveTemplate(
                            selectedFormation?.id?.takeUnless {
                                it.startsWith("builtin-")
                            },
                            templateName.trim(),
                            com.formup.app.ui.FormationCode.format(templateCode),
                            positions
                        )

                        saveDialogVisible = false
                    },
                    enabled = templateName.isNotBlank() &&
                            com.formup.app.ui.FormationCode.valid(com.formup.app.ui.FormationCode.format(templateCode))
                ) {
                    Text(stringResource(R.string.loc_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { saveDialogVisible = false }
                ) {
                    Text(stringResource(R.string.ui_cancel))
                }
            }
        )
    }

    if (deleteDialogVisible && selectedFormation != null) {
        AlertDialog(
            onDismissRequest = { deleteDialogVisible = false },
            title = { Text(stringResource(R.string.loc_delete_formation)) },
            text = {
                Text(stringResource(R.string.loc_delete_formation_confirm, selectedFormation.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTemplate(selectedFormation.id)
                        deleteDialogVisible = false
                    }
                ) {
                    Text(stringResource(R.string.ui_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteDialogVisible = false }
                ) {
                    Text(stringResource(R.string.ui_cancel))
                }
            }
        )
    }
}

@Composable
private fun formationLabel(formation: SavedFormation): String = when (formation.id) {
    "builtin-433" -> stringResource(R.string.mod_form_attacking)
    "builtin-442" -> stringResource(R.string.mod_form_classic)
    "builtin-4231" -> stringResource(R.string.mod_form_balanced)
    else -> formation.name
}
