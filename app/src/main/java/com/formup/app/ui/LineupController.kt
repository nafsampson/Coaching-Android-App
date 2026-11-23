// Manages match selections, availability, formations and pitch positions.

package com.formup.app.ui

import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.data.AvailabilityStatus
import com.formup.app.data.FormUpApi
import com.formup.app.data.SavedFormation
import com.formup.app.data.SquadUpdate
import com.formup.app.offline.PendingWrites
import com.formup.app.offline.WriteKind
import com.formup.app.offline.WriteLocks
import com.formup.app.offline.WritePayloads
import com.formup.app.security.SessionLease
import com.formup.app.security.runCatchingCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

internal class LineupController(
    private val state: SeasonState,
    private val api: FormUpApi,
    private val scope: CoroutineScope,
    private val messages: MessageSink,
    private val back: () -> Boolean,
    private val writes: PendingWrites?,
    private val session: SessionLease?,
    private val scheduleSync: () -> Unit
) {
    private val editRevisions = mutableMapOf<String, Long>()
    private val drafts = mutableSetOf<String>()
    fun hasDraft(id: String) = id in drafts

    private companion object {
        const val MAX_LINEUP = 11
    }

    fun availabilityToApi(status: AvailabilityStatus): String {
        return status.apiValue
    }

    fun loadMatchSquad(matchId: String) {
        val owner = session ?: return
        val revision = editRevisions[matchId] ?: 0
        scope.launch {
            runCatchingCancellable {
                WriteLocks.forAccount(owner.uid).withLock {
                    val pending = writes?.get(WriteKind.MATCH_LINEUP, matchId)
                    pending?.let { WritePayloads.squad(JSONObject(it.payloadJson)) } ?: state.fixture(matchId)?.squad?.values?.toList().orEmpty()
                }
            }.onSuccess { squad ->
                if ((editRevisions[matchId] ?: 0) != revision) return@onSuccess

                val apiEntries = squad.associateBy { it.playerId }

                val completeSquad = state.playerList.associate { player ->
                    val existing = apiEntries[player.id]

                    player.id to (
                            existing ?: SquadUpdate(
                                playerId = player.id,
                                status = availabilityToApi(player.status),
                                selection = "NotSelected"
                            )
                            )
                }.toMutableMap()

                state.matchSquadEntries[matchId] = completeSquad

                state.matchSquadSelections[matchId] =
                    completeSquad.mapValues { it.value.selection }.toMutableMap()

                state.matchSquadStatuses[matchId] =
                    completeSquad.mapValues { it.value.status }.toMutableMap()

            }.onFailure {
                messages.notify(R.string.msg_lineup_load_failed)
            }
        }
    }

    fun lineupSelection(
        matchId: String,
        playerId: String
    ): String {
        return state.matchSquadSelections[matchId]?.get(playerId)
            ?: "NotSelected"
    }

    fun squadEntryFor(
        matchId: String,
        playerId: String
    ): SquadUpdate? {
        return state.matchSquadEntries[matchId]?.get(playerId)
    }

    fun formationForMatch(matchId: String): SavedFormation? {
        state.selectedMatchFormations[matchId]?.let { return it }

        val match = state.fixture(matchId) ?: return null

        return state.savedFormations.firstOrNull { formation ->
            (match.formationCode.isNotBlank() &&
                    formation.code.equals(match.formationCode, ignoreCase = true)) ||
                    (match.formationName.isNotBlank() &&
                            formation.name.equals(match.formationName, ignoreCase = true))
        }
    }
    fun statusFor(
        matchId: String,
        playerId: String
    ): AvailabilityStatus {
        val status = state.matchSquadStatuses[matchId]?.get(playerId)
        return status?.let { AvailabilityStatus.fromApi(it) } ?: state.player(playerId)?.status ?: AvailabilityStatus.Fit
    }

    fun startingCount(matchId: String): Int {
        return state.matchSquadSelections[matchId]
            ?.count { it.value == "Starting" }
            ?: 0
    }

    fun substituteCount(matchId: String): Int {
        return state.matchSquadSelections[matchId]
            ?.count { it.value == "Substitute" }
            ?: 0
    }

    fun toggleLineupSelection(
        matchId: String,
        playerId: String
    ) {
        val current = lineupSelection(matchId, playerId)
        val next = when (current) {
            ApiValues.SELECTION_NOT_SELECTED -> ApiValues.SELECTION_STARTING
            ApiValues.SELECTION_STARTING -> ApiValues.SELECTION_SUBSTITUTE
            else -> ApiValues.SELECTION_NOT_SELECTED
        }
        setLineupSelection(matchId, playerId, next)
    }

    fun setLineupSelection(matchId: String, playerId: String, next: String) {
        val player = state.player(playerId) ?: return
        when (selectionProblem(lineupSelection(matchId, playerId), next, statusFor(matchId, playerId), startingCount(matchId))) {
            SelectionProblem.OUT -> { messages.notify(R.string.loc_player_out, player.name); return }
            SelectionProblem.FULL -> { messages.notify(R.string.msg_starting_xi_full); return }
            null -> Unit
        }
        editRevisions[matchId] = (editRevisions[matchId] ?: 0) + 1
        drafts.add(matchId)
        val selections =
            state.matchSquadSelections[matchId]?.toMutableMap() ?: mutableMapOf()

        selections[playerId] = next
        state.matchSquadSelections[matchId] = selections

        val entries =
            state.matchSquadEntries[matchId]?.toMutableMap() ?: mutableMapOf()

        val existing = entries[playerId] ?: SquadUpdate(
            playerId = playerId,
            status = availabilityToApi(player.status),
            selection = "NotSelected"
        )

        entries[playerId] = existing.copy(selection = next)
        state.matchSquadEntries[matchId] = entries
    }

    fun clearMatchLineup(matchId: String) {
        editRevisions[matchId] = (editRevisions[matchId] ?: 0) + 1
        drafts.add(matchId)
        val entries =
            state.matchSquadEntries[matchId]?.toMutableMap() ?: mutableMapOf()

        entries.keys.toList().forEach { playerId ->
            entries[playerId]?.let { entry ->
                entries[playerId] = entry.copy(
                    selection = "NotSelected",
                    assignedPosition = "",
                    pitchX = null,
                    pitchY = null
                )
            }
        }

        state.matchSquadEntries[matchId] = entries
        state.matchSquadSelections[matchId] =
            entries.mapValues { it.value.selection }.toMutableMap()

        messages.notify(R.string.msg_lineup_cleared)
    }

    fun applyFormation(
        matchId: String,
        formation: SavedFormation
    ) {
        editRevisions[matchId] = (editRevisions[matchId] ?: 0) + 1
        drafts.add(matchId)
        val selections = state.matchSquadSelections[matchId] ?: return

        val starters = state.playerList
            .filter { selections[it.id] == "Starting" }
            .map { it.id }

        if (starters.size != 11) {
            messages.notify(R.string.msg_select_exactly_11)
            return
        }

        if (formation.positions.size != 11) {
            messages.notify(R.string.loc_formation_invalid)
            return
        }

        val entries =
            state.matchSquadEntries[matchId]?.toMutableMap() ?: mutableMapOf()

        starters.forEachIndexed { index, playerId ->
            val slot = formation.positions[index]
            val existing = entries[playerId] ?: return@forEachIndexed

            entries[playerId] = existing.copy(
                assignedPosition = slot.label,
                pitchX = slot.x,
                pitchY = slot.y
            )
        }

        state.matchSquadEntries[matchId] = entries

        state.selectedMatchFormations[matchId] = formation

        messages.notify(R.string.loc_formation_applied, formation.name)
    }

    fun updatePlayerPitchPosition(
        matchId: String,
        playerId: String,
        assignedPosition: String,
        x: Double,
        y: Double
    ) {
        editRevisions[matchId] = (editRevisions[matchId] ?: 0) + 1
        drafts.add(matchId)
        if (x !in 0.0..100.0 || y !in 0.0..100.0) return

        val entries =
            state.matchSquadEntries[matchId]?.toMutableMap() ?: return

        val existing = entries[playerId] ?: return

        if (existing.selection != "Starting") return

        entries[playerId] = existing.copy(
            assignedPosition = assignedPosition,
            pitchX = x,
            pitchY = y
        )

        state.matchSquadEntries[matchId] = entries
    }

    fun saveMatchLineup(matchId: String) {
        val selections = state.matchSquadSelections[matchId] ?: mutableMapOf()

        if (selections.count { it.value == "Starting" } != 11) {
            messages.notify(R.string.loc_need_11_starters)
            return
        }

        val selectedOutPlayer = state.playerList.firstOrNull { player ->
            statusFor(matchId, player.id) == AvailabilityStatus.Out &&
                    selections[player.id] in listOf("Starting", "Substitute")
        }

        if (selectedOutPlayer != null) {
            messages.notify(R.string.loc_out_replace, selectedOutPlayer.name)
            return
        }

        val selectedFormation = state.selectedMatchFormations[matchId]
        val existingEntries = state.matchSquadEntries[matchId] ?: mutableMapOf()

        // Capture the full squad before starting the save operation.
        val squad = state.playerList.map { player ->
            val existing = existingEntries[player.id]

            SquadUpdate(
                playerId = player.id,
                status = state.matchSquadStatuses[matchId]
                    ?.get(player.id)
                    ?: availabilityToApi(player.status),
                selection = selections[player.id] ?: "NotSelected",
                assignedPosition = existing?.assignedPosition ?: "",
                pitchX = existing?.pitchX,
                pitchY = existing?.pitchY
            )
        }

        scope.launch {
            runCatchingCancellable {
                val queue = writes ?: error("Sign in to save")
                val match = state.fixture(matchId)
                queue.put(WriteKind.MATCH_LINEUP, matchId, WritePayloads.lineup(squad,
                    selectedFormation?.name ?: match?.formationName, selectedFormation?.code ?: match?.formationCode))
                scheduleSync()
            }.onSuccess {
                drafts.remove(matchId)

                val savedEntries = squad.associateBy { it.playerId }.toMutableMap()

                state.matchSquadEntries[matchId] = savedEntries

                state.matchSquadSelections[matchId] =
                    savedEntries.mapValues { it.value.selection }.toMutableMap()

                state.matchSquadStatuses[matchId] =
                    savedEntries.mapValues { it.value.status }.toMutableMap()

                if (selectedFormation != null) {
                    val index = state.fixtureList.indexOfFirst { it.id == matchId }

                    if (index >= 0) {
                        state.fixtureList[index] = state.fixtureList[index].copy(
                            formationName = selectedFormation.name,
                            formationCode = selectedFormation.code
                        )
                    }
                }

                messages.notify(R.string.msg_saved_pending_sync)
                back()
            }.onFailure {
                messages.notify(R.string.msg_lineup_save_failed)
            }
        }
    }

    private fun saveSquad(matchId: String) {
        val squad = state.playerList.map { player ->
            val entry = state.matchSquadEntries[matchId]?.get(player.id) ?: SquadUpdate(player.id, player.status.apiValue, ApiValues.SELECTION_NOT_SELECTED)
            entry.copy(status = state.matchSquadStatuses[matchId]?.get(player.id) ?: entry.status,
                selection = state.matchSquadSelections[matchId]?.get(player.id) ?: entry.selection)
        }
        val formation = formationForMatch(matchId)
        val payload = WritePayloads.lineup(squad, formation?.name, formation?.code)
        val revision = editRevisions[matchId] ?: 0
        scope.launch {
            runCatchingCancellable {
                (writes ?: error("Sign in to save")).put(WriteKind.MATCH_LINEUP, matchId, payload)
                if ((editRevisions[matchId] ?: 0) == revision) drafts.remove(matchId)
                scheduleSync()
            }.onFailure { messages.notify(R.string.msg_availability_save_failed) }
        }
    }

    fun refreshSquadStatuses() {
        // Roster availability follows the next fixture rather than every match.
        val statuses = state.nextFixture?.let { state.matchSquadStatuses[it.id] }.orEmpty()
        for (i in state.playerList.indices) {
            state.playerList[i] = state.playerList[i].copy(
                status = AvailabilityStatus.fromApi(statuses[state.playerList[i].id])
            )
        }
    }

    fun setAvailability(matchId: String, playerId: String, status: AvailabilityStatus) {
        if (state.player(playerId) == null) return
        editRevisions[matchId] = (editRevisions[matchId] ?: 0) + 1
        drafts.add(matchId)
        val updated = state.matchSquadStatuses[matchId].orEmpty().toMutableMap()
        updated[playerId] = status.apiValue
        state.matchSquadStatuses[matchId] = updated
        if (status == AvailabilityStatus.Out) {
            setLineupSelection(matchId, playerId, ApiValues.SELECTION_NOT_SELECTED)
        }
        refreshSquadStatuses()
        saveSquad(matchId)
    }

    fun markAllFit(matchId: String) {
        editRevisions[matchId] = (editRevisions[matchId] ?: 0) + 1
        drafts.add(matchId)
        state.matchSquadStatuses[matchId] = state.playerList.associate { it.id to AvailabilityStatus.Fit.apiValue }.toMutableMap()
        refreshSquadStatuses()
        saveSquad(matchId)
        messages.notify(R.string.msg_everyone_marked_fit)
    }
}
