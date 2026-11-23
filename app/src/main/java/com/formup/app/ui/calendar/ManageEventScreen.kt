// Validates and submits match and training event forms.

package com.formup.app.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.data.Fixture
import com.formup.app.data.TrainingSession
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEventScreen(
    editMatch: Fixture? = null,
    editSession: TrainingSession? = null,
    saving: Boolean = false,
    onBack: () -> Unit = {},
    onSaveMatch: (opponent: String, matchDateIso: String, venue: String) -> Unit = { _, _, _ -> },
    onSaveSession: (sessionDateIso: String, notes: String?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val editing = editMatch != null || editSession != null

    val sessionDateErrorText = stringResource(R.string.event_error_session_date)
    val sessionTimeErrorText = stringResource(R.string.event_error_session_time)
    val matchDateErrorText = stringResource(R.string.event_error_match_date)
    val kickoffErrorText = stringResource(R.string.event_error_kickoff)
    val opponentErrorText = stringResource(R.string.event_error_opponent)
    val invalidDateErrorText = stringResource(R.string.event_error_invalid_date)

    var kind by remember {
        mutableStateOf(if (editMatch != null) EventKind.MATCH else EventKind.TRAINING)
    }

    var trainingForm by remember(editSession?.id) {
        mutableStateOf(
            editSession?.let {
                val (date, time) = splitEventIso(it.sessionDateIso)
                TrainingFormState(date = date, time = time, notes = it.notes.orEmpty())
            } ?: TrainingFormState()
        )
    }

    var matchForm by remember(editMatch?.id) {
        mutableStateOf(
            editMatch?.let {
                val (date, time) = splitEventIso(it.matchDateIso)
                MatchFormState(opponent = it.opponent, date = date, time = time, venue = it.venue)
            } ?: MatchFormState()
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    fun createTraining() {
        val dateError = if (trainingForm.date.isBlank()) sessionDateErrorText else null
        val timeError = if (trainingForm.time.isBlank()) sessionTimeErrorText else null
        if (dateError != null || timeError != null) {
            trainingForm = trainingForm.copy(dateError = dateError, timeError = timeError)
            return
        }

        val isoDate = createEventIsoDateTime(trainingForm.date, trainingForm.time)
        if (isoDate == null) {
            trainingForm = trainingForm.copy(dateError = invalidDateErrorText)
            return
        }
        if (saving) return

        onSaveSession(isoDate, trainingForm.notes.trim().ifBlank { null })
    }

    fun createMatch() {
        val opponentError = if (matchForm.opponent.isBlank()) opponentErrorText else null
        val dateError = if (matchForm.date.isBlank()) matchDateErrorText else null
        val timeError = if (matchForm.time.isBlank()) kickoffErrorText else null
        if (opponentError != null || dateError != null || timeError != null) {
            matchForm = matchForm.copy(
                opponentError = opponentError,
                dateError = dateError,
                timeError = timeError
            )
            return
        }

        val isoDate = createEventIsoDateTime(matchForm.date, matchForm.time)
        if (isoDate == null) {
            matchForm = matchForm.copy(dateError = invalidDateErrorText)
            return
        }
        if (saving) return

        onSaveMatch(matchForm.opponent.trim(), isoDate, matchForm.venue)
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = FormUpColors.Background,
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        if (editing) {
                            stringResource(R.string.event_form_edit_title)
                        } else {
                            stringResource(R.string.event_form_create_title)
                        },
                        style = MaterialTheme.typography.titleMedium,
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Let the focused notes field scroll above the keyboard instead of being compressed.
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            if(!editing){EventTypeToggle(
                selected = kind,
                onSelect = {
                    kind = it
                    showDatePicker = false
                    showTimePicker = false
                }
            )}

            when (kind) {
                EventKind.MATCH -> {
                    SectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        EventFieldLabel(stringResource(R.string.event_form_opponent))

                        EventFormField(
                            value = matchForm.opponent,
                            onValueChange = {
                                matchForm = matchForm.copy(
                                    opponent = it,
                                    opponentError = null
                                )
                            },
                            placeholder = stringResource(R.string.event_form_opponent_placeholder),
                            error = matchForm.opponentError
                        )

                        Spacer(Modifier.height(16.dp))

                        EventFieldLabel(stringResource(R.string.event_form_venue))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = matchForm.venue == "Home",
                                onClick = {
                                    matchForm = matchForm.copy(venue = "Home")
                                },
                                label = { Text(stringResource(R.string.event_form_home)) },
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = matchForm.venue == "Away",
                                onClick = {
                                    matchForm = matchForm.copy(venue = "Away")
                                },
                                label = { Text(stringResource(R.string.event_form_away)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        EventFieldLabel(stringResource(R.string.event_form_match_date))

                        EventDateTimeField(
                            value = matchForm.date.ifBlank {
                                stringResource(R.string.event_form_select_date)
                            },
                            onClick = { showDatePicker = true }
                        )

                        matchForm.dateError?.let {
                            Text(
                                text = it,
                                color = FormUpColors.Danger,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        EventFieldLabel(stringResource(R.string.event_form_kickoff))

                        EventDateTimeField(
                            value = matchForm.time.ifBlank {
                                stringResource(R.string.event_form_select_time)
                            },
                            onClick = { showTimePicker = true }
                        )
                        matchForm.timeError?.let {
                            Text(text = it, color = FormUpColors.Danger, fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 2.dp))
                        }
                    }
                }

                EventKind.TRAINING -> {
                    SectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        EventFieldLabel(stringResource(R.string.event_form_session_date))

                        EventDateTimeField(
                            value = trainingForm.date.ifBlank {  stringResource(R.string.event_form_select_date) },
                            onClick = { showDatePicker = true }
                        )

                        trainingForm.dateError?.let {
                            Text(
                                text = it,
                                color = FormUpColors.Danger,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        EventFieldLabel(stringResource(R.string.event_form_session_time))

                        EventDateTimeField(
                            value = trainingForm.time.ifBlank {  stringResource(R.string.event_form_select_time) },
                            onClick = { showTimePicker = true }
                        )

                        trainingForm.timeError?.let {
                            Text(text = it, color = FormUpColors.Danger, fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 2.dp))
                        }

                        Spacer(Modifier.height(14.dp))

                        EventFieldLabel(stringResource(R.string.event_form_notes))

                        OutlinedTextField(
                            value = trainingForm.notes,
                            onValueChange = {
                                trainingForm = trainingForm.copy(notes = it)
                            },
                            placeholder = {
                                Text(
                                    stringResource(R.string.event_form_optional_notes),
                                    color = FormUpColors.TextSecondary
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            minLines = 4,
                            maxLines = 8,
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
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            com.formup.app.ui.common.FormUpButton(
                onClick = {
                    if (kind == EventKind.MATCH) {
                        createMatch()
                    } else {
                        createTraining()
                    }
                },
                enabled = !saving,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text(
                    text = when {
                        saving -> stringResource(R.string.event_form_saving)
                        editing -> stringResource(R.string.event_form_save_changes)
                        kind == EventKind.MATCH -> stringResource(R.string.event_form_create_match)
                        else -> stringResource(R.string.event_form_create_training)
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {

                            val date = Instant
                                .ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()

                            val formatted = date.format(
                                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                            )

                            if (kind == EventKind.MATCH) {
                                matchForm = matchForm.copy(date = formatted, dateError = null)
                            } else {
                                trainingForm = trainingForm.copy(date = formatted, dateError = null)
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

    if (showTimePicker) {
        val currentTime = if (kind == EventKind.MATCH) matchForm.time else trainingForm.time
        val pickerTime = EventTimeDefaults.pickerTime(kind, currentTime)
        val timePickerState = rememberTimePickerState(
            initialHour = pickerTime.hour,
            initialMinute = pickerTime.minute,
            is24Hour = true
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = FormUpColors.Surface,
            title = { Text(stringResource(R.string.event_form_select_time), color = FormUpColors.TextPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {

                        val selectedTime = String.format(
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )

                        if (kind == EventKind.MATCH) {
                            matchForm = matchForm.copy(time = selectedTime, timeError = null)
                        } else {
                            trainingForm = trainingForm.copy(time = selectedTime, timeError = null)                        }

                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ui_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.ui_cancel))
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimeInput(state = timePickerState, colors = androidx.compose.material3.TimePickerDefaults.colors(
                        containerColor = FormUpColors.Surface,
                        timeSelectorSelectedContainerColor = FormUpColors.ButtonFill,
                        timeSelectorSelectedContentColor = FormUpColors.OnButton,
                        timeSelectorUnselectedContainerColor = FormUpColors.SurfaceRaised,
                        timeSelectorUnselectedContentColor = FormUpColors.TextPrimary
                    ))
                }
            }
        )
    }
}
