// Provides the API methods used by the app.

package com.formup.app.data

import com.formup.app.security.SessionLease
import com.google.firebase.auth.FirebaseAuth

class FormUpApi(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    cache: ResponseCache? = null,
    session: SessionLease? = com.formup.app.security.AccountSessions.current.value
) {
    companion object {
        const val BASE_URL = "https://formup.runasp.net"
    }
    private val transport = ApiTransport(auth, session)
    private val seasonApi = SeasonApi(transport, cache)
    private val coachApi = CoachApi(transport)
    private val matchApi = MatchApi(transport)
    private val attendanceApi = AttendanceApi(transport)
    private val playerApi = PlayerApi(transport)
    private val trainingApi = TrainingApi(transport)
    private val formationApi = FormationApi(transport)

    suspend fun idempotentCreates(): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try { org.json.JSONObject(transport.request("GET", "/api/client-capabilities")).optBoolean("idempotentCreates", false) }
        catch (error: ApiException) { if (error.code == 404 || error.code == 501) false else throw error }
    }
    suspend fun loadSeason(): SeasonSnapshot = seasonApi.loadSeason()
    suspend fun cachedSeason(): SeasonSnapshot? = seasonApi.cachedSeason()

    suspend fun registerDeviceToken(token: String) = coachApi.registerDeviceToken(token)

    suspend fun updateReminderPreference(enabled: Boolean) = coachApi.updateReminderPreference(enabled)

    suspend fun updateCoach(
        displayName: String,
        preferredLanguage: String? = null
    ) = coachApi.updateCoach(displayName, preferredLanguage)

    suspend fun updateTeam(
        teamName: String,
        ageGroup: Int
    ) = coachApi.updateTeam(teamName, ageGroup)

    suspend fun updateMatchScores(
        matchId: String,
        teamScore: Int,
        opponentScore: Int
    ) = matchApi.updateMatchScores(matchId, teamScore, opponentScore)

    suspend fun getSquad(matchId: String): List<SquadUpdate> = matchApi.getSquad(matchId)

    suspend fun saveSquad(
        matchId: String,
        players: List<SquadUpdate>
    ) = matchApi.saveSquad(matchId, players)

    suspend fun saveMatchFormation(
        matchId: String,
        formationName: String,
        formationCode: String
    ) = matchApi.saveMatchFormation(matchId, formationName, formationCode)

    suspend fun saveStats(
        matchId: String,
        stats: List<StatUpdate>
    ) = matchApi.saveStats(matchId, stats)

    suspend fun createMatch(
        opponent: String,
        matchDateIso: String,
        venue: String, clientRequestId: String? = null
    ): String = matchApi.createMatch(opponent, matchDateIso, venue, clientRequestId)

    suspend fun updateMatch(
        matchId: String,
        opponent: String,
        matchDateIso: String,
        venue: String
    ) = matchApi.updateMatch(matchId, opponent, matchDateIso, venue)

    suspend fun deleteMatch(id: String) = matchApi.deleteMatch(id)

    suspend fun getAttendance(
        matchId: String
    ): List<AttendanceRecord> = attendanceApi.getAttendance(matchId)

    suspend fun saveAttendance(
        matchId: String,
        attendance: List<AttendanceRecord>
    ) = attendanceApi.saveAttendance(matchId, attendance)

    suspend fun getSessionAttendance(
        sessionId: String
    ): List<AttendanceRecord> = attendanceApi.getSessionAttendance(sessionId)

    suspend fun saveSessionAttendance(
        sessionId: String,
        attendance: List<AttendanceRecord>
    ) = attendanceApi.saveSessionAttendance(sessionId, attendance)

    suspend fun createPlayer(
        name: String,
        position: String,
        dateOfBirth: String?, clientRequestId: String? = null
    ) = playerApi.createPlayer(name, position, dateOfBirth, clientRequestId)

    suspend fun deletePlayer(id: String) = playerApi.deletePlayer(id)

    suspend fun updatePlayer(
        id: String,
        name: String,
        position: String,
        dateOfBirth: String?
    ) = playerApi.updatePlayer(id, name, position, dateOfBirth)

    suspend fun createSession(
        sessionDateIso: String,
        notes: String?, clientRequestId: String? = null
    ): String = trainingApi.createSession(sessionDateIso, notes, clientRequestId)

    suspend fun updateSession(
        sessionId: String,
        sessionDateIso: String,
        notes: String
    ) = trainingApi.updateSession(sessionId, sessionDateIso, notes)

    suspend fun deleteSession(id: String) = trainingApi.deleteSession(id)

    suspend fun getFormations(): List<SavedFormation> = formationApi.getFormations()

    suspend fun saveFormation(
        formationId: String? = null,
        name: String,
        code: String,
        positions: List<FormationSlot>, clientRequestId: String? = null
    ): String = formationApi.saveFormation(formationId, name, code, positions, clientRequestId)

    suspend fun deleteFormation(formationId: String) = formationApi.deleteFormation(formationId)
}
