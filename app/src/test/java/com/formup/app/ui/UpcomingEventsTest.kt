// Checks that home shortcuts select future events by their real timestamps.

package com.formup.app.ui

import com.formup.app.data.Fixture
import com.formup.app.data.TrainingSession
import com.formup.app.ui.navigation.Destination
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class UpcomingEventsTest {
    private val now = Instant.parse("2026-11-23T08:00:00Z")
    private fun training(id: String, time: String) = TrainingSession(id, "", "", null, time)
    private fun match(id: String, time: String, played: Boolean = false) =
        Fixture(id, "Opponent", "", "", "Home", played = played, matchDateIso = time)

    @Test fun homeAttendanceOpensTrainingWithoutNeedingAMatch() {
        val target = UpcomingEvents.trainingAttendance(listOf(training("session", "2026-11-23T17:30:00+02:00")), now)
        assertEquals(Destination.SessionAttendance("session"), target)
    }
    @Test fun earliestFutureTrainingWinsEvenWhenTheListIsUnsorted() {
        val sessions = listOf(training("later", "2026-11-25T15:30:00Z"), training("past", "2026-11-22T15:30:00Z"),
            training("next", "2026-11-23T15:30:00Z"), training("bad", "invalid"))
        assertEquals("next", UpcomingEvents.nextTraining(sessions, now)?.id)
    }
    @Test fun earlierTodayIsPastAndTheNextFutureSessionIsUsed() {
        val sessions = listOf(training("earlier", "2026-11-23T07:30:00+02:00"), training("later", "2026-11-23T17:30:00+02:00"))
        assertEquals("later", UpcomingEvents.nextTraining(sessions, now)?.id)
    }
    @Test fun noFutureTrainingHasNoAttendanceDestination() {
        assertNull(UpcomingEvents.trainingAttendance(listOf(training("past", "2026-11-22T17:30:00Z")), now))
        assertNull(UpcomingEvents.trainingAttendance(emptyList(), now))
    }
    @Test fun lineupUsesEarliestUnplayedFutureMatch() {
        val fixtures = listOf(match("played", "2026-11-23T09:00:00Z", true), match("later", "2026-11-25T12:30:00Z"),
            match("past", "2026-11-22T12:30:00Z"), match("next", "2026-11-23T12:30:00Z"), match("bad", "invalid"))
        assertEquals("next", UpcomingEvents.nextMatch(fixtures, now)?.id)
    }
    @Test fun advancingTheCurrentTimeSelectsTheFollowingMatch() {
        val fixtures = listOf(match("first", "2026-11-23T09:00:00Z"), match("second", "2026-11-24T09:00:00Z"))
        assertEquals("first", UpcomingEvents.nextMatch(fixtures, now)?.id)
        assertEquals("second", UpcomingEvents.nextMatch(fixtures, Instant.parse("2026-11-23T09:00:01Z"))?.id)
    }
    @Test fun yearBoundaryAndOffsetsUseInstantsRatherThanLabels() {
        val sessions = listOf(training("later", "2027-01-01T01:30:00+02:00"), training("first", "2026-12-31T23:00:00+02:00"))
        assertEquals("first", UpcomingEvents.nextTraining(sessions, Instant.parse("2026-12-31T20:00:00Z"))?.id)
    }
    @Test fun eventStartingNowIsEligibleButPastOnlyMatchesAreNot() {
        assertEquals("now", UpcomingEvents.nextMatch(listOf(match("now", now.toString())), now)?.id)
        assertNull(UpcomingEvents.nextMatch(listOf(match("past", "2026-11-22T12:30:00Z")), now))
    }
}
