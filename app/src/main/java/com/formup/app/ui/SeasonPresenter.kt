// Converts season data into the values used by each screen.

package com.formup.app.ui

import android.app.Application
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SportsScore
import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.data.AvailabilityStatus
import com.formup.app.data.Fixture
import com.formup.app.data.MatchLine
import com.formup.app.data.NotificationKind
import com.formup.app.data.Player
import com.formup.app.ui.calendar.LineupPlayer
import com.formup.app.ui.calendar.MatchDetailsUiState
import com.formup.app.ui.calendar.SubPlayer
import com.formup.app.ui.home.Availability
import com.formup.app.ui.home.HomeUiState
import com.formup.app.ui.home.LastMatch
import com.formup.app.ui.home.NextMatch
import com.formup.app.ui.notifications.NotificationGroup
import com.formup.app.ui.notifications.NotificationItem
import com.formup.app.ui.notifications.NotificationsUiState
import com.formup.app.ui.profile.ActivityItem
import com.formup.app.ui.profile.ActivityTone
import com.formup.app.ui.profile.ProfileUiState
import com.formup.app.ui.profile.SeasonStat
import com.formup.app.ui.profile.SupportedLanguages
import com.formup.app.ui.profile.TeamInfo
import com.formup.app.ui.stats.FixtureOption
import com.formup.app.ui.stats.LastMatchAnalysis
import com.formup.app.ui.stats.MatchReportUiState
import com.formup.app.ui.stats.MatchStatRow
import com.formup.app.ui.stats.PerformanceSummary
import com.formup.app.ui.stats.PlayerRating
import com.formup.app.ui.stats.PlayerStatEntry
import com.formup.app.ui.stats.StatsInputUiState
import com.formup.app.ui.stats.StatsUiState
import com.formup.app.ui.stats.SummaryStat
import com.formup.app.ui.stats.TeamScoreInfo
import com.formup.app.ui.stats.TopPerformer
import com.formup.app.ui.team.PlayerStatus
import com.formup.app.ui.team.SquadPlayer
import com.formup.app.ui.team.TeamUiState
import com.formup.app.ui.theme.FormUpColors
import kotlin.math.roundToInt

internal class SeasonPresenter(
    private val state: SeasonState,
    private val application: Application
) {
    val unreadCount: Int get() = state.notificationList.count { !it.isRead }

    val homeState: HomeUiState
        get() {
            val next = state.nextFixture
            val last = state.lastPlayedFixture
            return HomeUiState(
                coachName = "${application.getString(R.string.coach)} ${state.coach.name.substringBefore(' ')}",
                prompt = next
                    ?.let { "Next up: ${it.opponent} on ${it.dateLabel}. Squad availability: ${availabilityMood()}." }
                    ?: "No fixtures scheduled.",
                nextMatch = NextMatch(
                    opponent = next?.let { "vs. ${it.opponent}" } ?: "No upcoming match",
                    date = next?.dateLabel ?: "—",
                    kickoff = next?.let { "${it.kickoff} Kickoff" } ?: "—",
                    venue = next?.venue ?: "—",
                    teamName = state.team.name
                ),
                availability = Availability(
                    fit = state.playerList.count { it.status == AvailabilityStatus.Fit },
                    doubtful = state.playerList.count { it.status == AvailabilityStatus.Doubtful },
                    out = state.playerList.count { it.status == AvailabilityStatus.Out }
                ),
                lastMatch = LastMatch(
                    homeTeam = state.team.name,
                    homeScore = last?.teamScore ?: 0,
                    awayTeam = last?.opponent ?: "—",
                    awayScore = last?.opponentScore ?: 0,
                    status = if (last != null) "FT" else "—",
                    stats = last?.let {
                        listOf(
                            "Assists" to "${it.playerStats.values.sumOf { s -> s.assists }}",
                            "Clean Sheet" to if (it.opponentScore == 0) "Yes" else "No"
                        )
                    } ?: emptyList()
                ),
                unreadNotifications = unreadCount > 0
            )
        }

    private fun availabilityMood(): String {
        val out = state.playerList.count { it.status != AvailabilityStatus.Fit }
        return if (out <= 2) "healthy" else "thin"
    }

    val statsState: StatsUiState
        get() {
            val played = state.playedFixtures
            val matches = played.size.coerceAtLeast(1)
            val wins = played.count { it.teamScore > it.opponentScore }
            val cleanSheets = played.count { it.opponentScore == 0 }
            val goals = played.sumOf { it.teamScore }
            val winRate = (wins * 100f / matches).roundToInt()
            val goalsPerMatch = goals.toFloat() / matches

            return StatsUiState(
                squadName = state.team.squad,
                summary = listOf(
                    SummaryStat(
                        label = application.getString(R.string.stats_win_rate),
                        value = "$winRate%",
                        delta = if (winRate >= 50) "↑" else null,
                        progress = winRate / 100f,
                        barColor = FormUpColors.Primary,
                        highlighted = true
                    ),
                    SummaryStat(
                        label = application.getString(R.string.stats_goals_per_match),
                        value = String.format("%.1f", goalsPerMatch),
                        progress = (goalsPerMatch / 5f).coerceIn(0f, 1f),
                        barColor = FormUpColors.Amber
                    ),
                    SummaryStat(
                        label = application.getString(R.string.stats_clean_sheets),
                        value = application.getString(R.string.stats_clean_sheets_value, cleanSheets.toString(), played.size.toString()),
                        progress = cleanSheets.toFloat() / matches,
                        barColor = FormUpColors.Primary
                    ),
                    SummaryStat(
                        label = application.getString(R.string.stats_conceded_per_match),
                        value = String.format("%.1f", played.sumOf { it.opponentScore }.toFloat() / matches),
                        progress = (played.sumOf { it.opponentScore }.toFloat() / matches / 5f).coerceIn(0f, 1f),
                        barColor = FormUpColors.Amber
                    )
                ),
                goalsLeaders = leaders { state.goalsOf(it) to "Goals" },
                assistsLeaders = leaders { state.assistsOf(it) to "Assists" },
                lastMatch = state.lastPlayedFixture.let { fixture ->
                    val assists = fixture?.playerStats?.values?.sumOf { it.assists } ?: 0
                    LastMatchAnalysis(
                        opponent = fixture?.opponent ?: "—",
                        resultText = fixture?.resultText ?: "—",
                        rows = listOf(
                            MatchStatRow(label = application.getString(R.string.stats_goals_scored), valueText = "${fixture?.teamScore ?: 0}",
                                progress = ((fixture?.teamScore ?: 0) / 5f).coerceIn(0f, 1f), barColor = FormUpColors.Primary),
                            MatchStatRow(label = application.getString(R.string.stats_goals_conceded), valueText = "${fixture?.opponentScore ?: 0}",
                                progress = ((fixture?.opponentScore ?: 0) / 5f).coerceIn(0f, 1f), barColor = FormUpColors.Amber),
                            MatchStatRow(label = application.getString(R.string.assists), valueText = "$assists",
                                progress = (assists / 5f).coerceIn(0f, 1f), barColor = FormUpColors.Primary)
                        )
                    )
                }
            )
        }

    private fun leaders(selector: (Player) -> Pair<Int, String>): List<TopPerformer> =
        state.playerList
            .map { it to selector(it) }
            .filter { it.second.first > 0 }
            .sortedByDescending { it.second.first }
            .take(3)
            .mapIndexed { index, (player, stat) ->
                TopPerformer(
                    name = player.name,
                    position = player.position,
                    matches = state.matchesOf(player),
                    statValue = "${stat.first} ${stat.second}",
                    badgeColor = if (index == 0) FormUpColors.Amber else FormUpColors.NavIndicator,
                    badgeTextColor = if (index == 0) FormUpColors.Surface else FormUpColors.TextPrimary
                )
            }

    val profileState: ProfileUiState
        get() {
            val played = state.playedFixtures
            val teamGoals = played.sumOf { it.teamScore }
            val assists = state.playerList.sumOf { state.assistsOf(it) }
            return ProfileUiState(
                name = state.coach.name,

                seasonStats = listOf(
                    SeasonStat("Goals", teamGoals.toString(), Icons.Filled.SportsScore),
                    SeasonStat("Assists", assists.toString(), Icons.Filled.Handshake),
                    SeasonStat("Matches", played.size.toString(), Icons.Filled.EventAvailable),
                    SeasonStat("Wins", played.count { it.teamScore > it.opponentScore }.toString(), Icons.Filled.EmojiEvents)                ),
                recentActivity = state.activityList.take(4).map {
                    ActivityItem(
                        title = it.title,
                        subtitle = it.subtitle,
                        date = it.date,
                        tone = if (it.isTraining) ActivityTone.Training else ActivityTone.Match
                    )
                },
                teamInfo = TeamInfo(teamName = state.team.name, squad = state.team.squad),
                language = SupportedLanguages.firstOrNull { it.code == state.coach.languageCode }
                    ?: SupportedLanguages.first(),
                matchReminders = state.coach.matchReminders,
                weeklySummary = state.coach.weeklySummary,
                unreadNotifications = unreadCount > 0
            )
        }

    val notificationsState: NotificationsUiState
        get() = NotificationsUiState(
            items = state.notificationList.map { notification ->
                NotificationItem(
                    id = notification.id,
                    icon = when (notification.kind) {
                        NotificationKind.Attendance -> Icons.Filled.NotificationsActive
                        NotificationKind.Schedule -> Icons.Filled.CalendarMonth
                        NotificationKind.Fitness -> Icons.Filled.MonitorHeart
                        NotificationKind.Stats -> Icons.Filled.QueryStats
                    },
                    iconBackground = when (notification.kind) {
                        NotificationKind.Schedule -> FormUpColors.AmberTint
                        else -> FormUpColors.PrimaryTint
                    },
                    iconTint = when (notification.kind) {
                        NotificationKind.Schedule -> FormUpColors.AmberIcon
                        NotificationKind.Attendance -> FormUpColors.Primary
                        else -> FormUpColors.TextSecondary
                    },
                    title = notification.title,
                    timestamp = notification.timestamp,
                    body = notification.body,
                    group = if (notification.isRead) NotificationGroup.Earlier else NotificationGroup.New,
                    actionLabel = notification.actionLabel
                )
            }
        )

    val unreadNotificationIds: Set<String>
        get() = state.notificationList.filter { !it.isRead }.map { it.id }.toSet()

    fun statsInputState(fixtureId: String): StatsInputUiState {
        val match = state.fixture(fixtureId) ?: state.fixtureList.firstOrNull() ?: return StatsInputUiState(
            fixtureId = fixtureId,
            resultBadge = "NO MATCHES",
            matchDate = "—",
            opponent = "—",

            homeLabel = state.team.name.ifBlank { "TEAM" }.uppercase(),
            awayLabel = "—",
            homeScore = 0,
            awayScore = 0,
            availableFixtures = emptyList(),
            players = emptyList()
        )
        return StatsInputUiState(
            fixtureId = match.id,
            resultBadge = if (match.played) "FT ${match.scoreLine} (${match.resultLetter})" else "NOT PLAYED",
            matchDate = listOf(match.dateLabel, match.matchDateIso.take(4)).filter { it.isNotBlank() }.joinToString(", "),
            opponent = "vs. ${match.opponent}",
            homeLabel = state.team.name.uppercase(),
            awayLabel = match.opponent.uppercase(),
            homeScore = match.teamScore,
            awayScore = match.opponentScore,
            availableFixtures = state.fixtureList.map {
                FixtureOption(it.id, "${it.opponent} • ${it.dateLabel}")
            },
            players = state.playerList.map { player ->
                val line = match.playerStats[player.id]
                val starting = state.matchSquadSelections[match.id]?.get(player.id) == ApiValues.SELECTION_STARTING
                PlayerStatEntry(
                    id = player.id,
                    name = player.name,
                    position = player.position,
                    goals = line?.goals ?: 0,
                    assists = line?.assists ?: 0,
                    minutesPlayed = line?.minutes ?: 0,
                    rating = line?.rating ?: 0.0,
                    badgeColor = if (starting) FormUpColors.Primary else FormUpColors.NavIndicator,
                    badgeTextColor = if (starting) FormUpColors.Surface else FormUpColors.TextPrimary
                )
            }
        )
    }

    fun matchReportState(fixtureId: String): MatchReportUiState {
        val match = state.fixture(fixtureId) ?: state.fixtureList.firstOrNull() ?: return MatchReportUiState(
            statusLine = "NO MATCHES",
            awayTeam = TeamScoreInfo("—", "—", 0),
            homeTeam = TeamScoreInfo(state.team.name.take(3).uppercase(), state.team.name.ifBlank { "Team" }, 0, true),
            overview = "No match data is available yet.",
            myPerformance = PerformanceSummary("—", "NO STATS", "0'", "0", "0"),
            ratings = emptyList()
        )
        val lines = match.playerStats
        val ratings = lines.entries
            .filter { it.value.participated }
            .mapNotNull { (playerId, line) -> state.player(playerId)?.let { it to line } }
            .sortedByDescending { ratingValue(it.second) }
            .mapIndexed { index, (player, line) ->
                PlayerRating(
                    rank = index + 1,
                    name = player.name,
                    note = buildString {
                        append(player.position.take(3).uppercase())
                        if (line.goals > 0) append(" · ${line.goals} Goal${if (line.goals > 1) "s" else ""}")
                        if (line.assists > 0) append(" · ${line.assists} Assist${if (line.assists > 1) "s" else ""}")
                    },
                    rating = String.format("%.1f", ratingValue(line)),
                    isStandout = index == 0
                )
            }

        val best = lines.entries.filter { it.value.participated }.maxByOrNull { ratingValue(it.value) }
        return MatchReportUiState(
            statusLine = "${if (match.played) "FULL TIME" else "SCHEDULED"} · ${match.dateLabel.uppercase()} · ${match.venue.uppercase()}",
            awayTeam = TeamScoreInfo(
                abbreviation = match.opponent.take(3).uppercase(),
                name = match.opponent,
                score = match.opponentScore
            ),
            homeTeam = TeamScoreInfo(
                abbreviation = state.team.name.take(3).uppercase(),
                name = state.team.name,
                score = match.teamScore,
                isFormUp = true
            ),
            overview = overviewFor(match),
            myPerformance = PerformanceSummary(
                rating = best?.let { String.format("%.1f", ratingValue(it.value)) } ?: "—",
                ratingLabel = best?.let { state.player(it.key)?.name?.uppercase() } ?: "NO STATS LOGGED",
                minutes = "${best?.value?.minutes ?: 0}'",
                goals = "${best?.value?.goals ?: 0}",
                assists = "${best?.value?.assists ?: 0}"
            ),
            ratings = ratings
        )
    }

    private fun ratingValue(line: MatchLine): Double = line.rating

    private fun overviewFor(fixture: Fixture): String = when {
        !fixture.played -> "This fixture hasn't been played yet. Log the result from Stats Input once it's done."
        fixture.teamScore > fixture.opponentScore -> "${state.team.name} beat ${fixture.opponent} ${fixture.scoreLine}."
        fixture.teamScore < fixture.opponentScore -> "${state.team.name} lost ${fixture.scoreLine} to ${fixture.opponent}."
        else -> "${state.team.name} drew ${fixture.scoreLine} with ${fixture.opponent}."
    }

    fun matchDetailsState(fixtureId: String): MatchDetailsUiState {

        val match = state.fixture(fixtureId)

        val squadEntries =
            state.matchSquadEntries[fixtureId] ?: match?.squad.orEmpty()

        val startingXi = squadEntries
            .filter { it.value.selection.equals("Starting", ignoreCase = true) }
            .entries.mapIndexedNotNull { index, (playerId, entry) ->

                val selectedPlayer = state.player(playerId)
                    ?: return@mapIndexedNotNull null

                LineupPlayer(
                    name = selectedPlayer.name,
                    xPercent = ((entry.pitchX ?: builtInFormations.first().positions[index % 11].x) / 100.0).toFloat(),
                    yPercent = ((entry.pitchY ?: builtInFormations.first().positions[index % 11].y) / 100.0).toFloat(),
                    isGoalkeeper =
                        entry.assignedPosition.equals("GK", ignoreCase = true) ||
                                entry.assignedPosition.equals("Goalkeeper", ignoreCase = true) ||
                                selectedPlayer.position.equals("GK", ignoreCase = true) ||
                                selectedPlayer.position.equals("Goalkeeper", ignoreCase = true)
                )
            }

        val substitutes = squadEntries
            .filter { it.value.selection.equals("Substitute", ignoreCase = true) }
            .mapNotNull { (playerId, entry) ->

                val selectedPlayer = state.player(playerId)
                    ?: return@mapNotNull null

                SubPlayer(
                    name = selectedPlayer.name,
                    position = entry.assignedPosition
                        .ifBlank { selectedPlayer.position }
                )
            }

        val inCount = squadEntries.values.count {
            it.status.equals("Available", ignoreCase = true) ||
                    it.status.equals("Fit", ignoreCase = true)
        }

        val outCount = squadEntries.values.count {
            it.status.equals("Injured", ignoreCase = true) ||
                    it.status.equals("Out", ignoreCase = true)
        }

        val tbdCount = squadEntries.values.count {
            it.status.equals("Knock", ignoreCase = true) ||
                    it.status.equals("Doubtful", ignoreCase = true)
        }

        val formation = match?.formationCode
            ?.takeIf { it.isNotBlank() }
            ?: match?.formationName
                ?.takeIf { it.isNotBlank() }
            ?: "Starting XI"

        return MatchDetailsUiState(
            title = application.getString(R.string.loc_match_vs, match?.opponent ?: "Opponent"),
            dateLabel = match?.dateLabel ?: "",
            timeLabel = match?.kickoff ?: "",
            venueName = match?.venue ?: application.getString(R.string.loc_venue_unset),
            venueDetail = "",
            inCount = inCount,
            outCount = outCount,
            tbdCount = tbdCount,
            squadSize = state.playerList.size,
            formation = formation,
            startingXi = startingXi,
            substitutes = substitutes
        )
    }
    val teamState: TeamUiState
        get() = TeamUiState(
            players = state.playerList.map { player ->
                SquadPlayer(
                    id = player.id,
                    fullName = player.name,
                    position = player.position,
                    status = when (player.status) {
                        AvailabilityStatus.Fit -> PlayerStatus.FIT
                        AvailabilityStatus.Doubtful -> PlayerStatus.DOUBT
                        AvailabilityStatus.Out -> PlayerStatus.OUT
                    }
                )
            }
        )

}
