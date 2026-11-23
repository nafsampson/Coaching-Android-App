// Holds shared season data and calculates player and fixture totals.

package com.formup.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.formup.app.data.ActivityEntry
import com.formup.app.data.AppNotification
import com.formup.app.data.CoachProfile
import com.formup.app.data.Fixture
import com.formup.app.data.Player
import com.formup.app.data.SavedFormation
import com.formup.app.data.SquadUpdate
import com.formup.app.data.TeamProfile
import com.formup.app.data.TrainingSession

internal class SeasonState {
    val playerList = mutableStateListOf<Player>()
    val fixtureList = mutableStateListOf<Fixture>()
    val notificationList = mutableStateListOf<AppNotification>()

    val sessionList = mutableStateListOf<TrainingSession>()
    val matchSquadStatuses = mutableStateMapOf<String, MutableMap<String, String>>()
    val activityList = mutableStateListOf<ActivityEntry>()
    val attendanceCache = mutableStateMapOf<String, Map<String, String>>()
    val sessionAttendancePrevious = mutableStateMapOf<String, Map<String, String>>()
    val matchSquadSelections = mutableStateMapOf<String, MutableMap<String, String>>()

    val matchSquadEntries =
        mutableStateMapOf<String, MutableMap<String, SquadUpdate>>()

    val formationList = mutableStateListOf<SavedFormation>()
    val selectedMatchFormations =
        mutableStateMapOf<String, SavedFormation>()

    var coach by mutableStateOf(CoachProfile("", "en", false, false))
    var team by mutableStateOf(TeamProfile("", ""))

    fun clear() {
        playerList.clear(); fixtureList.clear(); notificationList.clear(); sessionList.clear()
        activityList.clear(); attendanceCache.clear(); sessionAttendancePrevious.clear()
        matchSquadSelections.clear(); matchSquadStatuses.clear(); matchSquadEntries.clear()
        formationList.clear(); selectedMatchFormations.clear()
        coach = CoachProfile("", "en", false, false)
        team = TeamProfile("", "")
    }

    val savedFormations: List<SavedFormation> get() = builtInFormations + formationList
    val nextFixture: Fixture? get() = UpcomingEvents.nextMatch(fixtureList)
    val lastPlayedFixture: Fixture? get() = fixtureList.filter { it.played && com.formup.app.data.ServerDates.instant(it.matchDateIso) != null }
        .maxByOrNull { com.formup.app.data.ServerDates.instant(it.matchDateIso)!! }
    val playedFixtures: List<Fixture> get() = fixtureList.filter { it.played }
    fun fixture(id: String): Fixture? = fixtureList.firstOrNull { it.id == id }
    fun player(id: String): Player? = playerList.firstOrNull { it.id == id }
    fun session(id: String): TrainingSession? = sessionList.firstOrNull { it.id == id }
    fun goalsOf(player: Player): Int = fixtureList.sumOf { it.playerStats[player.id]?.goals ?: 0 }
    fun assistsOf(player: Player): Int = fixtureList.sumOf { it.playerStats[player.id]?.assists ?: 0 }
    fun matchesOf(player: Player): Int = fixtureList.count { it.played && it.playerStats[player.id]?.participated == true }
    fun minutesOf(player: Player): Int = fixtureList.sumOf { it.playerStats[player.id]?.minutes ?: 0 }
}
