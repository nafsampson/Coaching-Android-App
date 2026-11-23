// Creates and shares lineup, match and season PDF files.

package com.formup.app.export

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfMatchSeasonExporter {
    suspend fun createTheReport(context: Context, report: PdfReport): File = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "pdf").apply { check(isDirectory || mkdirs()) }
        val safeName = report.fileName.removeSuffix(".pdf").replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(directory, "${safeName}_${UUID.randomUUID().toString().take(8)}.pdf")
        val document = PdfDocument()
        val writer = PdfPageWriter(document, report)
        try {
            writer.start()
            writer.paragraph(report.title, 25f, true)
            writer.paragraph(report.subtitle, 13f, color = PdfPageWriter.GREEN)
            report.metadata.filter { it.isNotBlank() }.forEach { writer.paragraph(it, 10f, color = PdfPageWriter.MUTED, gap = 3f) }
            writer.y += 12f
            writer.metrics(report.metrics)
            report.pitch?.let { PitchReportRenderer.draw(writer, it) }
            report.notes.forEach { writer.paragraph(it, 10f, color = PdfPageWriter.MUTED) }
            report.sections.forEach(writer::table)
            writer.finish()
            FileOutputStream(file).use(document::writeTo)
            file
        } catch (error: Exception) {
            file.delete()
            throw error
        } finally { document.close() }
    }
    fun open(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }
}
