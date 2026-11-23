// Defines notification groups and screen items.

package com.formup.app.ui.notifications

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class NotificationGroup(val label: String) {
    New("NEW"),
    Earlier("EARLIER")
}

data class NotificationItem(
    val id: String,
    val icon: ImageVector,
    val iconBackground: Color,
    val iconTint: Color,
    val title: String,
    val timestamp: String,
    val body: String,
    val group: NotificationGroup,
    val actionLabel: String? = null
)

data class NotificationsUiState(
    val items: List<NotificationItem>
) {
    val grouped: Map<NotificationGroup, List<NotificationItem>>
        get() = items.groupBy { it.group }
}
