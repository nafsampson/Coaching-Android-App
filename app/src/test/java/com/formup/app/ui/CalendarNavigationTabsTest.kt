// Checks that event and lineup pages remain in the calendar tab.

package com.formup.app.ui

import com.formup.app.ui.navigation.Destination
import com.formup.app.ui.navigation.tab
import com.formup.app.ui.home.components.HomeTab
import org.junit.Assert.*
import org.junit.Test

class CalendarNavigationTabsTest {
    @Test fun cardAndEventPagesStayInsideTheCalendarTab() {
        listOf(Destination.Calendar, Destination.ManageEvent, Destination.EditMatch("m"), Destination.EditSession("s"),
            Destination.Lineup("m"), Destination.MatchDetails("m"), Destination.Attendance("m"), Destination.SessionAttendance("s"))
            .forEach { assertEquals(HomeTab.Calendar, it.tab) }
    }
}
