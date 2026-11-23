// Validates player details before adding or updating a squad member.

package com.formup.app.ui.team

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.data.Player
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerScreen(
    existing: Player? = null,
    saving: Boolean = false,
    onBack: () -> Unit = {},
    onSave: (name: String, position: String, dateOfBirth: String?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val editing = existing != null

    val nameErrorText = stringResource(R.string.player_form_name_error)
    val positionErrorText = stringResource(R.string.player_form_position_error)
    var form by remember(existing?.id) {
        mutableStateOf(
            AddPlayerFormState(
                fullName = existing?.name.orEmpty(),
                position = existing?.position.orEmpty(),
                dateOfBirth = isoToDisplay(existing?.dateOfBirth),
                dateOfBirthIso = existing?.dateOfBirth?.take(10).orEmpty()
            )
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    fun update(block: (AddPlayerFormState) -> AddPlayerFormState) {
        form = block(form)
    }

    fun save() {
        val nameError = if (form.fullName.isBlank()) nameErrorText else null
        val positionError = if (form.position.isBlank()) positionErrorText else null

        if (nameError != null || positionError != null) {
            form = form.copy(nameError = nameError, positionError = positionError)
            return
        }
        if (saving) return

        onSave(form.fullName.trim(), form.position, form.dateOfBirthIso.ifBlank { null })
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = FormUpColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editing) {
                            stringResource(R.string.player_form_edit_title)
                        } else {
                            stringResource(R.string.player_form_add_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = FormUpColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.ui_back),
                            tint = FormUpColors.TextPrimary
                        )
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {
                SectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {

                    FieldLabel(stringResource(R.string.player_form_full_name))
                    FormField(
                        value = form.fullName,
                        onValueChange = {
                            update { state ->
                                state.copy(
                                    fullName = it,
                                    nameError = null
                                )
                            }
                        },
                        placeholder = stringResource(R.string.player_form_name_placeholder),
                        error = form.nameError
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            FieldLabel(	stringResource(R.string.player_form_date_of_birth))
                            DateTimeField(
                                value = form.dateOfBirth.ifBlank { "dd MMM yyyy" },
                                onClick = { showDatePicker = true }
                            )
                        }

                        Column(Modifier.weight(1f)) {
                            FieldLabel(stringResource(R.string.player_form_position))
                            PositionDropdown(
                                selected = form.position,
                                onSelect = {
                                    update { state ->
                                        state.copy(
                                            position = it,
                                            positionError = null
                                        )
                                    }
                                },
                                error = form.positionError
                            )
                        }
                    }

                }
            }

            item {
                com.formup.app.ui.common.FormUpButton(
                    onClick = ::save,
                    enabled = !saving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Text(
                        when {
                            saving -> stringResource(R.string.player_form_saving)
                            editing -> stringResource(R.string.player_form_save_changes)
                            else -> stringResource(R.string.player_form_add_to_squad)
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            val formatted = date.format(
                                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                            )

                            update { state ->
                                state.copy(dateOfBirth = formatted, dateOfBirthIso = date.toString())
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ui_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.ui_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateTimeField(
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = FormUpColors.Surface,
                disabledBorderColor = FormUpColors.Hairline,
                disabledTextColor = FormUpColors.TextPrimary,
                disabledPlaceholderColor = FormUpColors.TextSecondary
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        fontFamily = Mono,
        fontSize = 12.sp,
        color = FormUpColors.TextSecondary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = FormUpColors.TextSecondary
                )
            },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FormUpColors.Surface,
                unfocusedContainerColor = FormUpColors.Surface,
                focusedBorderColor = FormUpColors.Primary,
                unfocusedBorderColor = FormUpColors.Hairline,
                cursorColor = FormUpColors.Primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Text(
                it,
                fontFamily = Mono,
                fontSize = 12.sp,
                color = FormUpColors.Danger,
                modifier = Modifier.padding(
                    top = 4.dp,
                    start = 2.dp
                )
            )
        }
    }
}

@Composable
private fun PositionDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    FormUpColors.Surface,
                    RoundedCornerShape(10.dp)
                )
                .border(
                    1.dp,
                    if (error != null) {
                        FormUpColors.Danger
                    } else {
                        FormUpColors.Hairline
                    },
                    RoundedCornerShape(10.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when (selected) {
                    "Goalkeeper" -> stringResource(R.string.player_position_goalkeeper)
                    "Defender" -> stringResource(R.string.player_position_defender)
                    "Midfielder" -> stringResource(R.string.player_position_midfielder)
                    "Forward" -> stringResource(R.string.player_position_forward)
                    else -> stringResource(R.string.player_form_select_position)
                },
                color = if (selected.isBlank()) {
                    FormUpColors.TextSecondary
                } else {
                    FormUpColors.TextPrimary
                }
            )

            Spacer(Modifier.weight(1f))

            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = FormUpColors.TextPrimary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PositionOptions.forEach { position ->

                val translatedPosition = when (position) {
                    "Goalkeeper" -> stringResource(R.string.player_position_goalkeeper)
                    "Defender" -> stringResource(R.string.player_position_defender)
                    "Midfielder" -> stringResource(R.string.player_position_midfielder)
                    "Forward" -> stringResource(R.string.player_position_forward)
                    else -> position
                }

                DropdownMenuItem(
                    text = { Text(translatedPosition) },
                    onClick = {
                        onSelect(position)
                        expanded = false
                    }
                )
            }
        }

        error?.let {
            Text(
                it,
                fontFamily = Mono,
                fontSize = 12.sp,
                color = FormUpColors.Danger,
                modifier = Modifier.padding(
                    top = 4.dp,
                    start = 2.dp
                )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun isoToDisplay(iso: String?): String =
    if (iso.isNullOrBlank()) "" else runCatching {
        LocalDate.parse(iso.take(10)).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
    }.getOrDefault("")
