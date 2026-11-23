// Shows match and training cards with their available actions.

package com.formup.app.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.home.components.StatusPill
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@Composable
internal fun MatchEventCard(
    event: CalendarEvent.Match,
    onViewDetails: () -> Unit,
    onEditLineup: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = FormUpColors.Surface,
        border = BorderStroke(1.dp, FormUpColors.Hairline)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.event_form_match).uppercase(),
                    fontFamily = Mono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = FormUpColors.Primary
                )

                Spacer(Modifier.weight(1f))

                StatusPill(
                    text = event.dateLabel,
                    background = FormUpColors.Mint,
                    contentColor = FormUpColors.PrimaryDeep
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.calendar_opponent,
                    event.opponent
                ),
                style = MaterialTheme.typography.titleLarge,
                color = FormUpColors.TextPrimary
            )

            Spacer(Modifier.height(10.dp))

            EventInfoLine(
                Icons.Filled.AccessTime,
                event.timeText
            )

            Spacer(Modifier.height(4.dp))

            EventInfoLine(
                Icons.Filled.LocationOn,
                event.location
            )

            Spacer(Modifier.height(20.dp))

            com.formup.app.ui.common.FormUpButton(
                onClick = onViewDetails,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    stringResource(R.string.calendar_view_match),
                    fontFamily = Mono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onEditLineup,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, FormUpColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = FormUpColors.Surface,
                    contentColor = FormUpColors.Primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    stringResource(R.string.calendar_edit_lineup),
                    fontFamily = Mono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            com.formup.app.ui.common.ExportButton(stringResource(R.string.review_export_lineup), onExportPdf,
                Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(4.dp))
            EventEditDeleteRow(what = stringResource(R.string.calendar_match_word), onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
internal fun TrainingEventCard(
    event: CalendarEvent.Training,
    onViewTeamAttendance: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(
            stringResource(R.string.calendar_training),
            fontFamily = Mono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = FormUpColors.Primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            event.title,
            style = MaterialTheme.typography.titleLarge,
            color = FormUpColors.TextPrimary
        )
        Spacer(Modifier.height(10.dp))
        EventInfoLine(Icons.Filled.AccessTime, "${event.dateLabel} • ${event.timeRange}")
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onViewTeamAttendance,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, FormUpColors.Hairline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = FormUpColors.Surface,
                contentColor = FormUpColors.TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                stringResource(R.string.calendar_record_attendance),
                fontFamily = Mono,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(4.dp))
        EventEditDeleteRow(what = stringResource(R.string.calendar_session_word),onEdit = onEdit, onDelete = onDelete)
    }
}

@Composable
internal fun EventInfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = FormUpColors.TextSecondary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)
    }
}
@Composable
internal fun EventEditDeleteRow(what: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onEdit) { Text(stringResource(R.string.calendar_edit_item, what), color = FormUpColors.PrimaryDeep) }
        TextButton(onClick = { confirm = true }) { Text(stringResource(R.string.ui_delete), color = FormUpColors.Danger) }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            containerColor = FormUpColors.Surface,
            title = {
                Text(
                    stringResource(R.string.calendar_delete_question, what),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    stringResource(R.string.calendar_delete_undo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FormUpColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { confirm = false; onDelete() }) {
                    Text(stringResource(R.string.ui_delete), color = FormUpColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) {
                    Text(stringResource(R.string.ui_cancel), color = FormUpColors.TextSecondary)
                }
            }
        )
    }
}
