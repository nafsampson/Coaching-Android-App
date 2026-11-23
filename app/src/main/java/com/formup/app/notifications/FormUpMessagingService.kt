// Checks incoming push messages and saves them for the current account.

package com.formup.app.notifications

import com.formup.app.R
import com.formup.app.data.FormUpApi
import com.formup.app.offline.FormUpDatabase
import com.formup.app.offline.NotificationEntity
import com.formup.app.security.AccountSessions
import com.formup.app.security.runCatchingCancellable
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking

class FormUpMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val session = AccountSessions.current.value ?: return
        if (!PushSessionRegistry.belongsTo(this, session.uid)) return
        try { runBlocking { runCatchingCancellable { FormUpApi(session = session).registerDeviceToken(token) } } }
        catch (_: CancellationException) {                                                }
    }
    override fun onMessageReceived(message: RemoteMessage) {
        val session = AccountSessions.current.value ?: return
        if (!PushSessionRegistry.belongsTo(this, session.uid)) return
        // Ignore messages intended for another account.
        val explicitOwner = message.data["ownerUid"]
        if (explicitOwner != null && explicitOwner != session.uid) return
        val parsed = IncomingNotification.from(message.messageId, message.data, null, null) ?: return

        try { runBlocking {
            runCatchingCancellable {
                // Verify messages without an owner before showing their contents.
                val item = if (explicitOwner == session.uid) parsed else {
                    if (parsed.fixtureId != null) FormUpApi(session = session).getSquad(parsed.fixtureId)
                    parsed.copy(title = getString(R.string.mod_update_title), body = getString(R.string.mod_update_body), actionLabel = null)
                }
                session.check()
                FormUpDatabase.get(applicationContext, session.uid).notificationDao().insert(NotificationEntity(
                    id = item.id, kind = item.kind.name, title = item.title, body = item.body,
                    receivedAt = System.currentTimeMillis(), fixtureId = item.fixtureId, actionLabel = item.actionLabel
                ))
                session.check()
                NotificationHelper.showPush(applicationContext, item, session.uid)
            }
        } } catch (_: CancellationException) {                                                   }
    }
}
