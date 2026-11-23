// Builds translated lineup, match and season report content.

package com.formup.app.ui

import android.content.Context
import com.formup.app.R
import com.formup.app.data.*
import com.formup.app.export.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class ReportBuilder(private val state: SeasonState, private val context: Context, private val hasPendingWrites: () -> Boolean = { false }) {
    private fun s(id: Int, vararg args: Any) = context.getString(id, *args)
    private val locale get() = context.resources.configuration.locales[0]
    private fun number(value: Double) = String.format(locale, "%.1f", value)
    private fun date(value: String) = ServerDates.instant(value)?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", locale)) ?: s(R.string.mod_unknown_date)
    private fun labels() = ReportLabels(s(R.string.pdf_page), s(R.string.pdf_prepared), s(R.string.pdf_no_records), s(R.string.pdf_continued))
    private fun result(match: Fixture) = s(when { !match.played -> R.string.pdf_scheduled; match.teamScore > match.opponentScore -> R.string.pdf_win; match.teamScore < match.opponentScore -> R.string.pdf_loss; else -> R.string.pdf_draw })
    private fun position(raw: String) = when (raw) {
        "Goalkeeper" -> s(R.string.player_position_goalkeeper); "Defender" -> s(R.string.player_position_defender)
        "Midfielder" -> s(R.string.player_position_midfielder); "Forward" -> s(R.string.player_position_forward); else -> raw
    }
    private fun base(match: Fixture, lineupOnly: Boolean): PdfReport {
        val squad = state.matchSquadEntries[match.id] ?: match.squad
        val starters = state.playerList.filter { squad[it.id]?.selection == ApiValues.SELECTION_STARTING }
        val substitutes = state.playerList.filter { squad[it.id]?.selection == ApiValues.SELECTION_SUBSTITUTE }
        val fallback = builtInFormations.first().positions
        val pitchPlayers = starters.mapIndexed { index, player ->
            val entry = squad[player.id]
            val slot = fallback[index % fallback.size]
            PitchPlayer(player.name, entry?.assignedPosition?.ifBlank { position(player.position) } ?: position(player.position),
                entry?.pitchX ?: slot.x, entry?.pitchY ?: slot.y, player.position.equals("Goalkeeper", true) || entry?.assignedPosition == "GK")
        }
        val title = s(if (lineupOnly) R.string.pdf_lineup_title else R.string.pdf_match_title)
        val sections = mutableListOf(
            ReportSection(s(R.string.details_starting_xi), listOf(s(R.string.pdf_player), s(R.string.pdf_position)),
                pitchPlayers.map { listOf(it.name, it.position) }, listOf(2f, .8f)),
            ReportSection(s(R.string.pdf_substitutes), listOf(s(R.string.pdf_player), s(R.string.pdf_position)),
                substitutes.map { listOf(it.name, position(it.position)) }, listOf(2f, 1f))
        )
        if (!lineupOnly) sections += ReportSection(s(R.string.mod_player_performance),
            listOf(s(R.string.pdf_player), s(R.string.pdf_goals), s(R.string.pdf_assists), s(R.string.pdf_minutes), s(R.string.pdf_rating)),
            state.playerList.mapNotNull { player -> match.playerStats[player.id]?.takeIf { it.participated }?.let { stat ->
                listOf(player.name, stat.goals.toString(), stat.assists.toString(), stat.minutes.toString(), number(stat.rating))
            } }, listOf(2.7f, .7f, .7f, .8f, .8f))
        val metrics = if (lineupOnly) listOf(ReportMetric(s(R.string.details_starting_xi), starters.size.toString()), ReportMetric(s(R.string.pdf_substitutes), substitutes.size.toString()))
            else listOf(ReportMetric(s(R.string.pdf_result), match.scoreLine), ReportMetric(s(R.string.pdf_goals), match.teamScore.toString()),
                ReportMetric(s(R.string.pdf_conceded), match.opponentScore.toString()), ReportMetric(s(R.string.pdf_players), match.playerStats.values.count { it.participated }.toString()))
        val formation = state.selectedMatchFormations[match.id]?.code ?: match.formationCode.ifBlank { "4-3-3" }
        val venue = when (match.venue) { "Home" -> s(R.string.event_form_home); "Away" -> s(R.string.event_form_away); else -> match.venue }
        return PdfReport("FormUp_${if (lineupOnly) "Lineup" else "Match"}_${match.id.take(16)}.pdf", title,
            "${state.team.name} ${s(R.string.mod_versus)} ${match.opponent}", listOf(date(match.matchDateIso), "$venue · ${result(match)}"),
            metrics, sections, ReportPitch(s(R.string.pdf_tactical_board), formation, pitchPlayers).takeIf { pitchPlayers.isNotEmpty() },
            notes = (if (starters.any { squad[it.id]?.pitchX == null || squad[it.id]?.pitchY == null }) listOf(s(R.string.pdf_default_positions)) else emptyList()) + pendingNotes(),
            labels = labels(), localeTag = locale.toLanguageTag())
    }
    private fun pendingNotes() = if (hasPendingWrites()) listOf(s(R.string.pdf_pending_note)) else emptyList()
    fun lineupReport(id: String): PdfReport? = state.fixture(id)?.takeIf { match ->
        hasLineupSelection((state.matchSquadEntries[id] ?: match.squad).values, state.playerList.map { it.id }.toSet())
    }?.let { base(it, true) }
    fun matchReport(id: String): PdfReport? = state.fixture(id)?.takeIf { it.played }?.let { base(it, false) }
    fun seasonReport(): PdfReport? {
        val played = state.fixtureList.filter { it.played }.sortedBy { ServerDates.instant(it.matchDateIso) ?: Instant.MAX }
        if (played.isEmpty()) return null
        val metrics = listOf(
            ReportMetric(s(R.string.pdf_matches), played.size.toString()), ReportMetric(s(R.string.pdf_wins), played.count { it.resultLetter == "W" }.toString()),
            ReportMetric(s(R.string.pdf_draws), played.count { it.resultLetter == "D" }.toString()), ReportMetric(s(R.string.pdf_losses), played.count { it.resultLetter == "L" }.toString()),
            ReportMetric(s(R.string.pdf_goals), played.sumOf { it.teamScore }.toString()), ReportMetric(s(R.string.pdf_conceded), played.sumOf { it.opponentScore }.toString()),
            ReportMetric(s(R.string.pdf_clean_sheets), played.count { it.opponentScore == 0 }.toString()), ReportMetric(s(R.string.pdf_win_rate), "${played.count { it.resultLetter == "W" } * 100 / played.size}%")
        )
        val players = state.playerList.map { player ->
            val stats = played.mapNotNull { it.playerStats[player.id] }.filter { it.participated }
            val rated = stats.filter { it.rating > 0 }
            listOf(player.name, stats.size.toString(), stats.sumOf { it.goals }.toString(), stats.sumOf { it.assists }.toString(), stats.sumOf { it.minutes }.toString(), if (rated.isEmpty()) "-" else number(rated.map { it.rating }.average()))
        }
        return PdfReport("FormUp_Season.pdf", s(R.string.pdf_season_title), state.team.name,
            listOf("${date(played.first().matchDateIso)} - ${date(played.last().matchDateIso)}"), metrics,
            listOf(ReportSection(s(R.string.pdf_results), listOf(s(R.string.pdf_date), s(R.string.pdf_opponent), s(R.string.pdf_result)),
                played.map { listOf(date(it.matchDateIso), it.opponent, "${it.scoreLine} · ${result(it)}") }, listOf(1.2f, 1.8f, 1f)),
                ReportSection(s(R.string.pdf_season_players), listOf(s(R.string.pdf_player), s(R.string.pdf_played), s(R.string.pdf_goals), s(R.string.pdf_assists), s(R.string.pdf_minutes), s(R.string.pdf_rating)), players,
                    listOf(2.7f, .7f, .7f, .7f, .8f, .8f))), notes = pendingNotes(), labels = labels(), localeTag = locale.toLanguageTag())
    }
}
