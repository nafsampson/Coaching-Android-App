// Uploads new records before changes that depend on them.

package com.formup.app.offline

object SyncOrder {
    fun priority(item: PendingSyncEntity): Int {
        if (!EntityCodec.isKind(item.kind)) return 2
        return runCatching { when (EntityCodec.decode(item.payloadJson).action) {
            EntityAction.CREATE -> 0
            EntityAction.UPDATE -> 1
            EntityAction.DELETE -> 3
        } }.getOrDefault(2)
    }
}
