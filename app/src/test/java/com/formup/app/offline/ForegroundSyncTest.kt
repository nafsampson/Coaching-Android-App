// Checks reconnect uploads, automatic retries and session stopping.

package com.formup.app.offline

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class ForegroundSyncTest {
    @Test fun reconnectWakeStartsQueuedWorkWithoutRetryButton() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        var online = false
        val done = CompletableDeferred<Unit>()
        val runner = ForegroundSync(scope, { online }, sync = { done.complete(Unit); SyncOutcome(1, false) })
        runner.request(); delay(30); assertFalse(done.isCompleted)
        online = true; runner.request(); withTimeout(2000) { done.await() }; scope.cancel()
    }
    @Test fun transientFailureRetriesAutomatically() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val done = CompletableDeferred<Unit>(); var attempts = 0
        // Use a short retry delay so this test does not wait for the production backoff.
        val runner = ForegroundSync(scope, { true }, retryDelayMillis = 10, sync = {
            attempts++; if (attempts == 1) SyncOutcome(0, true) else { done.complete(Unit); SyncOutcome(1, false) }
        })
        runner.request(); withTimeout(2000) { done.await() }; assertEquals(2, attempts); scope.cancel()
    }
    @Test fun newRevisionWakesBackoffImmediately() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val first = CompletableDeferred<Unit>(); val done = CompletableDeferred<Unit>(); var attempts = 0
        val runner = ForegroundSync(scope, { true }, retryDelayMillis = 60000, sync = {
            attempts++; if (attempts == 1) { first.complete(Unit); SyncOutcome(0, true) }
            else { done.complete(Unit); SyncOutcome(1, false) }
        })
        runner.request(); withTimeout(2000) { first.await() }; runner.request()
        withTimeout(2000) { done.await() }; assertEquals(2, attempts); scope.cancel()
    }
    @Test fun stoppedSessionCannotUploadOnLaterWake() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        var calls = 0
        val runner = ForegroundSync(scope, { false }, sync = { calls++; SyncOutcome(0, false) })
        runner.request(); delay(50); assertEquals(0, calls); scope.cancel()
    }
}
