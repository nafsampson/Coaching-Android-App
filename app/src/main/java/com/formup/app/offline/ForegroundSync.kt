// Uploads queued changes when connected and retries temporary failures.

package com.formup.app.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.formup.app.security.runCatchingCancellable

internal class ForegroundSync(scope: CoroutineScope, private val canRun: () -> Boolean,
    private val retryDelayMillis: Long = 15_000,
    private val sync: suspend () -> SyncOutcome) {
    private val wake = Channel<Unit>(Channel.CONFLATED)
    init { scope.launch {
        for (signal in wake) {
            while (canRun()) {
                val result = runCatchingCancellable { sync() }
                val retry = result.getOrNull()?.retry ?: isConnectivityFailure(result.exceptionOrNull())
                if (!retry) break
                withTimeoutOrNull(retryDelayMillis) { wake.receive() }
            }
        }
    } }
    fun request() { wake.trySend(Unit) }
}
