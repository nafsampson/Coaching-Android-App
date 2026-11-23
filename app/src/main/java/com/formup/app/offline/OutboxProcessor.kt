// Uploads pending changes and keeps failed or newer changes in the queue.

package com.formup.app.offline

import com.formup.app.data.ApiException
import kotlinx.coroutines.CancellationException

data class SyncOutcome(val confirmed: Int, val retry: Boolean)

class OutboxProcessor(
    private val dao: PendingSyncDao,
    private val checkSession: () -> Unit,
    private val upload: suspend (PendingSyncEntity) -> Unit,
    private val confirmed: suspend (PendingSyncEntity) -> Unit = {}
) {
    suspend fun process(): SyncOutcome {
        var synced = 0
        var shouldRetry = false
        for (candidate in dao.getAll().sortedBy { SyncOrder.priority(it) }) {
            checkSession()
            val item = dao.get(candidate.key) ?: continue
            if (item.lastError != null) continue
            try {
                upload(item)
                checkSession()
                confirmed(item)
                // A newer edit must remain queued even if this upload succeeds.
                val deleted = dao.deleteIfUnchanged(item.key, item.revision)
                synced += deleted
                if (deleted == 0) shouldRetry = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (error is DependencyPendingException) { shouldRetry = true; continue }
                // Keep ambiguous creates for review instead of risking a duplicate.
                if (error is UncertainCreateException) { dao.markFailed(item.key, item.revision, "uncertain"); continue }
                val code = (error as? ApiException)?.code
                if (isConnectivityFailure(error) || code == 408 || code == 429 || (code != null && code >= 500)) {
                    shouldRetry = true
                } else {
                    val message = if (code == 401 || code == 403) "auth"
                        else "rejected"
                    dao.markFailed(item.key, item.revision, message)
                }
            }
        }

        return SyncOutcome(synced, shouldRetry || dao.getAll().any { it.lastError == null })
    }
}
