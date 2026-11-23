// Shows a dropdown for choosing one value from a list.

package com.formup.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.Mono

@Composable
internal fun OptionDropdown(
    selected: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .background(FormUpColors.Surface, RoundedCornerShape(10.dp))
                .border(1.dp, if (error != null) FormUpColors.Danger else FormUpColors.Hairline, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                selected.ifBlank { placeholder },
                color = if (selected.isBlank()) FormUpColors.TextSecondary else FormUpColors.TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = FormUpColors.TextSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
        if (error != null) {
            Text(error, fontFamily = Mono, fontSize = 12.sp, color = FormUpColors.Danger, modifier = Modifier.padding(top = 4.dp, start = 2.dp))
        }
    }
}
