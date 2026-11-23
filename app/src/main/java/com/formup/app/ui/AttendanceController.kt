// Loads attendance and queues the confirmed set of changes.

package com.formup.app.ui

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import com.formup.app.R
import com.formup.app.data.ApiValues
import com.formup.app.data.FormUpApi
import com.formup.app.offline.*
import com.formup.app.security.SessionLease
import com.formup.app.security.runCatchingCancellable
import com.formup.app.ui.calendar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

internal class AttendanceController(
    private val state: SeasonState,
    private val application: Application,
    private val api: FormUpApi,
    private val scope: CoroutineScope,
    private val messages: MessageSink,
    private val db: FormUpDatabase,
    private val session: SessionLease?
) {
    private val editRevisions = mutableMapOf<String, Long>()
    private val ready = mutableStateMapOf<String, Boolean>()
    private val drafts = mutableStateMapOf<String, AttendanceDraft>()
    private val saving = mutableStateMapOf<String, Boolean>()
    private val pendingWrites = session?.let { PendingWrites(db.pendingSyncDao(), it) }

    private fun buildAttendance(
        key: String,
        dateTime: String,
        title: String,
        statuses: Map<String, String>,
        ready: Boolean
    ): AttendanceUiState {
        val roster = state.playerList.map { player ->
            val raw = statuses[player.id] ?: ApiValues.NOT_RECORDED
            val status = when (raw) {
                "Present" -> RosterStatus.ATTENDING
                "Absent", "AbsentNoExcuse" -> RosterStatus.ABSENT
                else -> RosterStatus.NO_REPLY
            }
            RosterEntry(
                id = player.id,
                name = player.name,
                subtitle = player.position,
                statusBadge = if (raw == "AbsentNoExcuse") "NO EXCUSE" else null,
                status = status,
                action = if (status != RosterStatus.ATTENDING) RosterRowAction.MARK_PRESENT else RosterRowAction.NONE
            )
        }
        return AttendanceUiState(
            dateTimeLabel = dateTime,
            eventTitle = title,
            squadSize = roster.size,
            attending = roster.count { it.status == RosterStatus.ATTENDING },
            absentCount = roster.count { it.status == RosterStatus.ABSENT },
            noReplyCount = roster.count { it.status == RosterStatus.NO_REPLY },
            roster = roster,
            canEdit = ready && saving[key] != true,
            hasChanges = drafts[key]?.hasChanges == true,
            isSaving = saving[key] == true,
            notice = if (ready) null else application.getString(R.string.att_load_before_edit)
        )
    }

    fun attendanceState(fixtureId: String): AttendanceUiState {
        val match = state.fixture(fixtureId)
        return buildAttendance(
            key = PayloadCodec.keyFor(SyncKind.MATCH_ATTENDANCE, fixtureId),
            dateTime = listOfNotNull(match?.dateLabel, match?.kickoff, match?.venue)
                .filter { it.isNotBlank() }.joinToString(" · ").uppercase(),
            title = match?.let { application.getString(R.string.loc_match_vs, it.opponent) } ?: application.getString(R.string.loc_match_attendance),
            statuses = displayedValues(SyncKind.MATCH_ATTENDANCE, fixtureId),
            ready = ready[PayloadCodec.keyFor(SyncKind.MATCH_ATTENDANCE, fixtureId)] == true
        )
    }

    fun sessionAttendanceState(sessionId: String): AttendanceUiState {
        val session = state.session(sessionId)
        return buildAttendance(
            key = PayloadCodec.keyFor(SyncKind.SESSION_ATTENDANCE, sessionId),
            dateTime = listOfNotNull(session?.dateLabel, session?.time)
                .filter { it.isNotBlank() }.joinToString(" · ").uppercase(),
            title = session?.notes?.takeIf { it.isNotBlank() } ?: application.getString(R.string.loc_training_session),
            statuses = displayedValues(SyncKind.SESSION_ATTENDANCE, sessionId),
            ready = ready[PayloadCodec.keyFor(SyncKind.SESSION_ATTENDANCE, sessionId)] == true
        )
    }

    fun loadMatchAttendance(id: String) = load(SyncKind.MATCH_ATTENDANCE, id)
    fun loadSessionAttendance(id: String) = load(SyncKind.SESSION_ATTENDANCE, id)

    private fun load(kind: String, id: String) {
        val owner = session ?: return
        if (saving[PayloadCodec.keyFor(kind, id)] == true) return
        scope.launch {
            val key = PayloadCodec.keyFor(kind, id)
            val revision = editRevisions[key] ?: 0
            runCatchingCancellable {
                owner.check()
                val local = db.pendingSyncDao().get(key)?.payloadJson ?: db.apiCacheDao().get("attendance:$key")?.json
                if (local != null) {
                    setValues(kind, id, PayloadCodec.decode(local))
                    ready[key] = true
                }
                WriteLocks.forAccount(owner.uid).withLock {
                    val ids = StableEntityIds(db.entitySyncDao().getAll(), db.pendingSyncDao().getAll())
                    val type = if (kind == SyncKind.MATCH_ATTENDANCE) EntityType.MATCH else EntityType.SESSION
                    val remoteId = try { ids.resolve(type, id) } catch (_: DependencyPendingException) { null }

                    // New offline events have no earlier attendance records to preserve.
                    val records = if (remoteId == null && EntityCodec.local(id)) emptyList()
                        else if (kind == SyncKind.MATCH_ATTENDANCE) api.getAttendance(requireNotNull(remoteId)) else api.getSessionAttendance(requireNotNull(remoteId))
                    owner.check()
                    val confirmed = records.filter { it.status != ApiValues.NOT_RECORDED }.associate { ids.ui(EntityType.PLAYER, it.playerId) to it.status }
                    db.apiCacheDao().put(CachedResponseEntity("attendance:$key", PayloadCodec.encode(confirmed), System.currentTimeMillis()))
                    val pending = db.pendingSyncDao().get(key)?.payloadJson?.let(PayloadCodec::decode)
                    owner.check()
                    if ((editRevisions[key] ?: 0) == revision) setValues(kind, id, pending ?: confirmed)
                    ready[key] = true
                }
            }.onFailure {
                if (ready[key] != true || !isConnectivityFailure(it)) messages.notify(R.string.msg_attendance_load_failed)
            }
        }
    }

    private fun values(kind: String, id: String): Map<String, String> =
        if (kind == SyncKind.MATCH_ATTENDANCE) state.attendanceCache[id].orEmpty() else state.sessionAttendancePrevious[id].orEmpty()
    private fun setValues(kind: String, id: String, values: Map<String, String>) {
        if (kind == SyncKind.MATCH_ATTENDANCE) state.attendanceCache[id] = values else state.sessionAttendancePrevious[id] = values
    }
    private fun displayedValues(kind: String, id: String) =
        drafts[PayloadCodec.keyFor(kind, id)]?.resolve(values(kind, id)) ?: values(kind, id)

    private fun stage(kind: String, id: String, patch: Map<String, String>) {
        session?.check() ?: return
        val key = PayloadCodec.keyFor(kind, id)
        if (saving[key] == true) return
        if (ready[key] != true) { messages.notify(R.string.att_load_before_edit); return }
        drafts[key] = (drafts[key] ?: AttendanceDraft()).stage(values(kind, id), patch)
    }

    private fun confirm(kind: String, id: String) {
        val owner = session ?: return
        owner.check()
        val key = PayloadCodec.keyFor(kind, id)
        val draft = drafts[key]?.takeIf { it.hasChanges } ?: return
        if (ready[key] != true || saving[key] == true) return
        saving[key] = true
        editRevisions[key] = (editRevisions[key] ?: 0) + 1
        scope.launch {
            try {
                runCatchingCancellable {
                    val base = db.pendingSyncDao().get(key)?.payloadJson?.let(PayloadCodec::decode) ?: values(kind, id)
                    val updated = draft.resolve(base)
                    // Queue the complete attendance set before showing it as saved.
                    (pendingWrites ?: error("Sign in to save")).put(kind, id, PayloadCodec.encode(updated))
                    owner.check()
                    setValues(kind, id, updated)
                    drafts.remove(key)
                    SyncScheduler.schedule(application, owner.uid)
                    messages.notify(R.string.msg_saved_pending_sync)
                }.onFailure { messages.notify(R.string.msg_attendance_save_failed) }
            } finally { saving[key] = false }
        }
    }

    private fun discard(kind: String, id: String) { drafts.remove(PayloadCodec.keyFor(kind, id)) }
    fun confirmMatch(id: String) = confirm(SyncKind.MATCH_ATTENDANCE, id)
    fun confirmSession(id: String) = confirm(SyncKind.SESSION_ATTENDANCE, id)
    fun discardMatch(id: String) = discard(SyncKind.MATCH_ATTENDANCE, id)
    fun discardSession(id: String) = discard(SyncKind.SESSION_ATTENDANCE, id)
    fun setMatchAttendance(id: String, playerId: String, status: String) = stage(SyncKind.MATCH_ATTENDANCE, id, mapOf(playerId to status))
    fun setSessionAttendance(id: String, playerId: String, status: String) = stage(SyncKind.SESSION_ATTENDANCE, id, mapOf(playerId to status))
    fun markAllPresent(id: String) = stage(SyncKind.MATCH_ATTENDANCE, id, state.playerList.associate { it.id to ApiValues.ATTENDANCE_PRESENT })
    fun markAllSessionPresent(id: String) = stage(SyncKind.SESSION_ATTENDANCE, id, state.playerList.associate { it.id to ApiValues.ATTENDANCE_PRESENT })
}
