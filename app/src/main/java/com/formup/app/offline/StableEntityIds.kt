// Maps local record IDs to server IDs without changing screen selections.

package com.formup.app.offline

class StableEntityIds(private val states: List<EntitySyncState>, private val pending: List<PendingSyncEntity> = emptyList()) {
    private val receipts = states.associateBy { it.key }
    private val deleted = pending.filter { EntityCodec.isKind(it.kind) }.mapNotNull {
        runCatching { EntityCodec.decode(it.payloadJson) }.getOrNull()?.takeIf { it.action == EntityAction.DELETE }
    }
    fun ui(type: EntityType, id: String): String = states.firstOrNull {
        it.key.startsWith("${type.name}:") && it.remoteId == id
    }?.key?.substringAfter(':') ?: id
    fun resolve(type: EntityType, id: String): String? {
        val local = ui(type, id)
        val receipt = receipts[EntityCodec.key(type, local)]
        if (receipt?.deleted == true || deleted.any { it.type == type && ui(type, it.id) == local }) return null
        if (!EntityCodec.local(local)) return local
        // Wait for new records to upload before sending their dependent changes.
        return receipt?.remoteId ?: throw DependencyPendingException()
    }
}
