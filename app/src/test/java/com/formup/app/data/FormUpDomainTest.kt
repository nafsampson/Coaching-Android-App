// Checks match results, player participation and availability values.

package com.formup.app.data
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormUpDomainTest {

    private fun match(
        played: Boolean,
        ourScore: Int = 0,
        theirScore: Int = 0
    ) = Fixture(
        id = "test-match",
        opponent = "Test Opponent",
        dateLabel = "NOV 6",
        kickoff = "15:00",
        venue = "Home",
        played = played,
        teamScore = ourScore,
        opponentScore = theirScore
    )

    @Test
    fun completedMatchWithHigherScoreIsWin() {
        assertEquals("W", match(played = true, ourScore = 3, theirScore = 1).resultLetter)
    }

    @Test
    fun completedMatchWithLowerScoreIsLoss() {
        assertEquals("L", match(played = true, ourScore = 0, theirScore = 2).resultLetter)
    }

    @Test
    fun completedMatchWithEqualScoresIsDraw() {
        assertEquals("D", match(played = true, ourScore = 2, theirScore = 2).resultLetter)
    }

    @Test
    fun unplayedMatchDoesNotHaveResult() {
        val fixture = match(played = false)
        assertEquals("-", fixture.resultLetter)
        assertEquals("15:00", fixture.resultText)
    }

    @Test
    fun completedMatchDisplaysResultAndScoreline() {
        val fixture = match(played = true, ourScore = 2, theirScore = 1)
        assertEquals("2-1", fixture.scoreLine)
        assertEquals("W 2-1", fixture.resultText)
    }

    @Test
    fun playerWithoutRecordedStatsHasNotParticipated() {
        assertFalse(MatchLine().participated)
    }

    @Test
    fun playerWithMinutesOrGoalHasParticipated() {
        assertTrue(MatchLine(minutes = 45).participated)
        assertTrue(MatchLine(goals = 1).participated)
    }

    @Test
    fun availabilityFromApiHandlesApiValuesAndUnknownFallback() {
        assertEquals(AvailabilityStatus.Fit, AvailabilityStatus.fromApi("available"))
        assertEquals(AvailabilityStatus.Doubtful, AvailabilityStatus.fromApi("Knock"))
        assertEquals(AvailabilityStatus.Out, AvailabilityStatus.fromApi("INJURED"))
        assertEquals(AvailabilityStatus.Fit, AvailabilityStatus.fromApi("unrecognised"))
    }
}
