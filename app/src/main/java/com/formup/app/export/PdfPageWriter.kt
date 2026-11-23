// Draws PDF headings and tables and starts new pages when needed.

package com.formup.app.export

import android.graphics.*
import androidx.core.graphics.PathParser
import android.graphics.pdf.PdfDocument
import kotlin.math.*

internal class PdfPageWriter(private val document: PdfDocument, private val report: PdfReport) {
    companion object {
        const val MARGIN = 36f
        const val WIDTH = 595f
        const val BOTTOM = 792f
        val INK = Color.rgb(16, 35, 38)
        val GREEN = Color.rgb(20, 99, 76)
        val LIME = Color.rgb(213, 246, 106)
        val MUTED = Color.rgb(83, 105, 108)
        val RULE = Color.rgb(218, 228, 224)
    }
    private var page: PdfDocument.Page? = null
    private var number = 0
    var y = 0f
    val canvas get() = requireNotNull(page).canvas
    val contentWidth get() = WIDTH - 2 * MARGIN
    fun paint(size: Float = 10f, bold: Boolean = false, color: Int = INK): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; this.color = color; typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }
    fun start() {
        finish()
        number++
        page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, number).create())
        canvas.drawColor(Color.WHITE)
        canvas.drawRect(0f, 0f, WIDTH, 64f, paint(color = INK))
        canvas.drawRoundRect(36f, 16f, 66f, 46f, 8f, 8f, paint(color = LIME))
        canvas.save()
        canvas.translate(39f, 16f); canvas.scale(.5f, .5f)
        PathParser.createPathFromPathData("M12,13 L34,13 L30,20 L20,20 L18,25 L28,25 L24,32 L16,32 L12,42 L4,42 Z M33,25 L43,9 L47,25 L42,23 L34,37 L28,34 L36,21 Z")?.let { canvas.drawPath(it, paint(color = INK)) }
        canvas.restore()
        canvas.drawText("FormUp", 77f, 38f, paint(18f, true, Color.WHITE))
        canvas.drawRect(0f, 64f, WIDTH, 68f, paint(color = LIME))
        y = 94f
    }
    fun finish() {
        page?.let {
            val footer = paint(8f, color = MUTED)
            canvas.drawLine(MARGIN, 807f, WIDTH - MARGIN, 807f, paint(color = RULE))
            canvas.drawText(report.labels.prepared, MARGIN, 823f, footer)
            val pageText = "${report.labels.page} $number"
            canvas.drawText(pageText, WIDTH - MARGIN - footer.measureText(pageText), 823f, footer)
            document.finishPage(it)
        }
        page = null
    }
    fun ensure(height: Float) { if (y + height > BOTTOM) start() }
    fun paragraph(text: String, size: Float = 11f, bold: Boolean = false, color: Int = INK, gap: Float = 8f) {
        val p = paint(size, bold, color)
        val leading = size * 1.4f
        PdfTextLayout.wrap(text, contentWidth) { p.measureText(it) }.forEach { line ->
            ensure(leading)
            canvas.drawText(line, MARGIN, y + size, p)
            y += leading
        }
        y += gap
    }
    fun metrics(metrics: List<ReportMetric>) {
        metrics.chunked(4).forEach { row ->
            val width = (contentWidth - 9f * (row.size - 1)) / row.size
            val labelPaint = paint(9f, color = MUTED)
            val labels = row.map { PdfTextLayout.wrap(it.label, width - 20f, labelPaint::measureText) }
            val height = max(70f, 45f + labels.maxOf { it.size } * 13f)
            ensure(height + 16f)
            row.forEachIndexed { index, metric ->
                val x = MARGIN + index * (width + 9f)
                canvas.drawRoundRect(x, y, x + width, y + height, 10f, 10f, paint(color = Color.rgb(242, 246, 243)))
                canvas.drawText(metric.value, x + 10f, y + 28f, paint(21f, true, GREEN))
                labels[index].forEachIndexed { n, label -> canvas.drawText(label, x + 10f, y + 47f + n * 13f, labelPaint) }
            }
            y += height + 12f
        }
    }
    fun table(section: ReportSection) {
        val columns = section.headers.size
        if (columns == 0) return
        val weights = section.weights.takeIf { it.size == columns } ?: List(columns) { if (it == 0) 2f else 1f }
        val widths = weights.map { contentWidth * it / weights.sum() }
        val body = paint(9.5f)
        val head = paint(9f, true, Color.WHITE)
        val headers = section.headers.mapIndexed { i, label -> PdfTextLayout.wrap(label, widths[i] - 16f, head::measureText) }
        val headHeight = max(30f, headers.maxOf { it.size } * 13f + 16f)
        ensure(40f + headHeight + 32f)
        paragraph(section.title, 16f, true, gap = 10f)
        fun drawHeader() {
            canvas.drawRoundRect(MARGIN, y, WIDTH - MARGIN, y + headHeight, 6f, 6f, paint(color = INK))
            var x = MARGIN
            headers.forEachIndexed { i, lines ->
                lines.forEachIndexed { n, line -> canvas.drawText(line, x + 8f, y + 17f + n * 13f, head) }
                x += widths[i]
            }
            y += headHeight
        }
        fun continuation() { start(); paragraph("${section.title} · ${report.labels.continued}", 14f, true); drawHeader() }
        drawHeader()
        if (section.rows.isEmpty()) paragraph(report.labels.empty, color = MUTED)
        section.rows.forEachIndexed { rowIndex, row ->
            val cells = List(columns) { index -> PdfTextLayout.wrap(row.getOrElse(index) { "" }, widths[index] - 16f, body::measureText) }
            val maxLines = cells.maxOf { it.size }
            val rowHeight = maxLines * 14f + 16f
            if (y + min(rowHeight, 44f) > BOTTOM || (rowHeight < 600f && y + rowHeight > BOTTOM)) continuation()
            var offset = 0
            while (offset < maxLines) {
                if (BOTTOM - y < 30f) continuation()
                val count = min(maxLines - offset, floor((BOTTOM - y - 16f) / 14f).toInt().coerceAtLeast(1))
                val height = count * 14f + 16f
                canvas.drawRect(MARGIN, y, WIDTH - MARGIN, y + height, paint(color = if (rowIndex % 2 == 0) Color.rgb(245, 248, 246) else Color.WHITE))
                var x = MARGIN
                cells.forEachIndexed { i, lines ->
                    lines.drop(offset).take(count).forEachIndexed { n, line -> canvas.drawText(line, x + 8f, y + 18f + n * 14f, body) }
                    x += widths[i]
                }
                canvas.drawLine(MARGIN, y + height, WIDTH - MARGIN, y + height, paint(color = RULE))
                y += height; offset += count
                if (offset < maxLines) continuation()
            }
        }
        y += 22f
    }
}
