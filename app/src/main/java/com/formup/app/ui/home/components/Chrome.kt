// Shows the app header and bottom navigation.

package com.formup.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.common.FormUpBrand
import com.formup.app.ui.theme.FormUpColors

@Composable
fun FormUpTopBar(hasUnread: Boolean, onNotifications: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = FormUpColors.Background) {
        Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            FormUpBrand(compact = true)
            Spacer(Modifier.weight(1f))
            BadgedBox(badge = { if (hasUnread) Badge(containerColor = FormUpColors.Danger) }) {
                IconButton(onClick = onNotifications, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Notifications, stringResource(if (hasUnread) R.string.mod_unread_notifications else R.string.notifications), tint = FormUpColors.TextPrimary)
                }
            }
        }
    }
}
enum class HomeTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home), Calendar("Calendar", Icons.Default.CalendarMonth),
    Team("Team", Icons.Default.Groups), Stats("Stats", Icons.Default.BarChart), Profile("Profile", Icons.Default.Person)
}
@Composable
private fun tabLabel(tab: HomeTab) = stringResource(when (tab) {
    HomeTab.Home -> R.string.nav_home; HomeTab.Calendar -> R.string.nav_calendar
    HomeTab.Team -> R.string.nav_team; HomeTab.Stats -> R.string.nav_stats; HomeTab.Profile -> R.string.nav_profile
})
@Composable
fun FormUpBottomBar(selected: HomeTab, onSelect: (HomeTab) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier, containerColor = FormUpColors.Surface, tonalElevation = 0.dp) {
        HomeTab.entries.forEach { tab ->
            val label = tabLabel(tab)
            NavigationBarItem(selected = selected == tab, onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(23.dp)) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = FormUpColors.PrimaryDeep,
                    selectedTextColor = FormUpColors.PrimaryDeep, unselectedIconColor = FormUpColors.TextSecondary,
                    unselectedTextColor = FormUpColors.TextSecondary, indicatorColor = FormUpColors.NavIndicator))
        }
    }
}
