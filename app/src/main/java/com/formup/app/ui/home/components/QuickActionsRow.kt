// Shows shortcuts to attendance, lineup and statistics.

package com.formup.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.theme.FormUpColors

@Composable
fun QuickActionsRow(onAttendance: () -> Unit, onSelection: () -> Unit, onStats: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction(stringResource(R.string.mod_attendance_short), Icons.Default.HowToReg, onAttendance, Modifier.weight(1f))
        QuickAction(stringResource(R.string.lineup), Icons.Default.Groups, onSelection, Modifier.weight(1f))
        QuickAction(stringResource(R.string.nav_stats), Icons.Default.BarChart, onStats, Modifier.weight(1f))
    }
}
@Composable
private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick, modifier.heightIn(min = 108.dp), shape = RoundedCornerShape(22.dp), color = FormUpColors.Surface) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = FormUpColors.Primary, modifier = Modifier.size(26.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = FormUpColors.TextPrimary, textAlign = TextAlign.Center)
        }
    }
}
