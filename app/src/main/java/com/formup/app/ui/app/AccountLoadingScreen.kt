// Shows loading and retry states while account data is opening.

package com.formup.app.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R

@Composable
internal fun AccountLoadingScreen(loading: Boolean, onRetry: () -> Unit, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        com.formup.app.ui.common.FormUpBrand()
        Spacer(Modifier.height(28.dp))
        if (loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.mod_loading_team), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else {
            Text(stringResource(R.string.msg_load_data_failed))
            com.formup.app.ui.common.FormUpButton(onClick = onRetry) { Text(stringResource(R.string.sync_retry)) }
        }
        TextButton(onClick = onSignOut) { Text(stringResource(R.string.sync_sign_out)) }
    }
}
