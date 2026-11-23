// Converts saved notification records into screen items.

package com.formup.app.notifications

import android.content.Context
import com.formup.app.R
import com.formup.app.data.AppNotification
import com.formup.app.offline.NotificationEntity

internal fun NotificationEntity.toAppNotification(context: Context) = AppNotification(
    id = id,
    kind = IncomingNotification.parseKind(kind),
    title = title,
    timestamp = relativeLabel(context, receivedAt),
    body = body,
    isRead = isRead,
    actionLabel = actionLabel,
    fixtureId = fixtureId
)

private fun relativeLabel(context: Context, receivedAt: Long): String {
    val minutes = ((System.currentTimeMillis() - receivedAt) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> context.getString(R.string.mod_just_now)
        minutes < 60 -> context.getString(R.string.mod_minutes_ago, minutes)
        minutes < 1440 -> context.getString(R.string.mod_hours_ago, minutes / 60)
        else -> context.getString(R.string.mod_days_ago, minutes / 1440)
    }
}
