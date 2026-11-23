// Defines the sections, rows and player details used in PDF reports.

package com.formup.app.export

data class ReportMetric(val label: String, val value: String)
data class ReportSection(val title: String, val headers: List<String>, val rows: List<List<String>>,
    val weights: List<Float> = emptyList())
data class PitchPlayer(val name: String, val position: String, val x: Double, val y: Double, val goalkeeper: Boolean)
data class ReportPitch(val title: String, val formation: String, val players: List<PitchPlayer>)
data class ReportLabels(val page: String, val prepared: String, val empty: String, val continued: String)
data class PdfReport(val fileName: String, val title: String, val subtitle: String,
    val metadata: List<String>, val metrics: List<ReportMetric>, val sections: List<ReportSection>,
    val pitch: ReportPitch? = null, val notes: List<String> = emptyList(), val labels: ReportLabels,
    val localeTag: String = "en")
