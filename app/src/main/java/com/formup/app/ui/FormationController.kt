// Loads cached formations and queues template changes.

package com.formup.app.ui

import android.app.Application
import com.formup.app.R
import com.formup.app.data.FormUpApi
import com.formup.app.data.FormationSlot
import com.formup.app.data.SavedFormation
import com.formup.app.security.runCatchingCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

internal class FormationController(
    private val state: SeasonState,
    private val application: Application,
    private val api: FormUpApi,
    private val scope: CoroutineScope,
    private val messages: MessageSink,
    private val db: com.formup.app.offline.FormUpDatabase,
    private val entities: EntityController,
    private val session: com.formup.app.security.SessionLease?
) {
    fun loadFormations() {
        val owner = session ?: return
        scope.launch {
            runCatchingCancellable {
                com.formup.app.offline.WriteLocks.forAccount(owner.uid).withLock {
                owner.check()
                val gson = com.google.gson.Gson()
                var cached = false
                val formations = try {
                    api.getFormations().also { db.apiCacheDao().put(com.formup.app.offline.CachedResponseEntity("formations-snapshot", gson.toJson(it), System.currentTimeMillis())) }
                } catch (error: Exception) {
                    if (!com.formup.app.offline.isConnectivityFailure(error)) throw error
                    cached = true
                    db.apiCacheDao().get("formations-snapshot")?.let { gson.fromJson(it.json, Array<SavedFormation>::class.java).toList() } ?: emptyList()
                }
                val receipts = db.entitySyncDao().getAll()
                val projected = com.formup.app.offline.EntityProjection.formations(formations, db.pendingSyncDao().getAll(), receipts, cached)
                if (!cached) receipts.filter { it.key.startsWith("FORMATION:") && it.confirmedPayload != null }.forEach { db.entitySyncDao().put(it.copy(confirmedPayload = null)) }
                owner.check()
                projected
                }
            }.onSuccess { formations ->
                state.formationList.clear()
                state.formationList.addAll(formations)
                state.fixtureList.forEach { match ->
                    val savedFormation = (builtInFormations + formations).firstOrNull { formation ->
                        (match.formationCode.isNotBlank() &&
                                formation.code.equals(match.formationCode, ignoreCase = true)) ||
                                (match.formationName.isNotBlank() &&
                                        formation.name.equals(match.formationName, ignoreCase = true))
                    }

                    if (savedFormation != null) {
                        state.selectedMatchFormations[match.id] = savedFormation
                    }
                }
            }.onFailure {
                messages.notify(R.string.loc_formation_load_failed)
            }
        }
    }

    fun saveFormationTemplate(
        formationId: String?,
        name: String,
        code: String,
        positions: List<FormationSlot>,
        applyToMatchId: String? = null
    ) {
        if (name.isBlank() || code.isBlank()) {
            messages.notify(R.string.loc_enter_formation)
            return
        }

        if (!FormationCode.valid(code)) {
            messages.notify(R.string.final_formation_invalid)
            return
        }

        if (positions.size != 11) {
            messages.notify(R.string.loc_formation_11)
            return
        }

        val values = com.formup.app.offline.EntityValues(name = name.trim(), code = code.trim(), positions = positions)
        val saved: (String) -> Unit = { id ->
            if (applyToMatchId != null) state.selectedMatchFormations[applyToMatchId] = SavedFormation(id, values.name, values.code, positions)
        }
        if (formationId == null) entities.create(com.formup.app.offline.EntityType.FORMATION, values, false, saved)
        else entities.update(com.formup.app.offline.EntityType.FORMATION, formationId, values, false, saved)
    }

    fun deleteFormationTemplate(formationId: String) = entities.delete(com.formup.app.offline.EntityType.FORMATION, formationId)
}
