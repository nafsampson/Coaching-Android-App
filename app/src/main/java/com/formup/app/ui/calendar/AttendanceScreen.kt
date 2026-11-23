// Shows attendance marks and asks for confirmation before saving.

package com.formup.app.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.ui.home.components.IconBubble
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.home.components.StatusPill
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(

    state: AttendanceUiState,
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onDiscard: () -> Unit = {},
    onEditEvent: () -> Unit = {},
    onMarkAllPresent: () -> Unit = {},
    onSetStatus: (playerId: String, status: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
){
    val displayEventTitle = when {
        state.eventTitle.startsWith("Match vs. ") -> {
            stringResource(
                R.string.details_match_title,
                state.eventTitle.removePrefix("Match vs. ")
            )
        }

        state.eventTitle == "Match attendance" -> {
            stringResource(R.string.att_match_attendance)
        }

        else -> state.eventTitle
    }

    var nudgedIds by remember { mutableStateOf(setOf<String>()) }
    var markedPresentIds by remember { mutableStateOf(setOf<String>()) }

    var discardDialog by remember { mutableStateOf(false) }
    fun requestBack() { if (state.hasChanges) discardDialog = true else onBack() }
    androidx.activity.compose.BackHandler(enabled = state.hasChanges) { if (!state.isSaving) requestBack() }
    if (discardDialog) androidx.compose.material3.AlertDialog(
        onDismissRequest = { discardDialog = false },
        title = { Text(stringResource(R.string.review_discard_title)) },
        text = { Text(stringResource(R.string.review_discard_body)) },
        confirmButton = { androidx.compose.material3.TextButton({ discardDialog = false; onDiscard(); onBack() }) { Text(stringResource(R.string.review_discard)) } },
        dismissButton = { androidx.compose.material3.TextButton({ discardDialog = false }) { Text(stringResource(R.string.review_keep_editing)) } }
    )
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FormUpColors.Background,
        bottomBar = {
            Surface(color = FormUpColors.Surface) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)) {
                    Text(stringResource(R.string.review_attendance_draft), color = FormUpColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    com.formup.app.ui.common.FormUpButton(onConfirm, Modifier.fillMaxWidth(), enabled = state.canEdit && state.hasChanges) {
                        Text(stringResource(if (state.isSaving) R.string.review_saving else R.string.review_confirm_attendance))
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_record_attendance), color = FormUpColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { if (!state.isSaving) requestBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_back), tint = FormUpColors.TextPrimary)
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            state.notice?.let { notice -> item { Text(notice, color = FormUpColors.TextSecondary) } }
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = FormUpColors.TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(state.dateTimeLabel, fontFamily = Mono, fontSize = 12.sp, letterSpacing = 0.4.sp, color = FormUpColors.TextSecondary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(displayEventTitle, style = MaterialTheme.typography.headlineMedium, color = FormUpColors.TextPrimary)
                    Spacer(Modifier.height(10.dp))
                    com.formup.app.ui.common.FormUpButton(
                        onClick = onEditEvent,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.size(8.dp))
                        Text( stringResource(R.string.att_edit_event), fontFamily = Mono, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = FormUpColors.Surface,
                    border = BorderStroke(1.5.dp, FormUpColors.Primary)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.att_squad_status), fontFamily = Mono, fontSize = 12.sp, letterSpacing = 0.5.sp, color = FormUpColors.TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${state.attending}/${state.squadSize}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = FormUpColors.TextPrimary)
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.att_present), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AttendanceStatCard(
                        stringResource(R.string.att_present),
                        state.attending,
                        Icons.Filled.CheckCircle,
                        FormUpColors.Primary,
                        Modifier.weight(1f)
                    )

                    AttendanceStatCard(
                        stringResource(R.string.att_absent),
                        state.absentCount,
                        Icons.Filled.Cancel,
                        FormUpColors.Danger,
                        Modifier.weight(1f)
                    )

                    AttendanceStatCard(
                        stringResource(R.string.att_not_recorded),
                        state.noReplyCount,
                        Icons.Filled.Help,
                        FormUpColors.AmberIcon,
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.att_roster_details), style = MaterialTheme.typography.headlineMedium, color = FormUpColors.TextPrimary, modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onMarkAllPresent,
                        enabled = state.canEdit,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, FormUpColors.Primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FormUpColors.Primary),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.att_mark_all_present), fontFamily = Mono, fontSize = 12.sp)
                    }
                }
            }

            item {
                SectionCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                    RosterStatus.entries.forEach { group ->
                        val entries = state.roster.filter { it.status == group }
                        if (entries.isNotEmpty()) {
                            GroupHeader(group, entries.size)
                            entries.forEachIndexed { index, entry ->
                                RosterRow(
                                    entry = entry,
                                    enabled = state.canEdit,
                                    onSetStatus = { status -> onSetStatus(entry.id, status) }
                                )
                                if (index != entries.lastIndex) {
                                    HorizontalDivider(color = FormUpColors.Hairline, modifier = Modifier.padding(horizontal = 14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatCard(label: String, value: Int, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = FormUpColors.NavIndicator) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = FormUpColors.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(label, fontFamily = Mono, fontSize = 12.sp, letterSpacing = 0.4.sp, color = FormUpColors.TextSecondary)
        }
    }
}

@Composable
private fun GroupHeader(status: RosterStatus, count: Int) {
    val dotColor = when (status) {
        RosterStatus.NO_REPLY -> FormUpColors.AmberIcon
        RosterStatus.ABSENT -> FormUpColors.Danger
        RosterStatus.ATTENDING -> FormUpColors.Primary
    }
    Surface(color = FormUpColors.Background, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(6.dp)
                    .background(dotColor, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(Modifier.size(6.dp))
            val translatedStatus = when (status) {
                RosterStatus.NO_REPLY -> stringResource(R.string.att_not_recorded)
                RosterStatus.ABSENT -> stringResource(R.string.att_absent)
                RosterStatus.ATTENDING -> stringResource(R.string.att_present)
            }
            Text(
                stringResource(R.string.att_status_count, translatedStatus, count),
                fontFamily = Mono,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Medium,
                color = FormUpColors.TextSecondary
            )
        }
    }
}

@Composable
private fun RosterRow(entry: RosterEntry, enabled: Boolean, onSetStatus: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(size = 38.dp, background = FormUpColors.PrimaryTint) {
            Text(initialsOf(entry.name), color = FormUpColors.PrimaryDeep, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.name, style = MaterialTheme.typography.titleMedium, color = FormUpColors.TextPrimary)
            Spacer(Modifier.height(2.dp))
            if (entry.statusBadge != null) {
                StatusPill(
                    text = if (entry.statusBadge == "NO EXCUSE") {
                        stringResource(R.string.att_no_excuse)
                    } else {
                        entry.statusBadge
                    },
                    background = FormUpColors.Danger.copy(alpha = 0.12f),
                    contentColor = FormUpColors.Danger
                )
            } else {
                Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
            }
        }
        IconButton(enabled = enabled, onClick = { onSetStatus(ApiValues.ATTENDANCE_PRESENT) }) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = stringResource(
                    R.string.att_mark_player_present,
                    entry.name
                ),
                tint = if (entry.status == RosterStatus.ATTENDING) FormUpColors.Primary
                else FormUpColors.TextSecondary.copy(alpha = 0.5f)
            )
        }
        IconButton(enabled = enabled, onClick = { onSetStatus(ApiValues.ATTENDANCE_ABSENT) }) {
            Icon(
                Icons.Filled.Cancel,
                contentDescription = stringResource(
                    R.string.att_mark_player_absent,
                    entry.name
                ),
                tint = if (entry.status == RosterStatus.ABSENT) FormUpColors.Danger
                else FormUpColors.TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}
private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
