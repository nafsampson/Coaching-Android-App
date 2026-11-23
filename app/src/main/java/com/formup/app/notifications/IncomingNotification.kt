// Parses push messages and formats their display times.

package com.formup.app.notifications

import com.formup.app.data.NotificationKind
import java.util.UUID

data class IncomingNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val fixtureId: String?,
    val actionLabel: String?
) {
    companion object {

        fun from(
            messageId: String?,
            data: Map<String, String>,
            notificationTitle: String?,
            notificationBody: String?
        ): IncomingNotification? {
            val title = (data["title"] ?: notificationTitle).orEmpty().trim()
            val body = (data["body"] ?: notificationBody).orEmpty().trim()
            if (title.isEmpty() && body.isEmpty()) return null

            return IncomingNotification(
                id = messageId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                kind = parseKind(data["kind"]),
                title = title.ifEmpty { "FormUp" },
                body = body,
                fixtureId = data["fixtureId"]?.takeIf { it.isNotBlank() },
                actionLabel = data["actionLabel"]?.takeIf { it.isNotBlank() }
            )
        }

        fun parseKind(raw: String?): NotificationKind =
            NotificationKind.values().firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
                ?: NotificationKind.Schedule
    }
}

object RelativeTime {
    fun label(receivedAt: Long, now: Long = System.currentTimeMillis()): String {
        val minutes = ((now - receivedAt) / 60_000L).coerceAtLeast(0)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 24 * 60 -> "${minutes / 60}h ago"
            minutes < 48 * 60 -> "Yesterday"
            else -> "${minutes / (24 * 60)}d ago"
        }
    }
}
