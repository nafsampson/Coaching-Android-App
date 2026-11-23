// Provides the shared PDF export button style.

package com.formup.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.formup.app.ui.theme.FormUpColors

@Composable
internal fun ExportButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FormUpColors.Ink, contentColor = FormUpColors.Lime),
        border = BorderStroke(1.dp, FormUpColors.Lime.copy(alpha = .55f)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
