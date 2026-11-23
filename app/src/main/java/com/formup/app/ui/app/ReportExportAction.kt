// Creates and opens PDF exports for the current account.

package com.formup.app.ui.app

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.formup.app.R
import com.formup.app.export.PdfMatchSeasonExporter
import com.formup.app.export.PdfReport
import com.formup.app.security.SessionLease
import com.formup.app.security.runCatchingCancellable
import com.formup.app.ui.FormUpViewModel
import kotlinx.coroutines.launch

@Composable
internal fun rememberReportExportAction(viewModel: FormUpViewModel, session: SessionLease?): (PdfReport?, String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    return { report, missing ->
        if (report == null) viewModel.notify(missing)
        else if (!exporting && session != null) {
            exporting = true
            viewModel.notify(R.string.mod_preparing_pdf)
            scope.launch {
                try {
                    runCatchingCancellable {
                        session.check()
                        val file = PdfMatchSeasonExporter.createTheReport(context, report)
                        session.check()
                        PdfMatchSeasonExporter.open(context, file)
                    }.onFailure { error -> viewModel.notify(if (error is android.content.ActivityNotFoundException) R.string.mod_pdf_no_viewer else R.string.loc_pdf_failed) }
                } finally { exporting = false }
            }
        }
    }
}
