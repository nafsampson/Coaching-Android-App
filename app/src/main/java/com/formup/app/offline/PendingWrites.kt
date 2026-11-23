// Saves queued changes with unique revisions and coordinates account uploads.

package com.formup.app.offline

import com.formup.app.security.SessionLease
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object WriteLocks {
    private val locks = ConcurrentHashMap<String, Mutex>()
    fun forAccount(uid: String): Mutex = locks.getOrPut(uid) { Mutex() }
}

class PendingWrites(private val dao: PendingSyncDao, private val checkSession: () -> Unit) {
    constructor(dao: PendingSyncDao, session: SessionLease) : this(dao, session::check)
    private val enqueueLock = Mutex()
    suspend fun get(kind: String, targetId: String): PendingSyncEntity? {
        checkSession()
        return dao.get(PayloadCodec.keyFor(kind, targetId)).also { checkSession() }
    }
    suspend fun put(kind: String, targetId: String, payload: String) = enqueueLock.withLock {
        checkSession()
        dao.upsert(PendingSyncEntity(
            key = PayloadCodec.keyFor(kind, targetId), kind = kind, targetId = targetId,
            payloadJson = payload, createdAt = System.currentTimeMillis(), revision = UUID.randomUUID().toString()
        ))
        checkSession()
    }
}
