// Keeps the device push token linked to the correct account.

package com.formup.app.notifications

import android.content.Context
import com.formup.app.security.SessionLease
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

object PushSessionRegistry {
    private val lock = Mutex()
    private fun prefs(context: Context) = context.getSharedPreferences("formup_push_owner", Context.MODE_PRIVATE)
    suspend fun token(context: Context, session: SessionLease): String = lock.withLock {
        session.check()
        val store = prefs(context)
        if (store.getString("owner", null) != session.uid) {
            // Rotate the token so the previous account cannot receive pushes on this phone.
            FirebaseMessaging.getInstance().deleteToken().await()
            session.check()
        }
        val token = FirebaseMessaging.getInstance().token.await()
        session.check()
        store.edit().putString("owner", session.uid).apply()
        token
    }
    fun invalidate(context: Context) { prefs(context).edit().remove("owner").apply() }
    fun belongsTo(context: Context, uid: String): Boolean = prefs(context).getString("owner", null) == uid
}
