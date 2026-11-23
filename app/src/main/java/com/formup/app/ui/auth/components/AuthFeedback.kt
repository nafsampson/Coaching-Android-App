// Shows authentication errors and progress feedback.

package com.formup.app.ui.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import com.formup.app.R
import com.formup.app.ui.theme.FormUpColors

@Composable
internal fun AuthFeedback(busy: Boolean, error: String?) {
    if (busy) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FormUpColors.Primary,
            trackColor = FormUpColors.SurfaceRaised)
        Text(stringResource(R.string.auth_working), color = FormUpColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite })
    }
    if (error != null) {
        Surface(color = FormUpColors.DangerTint, shape = MaterialTheme.shapes.small) {
            Text(error, color = FormUpColors.Danger, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(16.dp).semantics { liveRegion = LiveRegionMode.Assertive })
        }
    }
}
