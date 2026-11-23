// Sends queued attendance, lineup, report and profile changes in order.

package com.formup.app.offline

import com.formup.app.data.AttendanceRecord
import com.formup.app.data.FormUpApi
import org.json.JSONObject

internal class WriteExecutor(private val api: FormUpApi, private val db: FormUpDatabase, private val checkSession: () -> Unit) {
    private val entities = EntityUploader(db.entitySyncDao(), checkSession, ApiEntityRemote(api))
    suspend fun upload(item: PendingSyncEntity) {
        checkSession()
        if (EntityCodec.isKind(item.kind)) { entities.upload(EntityCodec.decode(item.payloadJson)); return }
        val ids = StableEntityIds(db.entitySyncDao().getAll(), db.pendingSyncDao().getAll())
        fun player(id: String) = ids.resolve(EntityType.PLAYER, id)
        when (item.kind) {
            SyncKind.MATCH_ATTENDANCE, SyncKind.SESSION_ATTENDANCE -> {
                val records = PayloadCodec.decode(item.payloadJson).mapNotNull { (id, status) -> player(id)?.let { AttendanceRecord(it, status) } }
                val target = ids.resolve(if (item.kind == SyncKind.MATCH_ATTENDANCE) EntityType.MATCH else EntityType.SESSION, item.targetId) ?: return
                if (item.kind == SyncKind.MATCH_ATTENDANCE) api.saveAttendance(target, records)
                else api.saveSessionAttendance(target, records)
            }
            WriteKind.MATCH_REPORT -> {
                val body = JSONObject(item.payloadJson)
                val target = ids.resolve(EntityType.MATCH, item.targetId) ?: return
                api.saveStats(target, WritePayloads.stats(body).mapNotNull { stat -> player(stat.playerId)?.let { stat.copy(playerId = it) } })

                // Save detailed statistics before publishing the final score.
                api.updateMatchScores(target, body.getInt("teamScore"), body.getInt("opponentScore"))
            }
            WriteKind.MATCH_LINEUP -> {
                val body = JSONObject(item.payloadJson)
                val target = ids.resolve(EntityType.MATCH, item.targetId) ?: return
                api.saveSquad(target, WritePayloads.squad(body).mapNotNull { entry -> player(entry.playerId)?.let { entry.copy(playerId = it) } })
                val name = body.optString("formationName")
                val code = body.optString("formationCode")
                if (name.isNotBlank() && code.isNotBlank()) api.saveMatchFormation(target, name, code)
            }
            WriteKind.REMINDERS -> api.updateReminderPreference(JSONObject(item.payloadJson).getBoolean("enabled"))
            WriteKind.PROFILE -> {
                val body = JSONObject(item.payloadJson)
                api.updateCoach(body.getString("displayName"), body.optString("language").takeIf { it.isNotBlank() })
                api.updateTeam(body.getString("teamName"), body.getInt("ageGroup"))
            }
            else -> throw IllegalArgumentException("Unsupported queued operation")
        }
    }
}
