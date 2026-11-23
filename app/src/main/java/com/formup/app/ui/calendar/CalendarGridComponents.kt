// Draws calendar navigation, dates and event markers.

package com.formup.app.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.R
import com.formup.app.ui.home.components.SectionCard
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@Composable
internal fun CalendarMonthNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = FormUpColors.Surface,
        border = BorderStroke(1.dp, FormUpColors.Hairline),
        modifier = Modifier.size(48.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = description,
                tint = FormUpColors.TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun CalendarManageEventPill(onClick: () -> Unit) {
    com.formup.app.ui.common.FormUpButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Icon(Icons.Filled.EditCalendar, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(10.dp))
        Text(stringResource(R.string.calendar_add_event), fontWeight = FontWeight.SemiBold,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun CalendarMonthGrid(
    weekdayLetters: List<String>,
    days: List<CalendarDay>,
    selectedDate: Int,
    onSelectDate: (Int) -> Unit
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            weekdayLetters.forEach { letter ->
                Text(
                    text = letter,
                    fontFamily = Mono,
                    fontSize = 12.sp,
                    color = FormUpColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        days.chunked(7).forEach { week ->

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                week.forEach { day ->

                    CalendarDayCell(
                        day = day,
                        isSelected =
                            day.isInCurrentMonth &&
                                    day.date == selectedDate,

                        onClick = {
                            if (day.isInCurrentMonth) {
                                onSelectDate(day.date)
                            }
                        },

                        modifier = Modifier.weight(1f)
                    )
                }

                repeat(7 - week.size) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.85f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val markerLabel = stringResource(when (day.marker) {
        DayMarker.MATCH -> R.string.mod_calendar_match; DayMarker.TRAINING -> R.string.mod_calendar_training
        DayMarker.ACTION -> R.string.mod_calendar_both; null -> R.string.mod_calendar_none
    })
    val dateDescription = if (day.isInCurrentMonth) stringResource(R.string.mod_calendar_day, day.date, markerLabel) else ""
    val dotColor = when (day.marker) {
        DayMarker.MATCH -> FormUpColors.Primary
        DayMarker.TRAINING -> FormUpColors.AmberIcon
        DayMarker.ACTION -> FormUpColors.Danger
        null -> null
    }
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) FormUpColors.Mint else androidx.compose.ui.graphics.Color.Transparent)
            .semantics(mergeDescendants = true) { selected = isSelected; contentDescription = dateDescription }.clickable(enabled = day.isInCurrentMonth, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (day.isInCurrentMonth) day.date.toString() else "",
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> FormUpColors.PrimaryDeep
                !day.isInCurrentMonth -> FormUpColors.TextSecondary.copy(alpha = 0.4f)
                else -> FormUpColors.TextPrimary
            }
        )
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor ?: androidx.compose.ui.graphics.Color.Transparent)
        )
    }
}
