// Opens the selected screen and connects its actions to the view model.

package com.formup.app.ui.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.export.PdfReport
import com.formup.app.ui.FormUpViewModel
import com.formup.app.ui.calendar.AttendanceScreen
import com.formup.app.ui.calendar.CalendarScreen
import com.formup.app.ui.calendar.LineupBuilderScreen
import com.formup.app.ui.calendar.LineupSelectionScreen
import com.formup.app.ui.calendar.ManageEventScreen
import com.formup.app.ui.calendar.MatchDetailsScreen
import com.formup.app.ui.common.OfflineBanner
import com.formup.app.ui.home.HomeScreen
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.navigation.Destination
import com.formup.app.ui.notifications.NotificationsScreen
import com.formup.app.ui.profile.EditProfileScreen
import com.formup.app.ui.profile.ProfileScreen
import com.formup.app.ui.stats.MatchReportScreen
import com.formup.app.ui.stats.StatsInputScreen
import com.formup.app.ui.stats.StatsScreen
import com.formup.app.ui.team.AddPlayerScreen
import com.formup.app.ui.team.TeamScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun FormUpContent(
    viewModel: FormUpViewModel,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    biometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    exportReportToPdf: (PdfReport?, String) -> Unit,
    hasUnread: Boolean,
    openNotifications: () -> Unit,
    selectTab: (HomeTab) -> Unit
) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        OfflineBanner(
            viewModel.isOnline,
            viewModel.pendingSyncCount,
            viewModel.showingCachedData,
            failedChanges = viewModel.failedChanges,
            onRetry = viewModel::retryPendingChanges
        )

        Box(Modifier.weight(1f)) {

            when (val screen = viewModel.current) {

                Destination.Home -> HomeScreen(
                    state = viewModel.presentation.homeState,
                    onNotifications = openNotifications,
                    onStats = { viewModel.selectTab(HomeTab.Stats) },
                    onMatchDetails = {
                        viewModel.nextFixture?.let {
                            viewModel.navigate(Destination.MatchDetails(it.id))
                        } ?: viewModel.notify(R.string.msg_no_upcoming_fixture)
                    },
                    onLineup = {
                        viewModel.nextFixture?.let {
                            viewModel.navigate(Destination.Lineup(it.id))
                        } ?: viewModel.notify(R.string.msg_no_upcoming_fixture)
                    },
                    onAttendance = viewModel::openTrainingAttendance,
                    onViewResults = {
                        viewModel.lastPlayedFixture?.let {
                            viewModel.navigate(Destination.MatchReport(it.id))
                        } ?: viewModel.selectTab(HomeTab.Home)
                    },
                    onSelectTab = selectTab
                )

                Destination.Stats -> StatsScreen(
                    state = viewModel.presentation.statsState,
                    hasUnread = hasUnread,
                    onInputStats = {
                        val fixture = viewModel.lastPlayedFixture ?: viewModel.nextFixture
                        fixture?.let {
                            viewModel.navigate(Destination.StatsInput(it.id))
                        } ?: viewModel.notify(R.string.msg_no_fixtures_to_log)
                    },
                    onExportPdf = {
                        exportReportToPdf(
                            viewModel.reports.seasonReport(),
                            context.getString(R.string.loc_no_match_report)
                        )
                    },
                    onViewFullMatchReport = {
                        viewModel.lastPlayedFixture?.let {
                            viewModel.navigate(Destination.MatchReport(it.id))
                        } ?: viewModel.notify(R.string.msg_no_match_played)
                    },
                    onNotifications = openNotifications,
                    onSelectTab = selectTab
                )

                Destination.Profile -> ProfileScreen(
                    state = viewModel.presentation.profileState,
                    biometricEnabled = biometricEnabled,
                    onBiometricToggle = onBiometricToggle,
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange,
                    onNotifications = openNotifications,
                    onEditProfile = { viewModel.navigate(Destination.EditProfile) },
                    onEditTeamInfo = { viewModel.navigate(Destination.EditProfile) },
                    onLanguageSelected = { option ->
                        viewModel.profile.setLanguage(option)

                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(option.code)
                        )
                    },
                    onMatchRemindersChange = viewModel.profile::setMatchReminders,
                    onWeeklySummaryChange = viewModel.profile::setWeeklySummary,
                    onSignOut = viewModel::signOut,
                    onSelectTab = selectTab
                )

                Destination.Notifications -> NotificationsScreen(
                    state = viewModel.presentation.notificationsState,
                    unreadIds = viewModel.presentation.unreadNotificationIds,
                    onBack = { viewModel.back() },
                    onMarkAllRead = viewModel::markAllNotificationsRead,
                    onOpenNotification = viewModel::openNotification,
                    onSelectTab = selectTab
                )

                Destination.EditProfile -> EditProfileScreen(
                    coachName = viewModel.coach.name,
                    teamName = viewModel.team.name,
                    squad = viewModel.team.squad,
                    hasUnread = hasUnread,
                    onBack = { viewModel.back() },
                    onNotifications = openNotifications,
                    onSave = { name, team, squad ->
                        viewModel.profile.updateProfile(name)
                        viewModel.profile.updateTeam(team, squad)
                        viewModel.profile.saveProfileEdits()
                    },
                    onSelectTab = selectTab
                )

                is Destination.StatsInput -> {
                    val inputState = viewModel.presentation.statsInputState(screen.fixtureId)

                    StatsInputScreen(
                        state = inputState,
                        hasUnread = hasUnread,
                        onBack = { viewModel.back() },
                        onSelectFixture = { newId ->
                            viewModel.navigate(Destination.StatsInput(newId))
                        },
                        onSave = { playerRows, homeScore, awayScore ->
                            viewModel.saveMatchStats(
                                screen.fixtureId,
                                playerRows,
                                homeScore,
                                awayScore
                            )
                        },
                        onNotifications = openNotifications,
                        onSelectTab = selectTab
                    )
                }

                is Destination.MatchReport -> MatchReportScreen(
                    state = viewModel.presentation.matchReportState(screen.fixtureId),
                    onBack = { viewModel.back() },
                    onNotifications = openNotifications,
                    onSelectTab = selectTab
                )

                Destination.Calendar -> CalendarScreen(
                    state = viewModel.calendar.calendarState,
                    hasUnread = hasUnread,
                    onSelectDate = viewModel.calendar::selectCalendarDay,
                    onPreviousMonth = viewModel.calendar::previousCalendarMonth,
                    onNextMonth = viewModel.calendar::nextCalendarMonth,
                    onManageTrainingOrLineup = {
                        viewModel.navigate(Destination.ManageEvent)
                    },
                    onViewMatchDetails = { matchId ->
                        viewModel.navigate(Destination.MatchDetails(matchId))
                    },
                    onEditLineup = { matchId ->
                        viewModel.navigate(Destination.Lineup(matchId))
                    },
                    onEditMatch = {
                        viewModel.navigate(Destination.EditMatch(it))
                    },
                    onDeleteMatch = viewModel::deleteMatch,
                    onExportLineup = {
                        exportReportToPdf(
                            viewModel.reports.lineupReport(it),
                            context.getString(R.string.review_no_lineup)
                        )
                    },
                    onEditSession = {
                        viewModel.navigate(Destination.EditSession(it))
                    },
                    onDeleteSession = viewModel::deleteSession,
                    onViewTeamAttendance = { event ->
                        viewModel.navigate(Destination.SessionAttendance(event.id))
                    },
                    onNotifications = openNotifications,
                    onSelectTab = selectTab
                )

                Destination.ManageEvent -> ManageEventScreen(
                    saving = viewModel.busy,
                    onBack = { viewModel.back() },
                    onSaveMatch = { opponent, iso, venue ->
                        viewModel.addMatch(opponent, iso, venue)
                    },
                    onSaveSession = { iso, notes ->
                        viewModel.addSession(iso, notes)
                    }
                )

                Destination.Team -> TeamScreen(
                    state = viewModel.presentation.teamState,
                    hasUnread = hasUnread,
                    onNotifications = openNotifications,
                    onAddPlayer = { viewModel.navigate(Destination.AddPlayer) },
                    onEditPlayer = {
                        viewModel.navigate(Destination.EditPlayer(it))
                    },
                    onDeletePlayer = viewModel::deletePlayer,
                    onSelectTab = selectTab
                )

                Destination.AddPlayer -> AddPlayerScreen(
                    saving = viewModel.busy,
                    onBack = { viewModel.back() },
                    onSave = { name, position, dob ->
                        viewModel.addPlayer(name, position, dob)
                    }
                )

                is Destination.EditPlayer -> AddPlayerScreen(
                    existing = viewModel.player(screen.playerId),
                    saving = viewModel.busy,
                    onBack = { viewModel.back() },
                    onSave = { name, position, dob ->
                        viewModel.editPlayer(screen.playerId, name, position, dob)
                    }
                )

                is Destination.EditMatch -> ManageEventScreen(
                    editMatch = viewModel.fixture(screen.matchId),
                    saving = viewModel.busy,
                    onBack = { viewModel.back() },
                    onSaveMatch = { opponent, iso, venue ->
                        viewModel.editMatch(screen.matchId, opponent, iso, venue)
                    }
                )

                is Destination.EditSession -> ManageEventScreen(
                    editSession = viewModel.session(screen.sessionId),
                    saving = viewModel.busy,
                    onBack = { viewModel.back() },
                    onSaveSession = { iso, notes ->
                        viewModel.editSession(screen.sessionId, iso, notes.orEmpty())
                    }
                )

                is Destination.SessionAttendance -> {
                    LaunchedEffect(screen.sessionId) {
                        viewModel.attendance.loadSessionAttendance(screen.sessionId)
                    }

                    AttendanceScreen(
                        onConfirm = { viewModel.attendance.confirmSession(screen.sessionId) },
                        onDiscard = { viewModel.attendance.discardSession(screen.sessionId) },
                        state = viewModel.attendance.sessionAttendanceState(screen.sessionId),
                        onBack = { viewModel.back() },
                        onEditEvent = {
                            viewModel.navigate(Destination.EditSession(screen.sessionId))
                        },
                        onMarkAllPresent = {
                            viewModel.attendance.markAllSessionPresent(screen.sessionId)
                        },
                        onSetStatus = { playerId, status ->
                            viewModel.attendance.setSessionAttendance(screen.sessionId, playerId, status)
                        }
                    )
                }

                is Destination.MatchDetails -> {
                    LaunchedEffect(screen.fixtureId) {
                        viewModel.lineup.loadMatchSquad(screen.fixtureId)
                    }

                    MatchDetailsScreen(
                        state = viewModel.presentation.matchDetailsState(screen.fixtureId),
                        onExportLineup = { exportReportToPdf(viewModel.reports.lineupReport(screen.fixtureId), context.getString(R.string.review_no_lineup)) },
                        onBack = { viewModel.back() }
                    )
                }

                is Destination.Attendance -> {
                    LaunchedEffect(screen.fixtureId) {
                        viewModel.attendance.loadMatchAttendance(screen.fixtureId)
                    }

                    AttendanceScreen(
                        onConfirm = { viewModel.attendance.confirmMatch(screen.fixtureId) },
                        onDiscard = { viewModel.attendance.discardMatch(screen.fixtureId) },
                        state = viewModel.attendance.attendanceState(screen.fixtureId),
                        onBack = { viewModel.back() },
                        onEditEvent = {
                            viewModel.navigate(Destination.EditMatch(screen.fixtureId))
                        },
                        onMarkAllPresent = {
                            viewModel.attendance.markAllPresent(screen.fixtureId)
                        },
                        onSetStatus = { playerId, status ->
                            viewModel.attendance.setMatchAttendance(screen.fixtureId, playerId, status)
                        }
                    )
                }

                is Destination.Lineup -> {
                    var showBuilder by remember(screen.fixtureId) {
                        mutableStateOf(false)
                    }

                    LaunchedEffect(screen.fixtureId) {
                        viewModel.lineup.loadMatchSquad(screen.fixtureId)
                        viewModel.formations.loadFormations()
                    }

                    if (showBuilder) {
                        val starters = viewModel.players.filter {
                            viewModel.lineup.lineupSelection(screen.fixtureId, it.id) == ApiValues.SELECTION_STARTING
                        }
                        val substitutes = viewModel.players.filter {
                            viewModel.lineup.lineupSelection(screen.fixtureId, it.id) == ApiValues.SELECTION_SUBSTITUTE
                        }

                        LineupBuilderScreen(
                            opponent = viewModel.fixture(screen.fixtureId)?.opponent ?: "Opponent",
                            starters = starters,
                            substitutes = substitutes,
                            formations = viewModel.savedFormations,
                            selectedFormation = viewModel.lineup.formationForMatch(screen.fixtureId),
                            entryFor = { playerId ->
                                viewModel.lineup.squadEntryFor(screen.fixtureId, playerId)
                            },
                            onApplyFormation = { formation ->
                                viewModel.lineup.applyFormation(screen.fixtureId, formation)
                            },
                            onPositionChange = { playerId, position, x, y ->
                                viewModel.lineup.updatePlayerPitchPosition(
                                    matchId = screen.fixtureId,
                                    playerId = playerId,
                                    assignedPosition = position,
                                    x = x,
                                    y = y
                                )
                            },
                            onSaveTemplate = { formationId, name, code, positions ->
                                viewModel.formations.saveFormationTemplate(
                                    formationId = formationId,
                                    name = name,
                                    code = code,
                                    positions = positions
                                )
                            },
                            onDeleteTemplate = viewModel.formations::deleteFormationTemplate,
                            onSaveLineup = {
                                viewModel.lineup.saveMatchLineup(screen.fixtureId)
                            },
                            onBack = { showBuilder = false }
                        )
                    } else {
                        LineupSelectionScreen(
                            opponent = viewModel.fixture(screen.fixtureId)?.opponent ?: "Opponent",
                            players = viewModel.players,
                            startingCount = viewModel.lineup.startingCount(screen.fixtureId),
                            substituteCount = viewModel.lineup.substituteCount(screen.fixtureId),
                            selectionOf = { viewModel.lineup.lineupSelection(screen.fixtureId, it) },
                            statusOf = { viewModel.lineup.statusFor(screen.fixtureId, it) },
                            onBack = { viewModel.back() },
                            onSelection = { playerId, choice -> viewModel.lineup.setLineupSelection(screen.fixtureId, playerId, choice) },
                            onAvailability = { playerId, status -> viewModel.lineup.setAvailability(screen.fixtureId, playerId, status) },
                            onMarkAllFit = { viewModel.lineup.markAllFit(screen.fixtureId) },
                            onClear = { viewModel.lineup.clearMatchLineup(screen.fixtureId) },
                            onBuild = { showBuilder = true }
                        )
                    }
                }
            }
        }
    }
}
