// Runs foreground and background uploads through the same account queue.

package com.formup.app.offline

import android.content.Context
import com.formup.app.data.FormUpApi
import com.formup.app.notifications.NotificationHelper
import com.formup.app.security.SessionLease
import kotlinx.coroutines.sync.withLock

internal object SyncEngine {
    suspend fun run(context: Context, owner: SessionLease): SyncOutcome = WriteLocks.forAccount(owner.uid).withLock {
        owner.check()
        val db = FormUpDatabase.get(context, owner.uid)
        val dao = db.pendingSyncDao()
        val executor = WriteExecutor(FormUpApi(session = owner), db, owner::check)
        val outcome = OutboxProcessor(dao, owner::check, executor::upload) { item ->
            if (item.kind in listOf(SyncKind.MATCH_ATTENDANCE, SyncKind.SESSION_ATTENDANCE)) {
                val ids = StableEntityIds(db.entitySyncDao().getAll())
                val type = if (item.kind == SyncKind.MATCH_ATTENDANCE) EntityType.MATCH else EntityType.SESSION
                val target = ids.ui(type, item.targetId)
                val rows = PayloadCodec.decode(item.payloadJson).mapKeys { ids.ui(EntityType.PLAYER, it.key) }
                db.apiCacheDao().put(CachedResponseEntity("attendance:${PayloadCodec.keyFor(item.kind, target)}", PayloadCodec.encode(rows), System.currentTimeMillis()))
            }
        }.process()
        owner.check()
        if (outcome.confirmed > 0) NotificationHelper.showSyncComplete(context, outcome.confirmed)
        outcome
    }
}
