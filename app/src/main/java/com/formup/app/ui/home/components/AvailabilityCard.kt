// Shows availability totals for the next fixture.

package com.formup.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.home.Availability
import com.formup.app.ui.theme.FormUpColors

@Composable
fun AvailabilityCard(availability: Availability, onInfo: () -> Unit, modifier: Modifier = Modifier) {
    SectionCard(modifier.fillMaxWidth(), PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.squad_availability_title), style = MaterialTheme.typography.titleLarge, color = FormUpColors.TextPrimary)
                Text(stringResource(R.string.mod_next_fixture_status), style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
            }
            IconButton(onInfo, Modifier.size(48.dp)) { Icon(Icons.Default.Info, stringResource(R.string.mod_availability_info), tint = FormUpColors.TextSecondary) }
        }
        Spacer(Modifier.height(12.dp))
        val description = stringResource(R.string.squad_availability_message, availability.fit, availability.doubtful, availability.out, availability.total)
        Row(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)).background(FormUpColors.Hairline).semantics { contentDescription = description }) {
            listOf(availability.fit to FormUpColors.Primary, availability.doubtful to FormUpColors.AmberIcon, availability.out to FormUpColors.Danger).forEach { (count, color) ->
                if (count > 0) Box(Modifier.weight(count.toFloat()).fillMaxHeight().background(color))
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AvailabilityMetric(stringResource(R.string.fit), availability.fit, FormUpColors.Primary, Modifier.weight(1f))
            AvailabilityMetric(stringResource(R.string.doubtful), availability.doubtful, FormUpColors.AmberIcon, Modifier.weight(1f))
            AvailabilityMetric(stringResource(R.string.out), availability.out, FormUpColors.Danger, Modifier.weight(1f))
        }
    }
}
@Composable
private fun AvailabilityMetric(label: String, count: Int, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextSecondary)
    }
}
