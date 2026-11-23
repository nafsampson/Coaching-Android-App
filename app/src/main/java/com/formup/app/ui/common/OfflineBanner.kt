// Shows connection status, pending changes and failed uploads.

package com.formup.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import com.formup.app.R

fun offlineBannerText(isOnline: Boolean, pendingChanges: Int, showingCachedData: Boolean): String? = when {
    pendingChanges > 0 && !isOnline -> "No connection. $pendingChanges changes are safe on this phone."
    pendingChanges > 0 -> "$pendingChanges saved changes are waiting to update."
    !isOnline -> "No connection. Showing your last saved data."
    showingCachedData -> "Showing your last saved data."
    else -> null
}

@Composable
fun OfflineBanner(isOnline: Boolean, pendingChanges: Int, showingCachedData: Boolean,
    failedChanges: List<String> = emptyList(), onRetry: () -> Unit = {}) {
    val text = when {
        pendingChanges > 0 && !isOnline -> stringResource(R.string.mod_offline_pending, pendingChanges)
        pendingChanges > 0 -> stringResource(R.string.mod_online_pending, pendingChanges)
        !isOnline -> stringResource(R.string.mod_offline_saved)
        showingCachedData -> stringResource(R.string.mod_cached_data)
        else -> null
    }
    var reviewing by remember { mutableStateOf(false) }
    if (text != null || failedChanges.isNotEmpty()) {
        Column(Modifier.fillMaxWidth().background(if (failedChanges.isEmpty()) Color(0xFF455A64) else Color(0xFF8B2D24)).padding(horizontal = 16.dp, vertical = 10.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
            Text(if (failedChanges.isEmpty()) text.orEmpty() else stringResource(R.string.sync_failed, failedChanges.size), color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (failedChanges.isNotEmpty()) TextButton(onClick = { reviewing = true }) { Text(stringResource(R.string.sync_review), color = Color.White) }
                if (pendingChanges > 0) TextButton(onClick = onRetry) { Text(stringResource(R.string.sync_retry), color = Color.White) }
            }
        }
    }
    if (reviewing) AlertDialog(onDismissRequest = { reviewing = false },
        title = { Text(stringResource(R.string.sync_review)) },
        text = { androidx.compose.foundation.lazy.LazyColumn { items(failedChanges.size) { Text(failedChanges[it], Modifier.padding(vertical = 6.dp)) } } },
        confirmButton = { TextButton(onClick = { onRetry(); reviewing = false }) { Text(stringResource(R.string.sync_retry)) } },
        dismissButton = { TextButton(onClick = { reviewing = false }) { Text(stringResource(R.string.ui_cancel)) } })
}
