// Saves player, event and formation changes locally before closing the form.

package com.formup.app.ui

import com.formup.app.R
import com.formup.app.offline.*
import com.formup.app.security.SessionLease
import com.formup.app.security.runCatchingCancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

internal class EntityController(private val scope: CoroutineScope, private val session: SessionLease?,
    private val writes: PendingWrites?, private val messages: MessageSink,
    private val project: suspend (EntityMutation) -> Unit, private val schedule: () -> Unit, private val back: () -> Boolean) {
    fun create(type: EntityType, values: EntityValues, close: Boolean = true, then: (String) -> Unit = {}) =
        save(type, EntityAction.CREATE, "local-${UUID.randomUUID()}", values, close, then)
    fun update(type: EntityType, id: String, values: EntityValues, close: Boolean = true, then: (String) -> Unit = {}) =
        save(type, EntityAction.UPDATE, id, values, close, then)
    fun delete(type: EntityType, id: String) = save(type, EntityAction.DELETE, id, EntityValues(), false) {}

    private fun save(type: EntityType, action: EntityAction, id: String, values: EntityValues, close: Boolean, then: (String) -> Unit) {
        val owner = session ?: return
        val queue = writes ?: return
        scope.launch {
            runCatchingCancellable {
                owner.check()
                val kind = EntityCodec.kind(type)
                val previous = queue.get(kind, id)?.let { EntityCodec.decode(it.payloadJson) }
                val mutation = EntityCodec.next(previous, type, action, id, values)
                // Save to Room before updating the screen or closing the form.
                queue.put(kind, id, EntityCodec.encode(mutation))
                project(mutation)
                then(id)
                schedule()
                messages.notify(R.string.msg_saved_pending_sync)
                if (close) back()
            }.onFailure { messages.notify(R.string.offline_save_failed) }
        }
    }
}
