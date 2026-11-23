// Finds the next training session and unplayed match using their full timestamps.

package com.formup.app.ui

import com.formup.app.data.Fixture
import com.formup.app.data.ServerDates
import com.formup.app.data.TrainingSession
import com.formup.app.ui.navigation.Destination
import java.time.Instant

internal object UpcomingEvents {
    fun nextMatch(fixtures: List<Fixture>, now: Instant = Instant.now()): Fixture? =
        fixtures.filterNot { it.played }.mapNotNull { fixture ->
            ServerDates.instant(fixture.matchDateIso)?.takeIf { !it.isBefore(now) }?.let { it to fixture }
        }.minByOrNull { it.first }?.second

    fun nextTraining(sessions: List<TrainingSession>, now: Instant = Instant.now()): TrainingSession? =
        sessions.mapNotNull { session ->
            ServerDates.instant(session.sessionDateIso)?.takeIf { !it.isBefore(now) }?.let { it to session }
        }.minByOrNull { it.first }?.second

    fun trainingAttendance(sessions: List<TrainingSession>, now: Instant = Instant.now()): Destination.SessionAttendance? =
        nextTraining(sessions, now)?.let { Destination.SessionAttendance(it.id) }
}
