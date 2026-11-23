// Checks new-event defaults and preservation of the coach’s chosen time.

package com.formup.app.ui

import com.formup.app.ui.calendar.*
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

class EventTimeDefaultsTest {
    @Test fun trainingStartsAtFiveThirtyInTheAfternoonByDefault() {
        assertEquals("17:30", TrainingFormState().time)
        assertEquals(LocalTime.of(17, 30), EventTimeDefaults.pickerTime(EventKind.TRAINING, ""))
    }
    @Test fun matchStartsAtTwelveThirtyByDefault() {
        assertEquals("12:30", MatchFormState().time)
        assertEquals(LocalTime.of(12, 30), EventTimeDefaults.pickerTime(EventKind.MATCH, ""))
    }
    @Test fun savedTimesAreNotOverwrittenWhenEditingOrReopeningThePicker() {
        assertEquals("19:15", TrainingFormState(time = "19:15").time)
        assertEquals(LocalTime.of(19, 15), EventTimeDefaults.pickerTime(EventKind.TRAINING, "19:15"))
        assertEquals(LocalTime.of(9, 45), EventTimeDefaults.pickerTime(EventKind.MATCH, "09:45"))
    }
    @Test fun invalidPickerTimeUsesTheCorrectEventDefault() {
        assertEquals(LocalTime.of(17, 30), EventTimeDefaults.pickerTime(EventKind.TRAINING, "invalid"))
        assertEquals(LocalTime.of(12, 30), EventTimeDefaults.pickerTime(EventKind.MATCH, "25:70"))
    }
}
