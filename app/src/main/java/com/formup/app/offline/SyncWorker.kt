// Schedules connected background uploads and recovery retries.

package com.formup.app.offline

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.formup.app.security.AccountSessions
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val owner = inputData.getString("owner_uid") ?: return Result.failure()
        val session = AccountSessions.current.value?.takeIf { it.uid == owner } ?: return Result.success()
        return try {
            if (SyncEngine.run(applicationContext, session).retry) Result.retry() else Result.success()
        } catch (error: kotlinx.coroutines.CancellationException) { throw error }
        catch (error: Exception) { if (isConnectivityFailure(error)) Result.retry() else Result.failure() }
    }
}

object SyncScheduler {
    private fun name(ownerUid: String) = "formup_sync_$ownerUid"
    fun schedule(context: Context, ownerUid: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("owner_uid" to ownerUid))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        val manager = WorkManager.getInstance(context)
        manager.enqueueUniqueWork(name(ownerUid), ExistingWorkPolicy.KEEP, request)
        val recovery = androidx.work.PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf("owner_uid" to ownerUid))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        manager.enqueueUniquePeriodicWork("${name(ownerUid)}_recovery", androidx.work.ExistingPeriodicWorkPolicy.KEEP, recovery)
    }
    fun cancel(context: Context, ownerUid: String) {
        WorkManager.getInstance(context).cancelUniqueWork(name(ownerUid))
        WorkManager.getInstance(context).cancelUniqueWork("${name(ownerUid)}_recovery")

        WorkManager.getInstance(context).cancelUniqueWork("formup_offline_sync")
    }
}
