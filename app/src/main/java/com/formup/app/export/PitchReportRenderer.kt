// Draws the saved lineup and player positions in a PDF.

package com.formup.app.export

import android.graphics.*
import kotlin.math.*

internal object PitchReportRenderer {
    fun draw(writer: PdfPageWriter, pitch: ReportPitch) {
        val height = 388f
        writer.ensure(height + 58f)
        writer.paragraph("${pitch.title} · ${pitch.formation}", 16f, true)
        val canvas = writer.canvas
        val left = PdfPageWriter.MARGIN
        val top = writer.y
        val width = writer.contentWidth
        canvas.drawRoundRect(left, top, left + width, top + height, 14f, 14f, writer.paint(color = Color.rgb(18, 62, 53)))
        for (stripe in 0..7) if (stripe % 2 == 0) {
            canvas.drawRect(left + 12f, top + 12f + stripe * (height - 24f) / 8f, left + width - 12f,
                top + 12f + (stripe + 1) * (height - 24f) / 8f, writer.paint(color = Color.rgb(24, 71, 59)))
        }
        val line = writer.paint(color = Color.rgb(118, 161, 143)).apply { style = Paint.Style.STROKE; strokeWidth = 1.1f }
        val inset = 16f
        canvas.drawRect(left + inset, top + inset, left + width - inset, top + height - inset, line)
        canvas.drawLine(left + inset, top + height / 2, left + width - inset, top + height / 2, line)
        canvas.drawCircle(left + width / 2, top + height / 2, 43f, line)
        for (bottom in listOf(false, true)) {
            val y = if (bottom) top + height - inset - 60f else top + inset
            canvas.drawRect(left + width * .28f, y, left + width * .72f, y + 60f, line)
        }
        pitch.players.forEach { player ->
            val x = (left + width * player.x.toFloat() / 100f).coerceIn(left + 48f, left + width - 48f)
            val y = (top + height * player.y.toFloat() / 100f).coerceIn(top + 32f, top + height - 40f)
            val jersey = Path().apply {
                moveTo(x - 9f, y - 15f); lineTo(x - 20f, y - 9f); lineTo(x - 15f, y + 1f); lineTo(x - 10f, y - 2f)
                lineTo(x - 10f, y + 17f); lineTo(x + 10f, y + 17f); lineTo(x + 10f, y - 2f)
                lineTo(x + 15f, y + 1f); lineTo(x + 20f, y - 9f); lineTo(x + 9f, y - 15f); close()
            }
            canvas.drawPath(jersey, writer.paint(color = if (player.goalkeeper) Color.rgb(247, 201, 105) else PdfPageWriter.LIME))
            val name = writer.paint(8.5f, true, Color.WHITE)

            val lines = PdfTextLayout.wrap(player.name, 88f, name::measureText)
            lines.take(2).forEachIndexed { index, text ->
                canvas.drawText(text, x - name.measureText(text) / 2, y + 30f + index * 11f, name)
            }
        }
        writer.y = top + height + 24f
    }
}
