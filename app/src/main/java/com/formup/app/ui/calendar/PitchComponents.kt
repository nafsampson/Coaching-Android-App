// Draws the football pitch and player shirt markers.

package com.formup.app.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.formup.app.ui.theme.FormUpColors

@Composable
internal fun FootballPitch(modifier: Modifier = Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(22.dp)).background(FormUpColors.Pitch)) {
        val inset = 12.dp.toPx()
        val area = Size(size.width - 2 * inset, size.height - 2 * inset)
        for (stripe in 0..7) if (stripe % 2 == 0) drawRect(Color(0xFF194B3C), Offset(inset, inset + stripe * area.height / 8), Size(area.width, area.height / 8))
        val line = Color(0xFF8EB5A0)
        val stroke = 1.2.dp.toPx()
        drawRect(line, Offset(inset, inset), area, style = Stroke(stroke))
        drawLine(line, Offset(inset, size.height / 2), Offset(size.width - inset, size.height / 2), stroke)
        drawCircle(line, size.width * .12f, Offset(size.width / 2, size.height / 2), style = Stroke(stroke))
        for (bottom in listOf(false, true)) {
            val h = area.height * .15f
            drawRect(line, Offset(size.width * .25f, if (bottom) size.height - inset - h else inset), Size(size.width * .5f, h), style = Stroke(stroke))
        }
    }
}
@Composable
internal fun JerseyMarker(name: String, goalkeeper: Boolean, selected: Boolean = false, showName: Boolean = true, modifier: Modifier = Modifier) {
    Column(modifier.width(if (showName) 76.dp else 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                val shirt = Path().apply {
                    moveTo(w*.28f,h*.15f); lineTo(w*.05f,h*.28f); lineTo(w*.15f,h*.52f); lineTo(w*.28f,h*.45f)
                    lineTo(w*.28f,h*.9f); lineTo(w*.72f,h*.9f); lineTo(w*.72f,h*.45f); lineTo(w*.85f,h*.52f)
                    lineTo(w*.95f,h*.28f); lineTo(w*.72f,h*.15f); lineTo(w*.6f,h*.22f); lineTo(w*.4f,h*.22f); close()
                }
                drawPath(shirt, if (goalkeeper) Color(0xFFF7C969) else FormUpColors.Lime)
                if (selected) drawPath(shirt, Color.White, style = Stroke(2.dp.toPx()))
            }
        }
        if (showName) Surface(color = FormUpColors.Ink.copy(alpha = .94f), shape = RoundedCornerShape(6.dp)) {
            Text(name, Modifier.padding(horizontal = 4.dp, vertical = 3.dp), color = Color.White, fontSize = 12.sp,
                lineHeight = 15.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
