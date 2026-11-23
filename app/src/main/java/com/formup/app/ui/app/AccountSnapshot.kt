// Loads the coach and team details needed after sign-in.

package com.formup.app.ui.app

import android.content.Context
import com.formup.app.data.FormUpApi
import com.formup.app.data.SeasonSnapshot
import com.formup.app.offline.FormUpDatabase
import com.formup.app.offline.PendingOverlay
import com.formup.app.offline.RoomResponseCache
import com.formup.app.offline.WriteLocks
import com.formup.app.security.SessionLease
import kotlinx.coroutines.sync.withLock

internal suspend fun accountSnapshot(context: Context, owner: SessionLease): SeasonSnapshot {
    val cache = RoomResponseCache(FormUpDatabase.get(context, owner.uid).apiCacheDao(), owner)
    return WriteLocks.forAccount(owner.uid).withLock {
        val snapshot = FormUpApi(cache = cache, session = owner).loadSeason()
        PendingOverlay.apply(snapshot, FormUpDatabase.get(context, owner.uid).pendingSyncDao().getAll())
    }
}
