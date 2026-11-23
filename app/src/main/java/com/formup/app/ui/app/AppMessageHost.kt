// Shows app messages and clears them after display.

package com.formup.app.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.formup.app.ui.FormUpViewModel
import com.formup.app.ui.UiMessage
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun AppMessageHost(viewModel: FormUpViewModel, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val host = remember(viewModel) { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        snapshotFlow { viewModel.message }.filterNotNull().collectLatest { message ->
            val text = when (message) {
                is UiMessage.Resource -> context.getString(message.resourceId, *message.arguments.toTypedArray())
                is UiMessage.Raw -> message.text
            }
            viewModel.consumeMessage()
            host.showSnackbar(text, withDismissAction = true)
        }
    }
    Box(Modifier.fillMaxSize()) {
        content()
        SnackbarHost(host, Modifier.align(Alignment.BottomCenter).imePadding().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 88.dp))
    }
}
