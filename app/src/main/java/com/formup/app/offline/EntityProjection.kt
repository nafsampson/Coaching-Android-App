// Combines saved server data with local player, event and formation changes.

package com.formup.app.offline

import com.formup.app.data.*

object EntityProjection {
    fun apply(snapshot: SeasonSnapshot, pending: List<PendingSyncEntity>, receipts: List<EntitySyncState>): SeasonSnapshot {
        val ids = StableEntityIds(receipts)
        var result = snapshot.copy(
            players = snapshot.players.map { it.copy(id = ids.ui(EntityType.PLAYER, it.id)) },
            fixtures = snapshot.fixtures.map { f -> f.copy(id = ids.ui(EntityType.MATCH, f.id),
                playerStats = f.playerStats.mapKeys { ids.ui(EntityType.PLAYER, it.key) },
                squad = f.squad.values.map { it.copy(playerId = ids.ui(EntityType.PLAYER, it.playerId)) }.associateBy { it.playerId }) },
            sessions = snapshot.sessions.map { it.copy(id = ids.ui(EntityType.SESSION, it.id)) })
        // Apply local changes after server data so queued edits stay visible.
        val changes = (if (snapshot.fromCache) receipts.mapNotNull { it.confirmedPayload } else emptyList()) +
            pending.filter { EntityCodec.isKind(it.kind) }.map { it.payloadJson }
        changes.forEach { json -> runCatching { EntityCodec.decode(json) }.getOrNull()?.let { result = one(result, it) } }
        return result
    }
    fun one(snapshot: SeasonSnapshot, m: EntityMutation): SeasonSnapshot {
        val v = m.values
        val remove = m.action == EntityAction.DELETE
        return when (m.type) {
            EntityType.PLAYER -> snapshot.copy(players = replace(snapshot.players, m.id, { it.id }, remove) {
                (snapshot.players.firstOrNull { it.id == m.id } ?: Player(m.id, v.name, v.position))
                    .copy(name = v.name, position = v.position, dateOfBirth = v.dateOfBirth)
            })
            EntityType.MATCH -> snapshot.copy(fixtures = replace(snapshot.fixtures, m.id, { it.id }, remove) {
                val labels = ServerDates.labels(v.dateIso)
                (snapshot.fixtures.firstOrNull { it.id == m.id } ?: Fixture(m.id, v.opponent, labels.first, labels.second, v.venue))
                    .copy(opponent = v.opponent, dateLabel = labels.first, kickoff = labels.second, venue = v.venue, matchDateIso = v.dateIso)
            })
            EntityType.SESSION -> snapshot.copy(sessions = replace(snapshot.sessions, m.id, { it.id }, remove) {
                val labels = ServerDates.labels(v.dateIso)
                TrainingSession(m.id, labels.first, labels.second, v.notes, v.dateIso)
            })
            EntityType.FORMATION -> snapshot
        }
    }
    fun formations(server: List<SavedFormation>, pending: List<PendingSyncEntity>, receipts: List<EntitySyncState>, cached: Boolean): List<SavedFormation> {
        val ids = StableEntityIds(receipts)
        var result = server.map { it.copy(id = ids.ui(EntityType.FORMATION, it.id)) }
        val changes = (if (cached) receipts.mapNotNull { it.confirmedPayload } else emptyList()) + pending.filter { EntityCodec.isKind(it.kind) }.map { it.payloadJson }
        changes.forEach { json -> runCatching { EntityCodec.decode(json) }.getOrNull()?.takeIf { it.type == EntityType.FORMATION }?.let { m ->
            result = replace(result, m.id, { it.id }, m.action == EntityAction.DELETE) { SavedFormation(m.id, m.values.name, m.values.code, m.values.positions) }
        } }
        return result
    }
    private fun <T> replace(list: List<T>, id: String, key: (T) -> String, remove: Boolean, value: () -> T): List<T> =
        list.filterNot { key(it) == id } + if (remove) emptyList() else listOf(value())
}
