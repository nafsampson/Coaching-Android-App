// Shows the link between the sign-in and sign-up screens.

package com.formup.app.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.formup.app.ui.theme.FormUpColors

@Composable
internal fun AuthFooter(prompt: String, action: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(prompt, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium,
            color = FormUpColors.TextSecondary, textAlign = TextAlign.Center)
        TextButton(onClick, Modifier.heightIn(min = 48.dp)) {
            Text(action, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                color = FormUpColors.Primary, textAlign = TextAlign.Center)
        }
    }
}
