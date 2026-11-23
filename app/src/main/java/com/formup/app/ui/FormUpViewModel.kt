// Coordinates season data, feature controllers, navigation and sync status.

package com.formup.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.formup.app.R
import com.formup.app.data.ActivityEntry
import com.formup.app.data.AppNotification
import com.formup.app.data.CoachProfile
import com.formup.app.data.Fixture
import com.formup.app.data.FormUpApi
import com.formup.app.data.NotificationKind
import com.formup.app.data.Player
import com.formup.app.data.SavedFormation
import com.formup.app.data.SeasonSnapshot
import com.formup.app.data.SquadUpdate
import com.formup.app.data.StatUpdate
import com.formup.app.data.TeamProfile
import com.formup.app.data.TrainingSession
import com.formup.app.data.resultWord
import com.formup.app.notifications.IncomingNotification
import com.formup.app.notifications.NotificationHelper
import com.formup.app.notifications.NotificationPrefs
import com.formup.app.notifications.toAppNotification
import com.formup.app.offline.ConnectivityObserver
import com.formup.app.offline.FormUpDatabase
import com.formup.app.offline.PendingWrites
import com.formup.app.offline.RoomResponseCache
import com.formup.app.offline.SyncKind
import com.formup.app.offline.SyncScheduler
import com.formup.app.offline.WriteKind
import com.formup.app.offline.WriteLocks
import com.formup.app.offline.WritePayloads
import com.formup.app.offline.isConnectivityFailure
import com.formup.app.security.AccountSessions
import com.formup.app.security.runCatchingCancellable
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.navigation.Destination
import com.formup.app.ui.navigation.destination
import com.formup.app.ui.navigation.tab
import com.formup.app.ui.stats.PlayerStatEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

internal class FormUpViewModel(application: Application) : AndroidViewModel(application), MessageSink {

    private val session = AccountSessions.current.value
    private val sessionJob = SupervisorJob(viewModelScope.coroutineContext[Job])
    private val sessionScope = CoroutineScope(viewModelScope.coroutineContext + sessionJob)
    private val db = FormUpDatabase.get(application, session?.uid ?: "signed-out-${java.util.UUID.randomUUID()}")
    private val writes = session?.let { PendingWrites(db.pendingSyncDao(), it) }
    private val notificationDao = db.notificationDao()
    private val api = FormUpApi(cache = session?.let { RoomResponseCache(db.apiCacheDao(), it) }, session = session)
    private var pushSetupDone = false
    private var activated = false

    var isOnline by mutableStateOf(ConnectivityObserver.isOnlineNow(application))
        private set
    var failedChanges by mutableStateOf<List<String>>(emptyList())
        private set
    var failedSyncCount by mutableStateOf(0)
        private set
    var pendingSyncCount by mutableStateOf(0)
        private set
    var showingCachedData by mutableStateOf(false)
        private set

    private val foreground = com.formup.app.offline.ForegroundSync(sessionScope,
        { activated && isOnline && session != null && AccountSessions.current.value == session },
        sync = { com.formup.app.offline.SyncEngine.run(getApplication(), requireNotNull(session)) })

    private val state = SeasonState()
    internal val attendance = AttendanceController(state, application, api, sessionScope, this, db, session)
    internal val entities = EntityController(sessionScope, session, writes, this, ::projectEntity,
        { session?.let { SyncScheduler.schedule(application, it.uid) } }, ::back)
    internal val formations = FormationController(state, application, api, sessionScope, this, db, entities, session)
    internal val lineup = LineupController(state, api, sessionScope, this, ::back, writes, session) { session?.let { SyncScheduler.schedule(application, it.uid) } }
    internal val reports = ReportBuilder(state, application) { pendingSyncCount > 0 }
    internal val presentation = SeasonPresenter(state, application)
    internal val calendar = CalendarPresenter(state, application)
    internal val profile = ProfileController(state, application, api, sessionScope, this, ::back, writes, session)
    val players: List<Player> get() = state.playerList
    val fixtures: List<Fixture> get() = state.fixtureList
    val notifications: List<AppNotification> get() = state.notificationList
    val nextFixture: Fixture? get() = state.nextFixture
    fun openTrainingAttendance() {
        // Resolve at tap time so a session that has already started is not selected.
        val destination = UpcomingEvents.trainingAttendance(state.sessionList)
        if (destination == null) notify(R.string.msg_no_upcoming_training)
        else navigate(destination)
    }
    val lastPlayedFixture: Fixture? get() = state.lastPlayedFixture
    val savedFormations: List<SavedFormation> get() = state.savedFormations
    val coach: CoachProfile get() = state.coach
    val team: TeamProfile get() = state.team
    fun fixture(id: String): Fixture? = state.fixture(id)
    fun player(id: String): Player? = state.player(id)
    fun session(id: String): TrainingSession? = state.session(id)

    init {
        viewModelScope.launch {
            AccountSessions.current.collect { current ->
                if (current != session) {
                    sessionScope.cancel()
                    session?.let { SyncScheduler.cancel(application, it.uid) }
                    androidx.core.app.NotificationManagerCompat.from(application).cancelAll()
                    state.clear()
                    pendingSyncCount = 0
                    failedSyncCount = 0
                    failedChanges = emptyList()
                    showingCachedData = false
                }
            }
        }
        if (session != null) {

            sessionScope.launch {
                notificationDao.observeAll().collect { rows ->
                    state.notificationList.clear()
                    state.notificationList.addAll(rows.map { it.toAppNotification(application) })
                }
            }

            sessionScope.launch {
                // Watch revisions as well as counts so replacing a queued edit also wakes sync.
                db.pendingSyncDao().observeAll().collect { rows ->
                    val count = rows.size
                    val completed = pendingSyncCount > 0 && count == 0
                    pendingSyncCount = count
                    if (activated) {
                        WriteLocks.forAccount(session.uid).withLock {
                            api.cachedSeason()?.let { applySnapshot(it) }
                        }
                        foreground.request()
                        if (completed) { refreshFromApi(); formations.loadFormations() }
                    }
                }
            }

            sessionScope.launch {
                var first = true
                ConnectivityObserver.observe(application).collect { online ->
                    isOnline = online
                    Log.i("FormUpOffline", "Connectivity changed: ${if (online) "online" else "offline"}")
                    if (online && !first && activated) {
                        foreground.request()
                        SyncScheduler.schedule(application, session.uid)
                        refreshFromApi()
                    }
                    first = false
                }
            }
            sessionScope.launch {
                db.pendingSyncDao().observeFailures().collect { failures ->
                    failedSyncCount = failures.size
                    failedChanges = failures.map { item ->
                        val target = state.fixture(item.targetId)?.opponent ?: state.session(item.targetId)?.notes
                            ?: getApplication<Application>().getString(R.string.sync_change)
                        val reason = getApplication<Application>().getString(if (item.lastError == "uncertain") R.string.offline_uncertain else if (item.lastError == "auth" || item.lastError.orEmpty().startsWith("Sign in again")) R.string.mod_sync_auth else R.string.mod_sync_rejected)
                        "$target: $reason"
                    }
                }
            }
        }
    }

    fun activateSession() {
        val owner = session ?: return
        if (activated) return
        owner.check()
        activated = true
        sessionScope.launch { db.pendingSyncDao().retryAuth(); foreground.request() }
        SyncScheduler.schedule(getApplication(), owner.uid)
        refreshFromApi()
        formations.loadFormations()
    }

    private var refreshRevision = 0L

    fun refreshFromApi() {
        if (session == null || !activated) return
        session.check()
        val revision = ++refreshRevision
        sessionScope.launch {
            runCatchingCancellable {
                WriteLocks.forAccount(session.uid).withLock {
                    val snapshot = api.loadSeason()
                    session.check()
                    if (revision == refreshRevision) { applySnapshot(snapshot); setupPush() }
                }
            }
                .onFailure { error ->
                    if (isConnectivityFailure(error)) {
                        Log.w("FormUpOffline", "Could not load live data; checking cached data")
                    }
                    notify(if (isConnectivityFailure(error)) R.string.offline_connection else R.string.msg_load_data_failed)
                }
        }
    }

    private fun setupPush() {
        val owner = session ?: return
        if (pushSetupDone || snapshotIsCached()) return
        val app = getApplication<Application>()
        NotificationPrefs.syncSubscriptions(app)
        sessionScope.launch {
            runCatchingCancellable {
                val token = com.formup.app.notifications.PushSessionRegistry.token(app, owner)
                NotificationPrefs.saveToken(app, token)
                api.registerDeviceToken(token)
                if (writes?.get(WriteKind.REMINDERS, "reminders") == null) api.updateReminderPreference(NotificationPrefs.matchReminders(app))
                owner.check()
                pushSetupDone = true
                Log.i("FormUpFCM", "Device token registered with API")
            }.onFailure { Log.w("FormUpFCM", "Token registration skipped: ${it.message}") }
        }
    }

    private fun snapshotIsCached() = showingCachedData

    fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getStringExtra(NotificationHelper.EXTRA_OWNER_UID) != session?.uid) return
        val kind = intent?.getStringExtra(NotificationHelper.EXTRA_KIND) ?: return
        val fixtureId = intent.getStringExtra(NotificationHelper.EXTRA_FIXTURE_ID)
        intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_ID)?.let { id ->
            sessionScope.launch { notificationDao.markRead(id) }
        }
        Log.i("FormUpFCM", "Opened from notification: $kind")
        navigateForNotification(IncomingNotification.parseKind(kind), fixtureId)
    }

    private fun navigateForNotification(kind: NotificationKind, fixtureId: String?) {
        if (fixtureId != null && state.fixture(fixtureId) == null) {
            navigate(Destination.Notifications)
            return
        }
        when (kind) {
            NotificationKind.Attendance -> fixtureId?.let { navigate(Destination.Attendance(it)) }
            NotificationKind.Schedule -> fixtureId?.let { navigate(Destination.MatchDetails(it)) }
            NotificationKind.Stats -> fixtureId?.let { navigate(Destination.MatchReport(it)) }
            NotificationKind.Fitness -> navigate(Destination.Team)
        }
        if (fixtureId == null && kind != NotificationKind.Fitness) navigate(Destination.Notifications)
    }

    private suspend fun applySnapshot(confirmed: SeasonSnapshot) {
        session?.check()
        val pending = db.pendingSyncDao().getAll()
        val receipts = db.entitySyncDao().getAll()
        val ids = com.formup.app.offline.StableEntityIds(receipts)
        val normalized = pending.map { item -> item.copy(targetId = when (item.kind) {
            SyncKind.SESSION_ATTENDANCE -> ids.ui(com.formup.app.offline.EntityType.SESSION, item.targetId)
            WriteKind.MATCH_LINEUP, WriteKind.MATCH_REPORT, SyncKind.MATCH_ATTENDANCE -> ids.ui(com.formup.app.offline.EntityType.MATCH, item.targetId)
            else -> item.targetId
        }) }
        val snapshot = com.formup.app.offline.PendingOverlay.apply(com.formup.app.offline.EntityProjection.apply(confirmed, pending, receipts), normalized)
        session?.check()
        state.playerList.clear(); state.playerList.addAll(snapshot.players)
        state.fixtureList.clear(); state.fixtureList.addAll(snapshot.fixtures.sortedBy { com.formup.app.data.ServerDates.instant(it.matchDateIso) ?: java.time.Instant.MAX })
        state.sessionList.clear(); state.sessionList.addAll(snapshot.sessions)
        val app = getApplication<Application>()

        state.coach = snapshot.coach.copy(
            matchReminders = NotificationPrefs.matchReminders(app),
            weeklySummary = NotificationPrefs.weeklySummary(app)
        )
        state.team = snapshot.team
        showingCachedData = snapshot.fromCache

        snapshot.fixtures.forEach { f ->
            // Do not overwrite unsaved lineup choices during a background refresh.
            if (lineup.hasDraft(f.id)) return@forEach

            val completeSquad = snapshot.players.associate { player ->
                player.id to (
                        f.squad[player.id] ?: SquadUpdate(
                            playerId = player.id,
                            status = lineup.availabilityToApi(player.status),
                            selection = "NotSelected"
                        )
                        )
            }.toMutableMap()

            state.matchSquadEntries[f.id] = completeSquad

            state.matchSquadSelections[f.id] =
                completeSquad.mapValues { it.value.selection }.toMutableMap()

            state.matchSquadStatuses[f.id] =
                completeSquad.mapValues { it.value.status }.toMutableMap()

            val savedFormation = state.savedFormations.firstOrNull { formation ->
                (f.formationCode.isNotBlank() &&
                        formation.code.equals(f.formationCode, ignoreCase = true)) ||
                        (f.formationName.isNotBlank() &&
                                formation.name.equals(f.formationName, ignoreCase = true))
            }

            if (savedFormation != null) {
                state.selectedMatchFormations[f.id] = savedFormation
            }
        }

        lineup.refreshSquadStatuses()
        rebuildDerivedActivity()
        if (!confirmed.fromCache) receipts.filter { it.confirmedPayload != null && !it.key.startsWith("FORMATION:") }.forEach {
            db.entitySyncDao().put(it.copy(confirmedPayload = null))
        }
    }

    private fun rebuildDerivedActivity() {
        state.activityList.clear()
        state.fixtureList.filter { it.played }.sortedByDescending { it.matchDateIso }.take(4).forEach { match ->
            state.activityList.add(
                ActivityEntry(
                    id = "match-${match.id}",
                    title = getApplication<Application>().getString(R.string.loc_match_vs, match.opponent),
                    subtitle = "${match.resultWord} ${match.scoreLine}",
                    date = match.dateLabel.uppercase(),
                    isTraining = false
                )
            )
        }
    }

    var message by mutableStateOf<UiMessage?>(null)
        private set

    var busy by mutableStateOf(false)
        private set

    private val backStack = mutableStateListOf<Destination>(Destination.Home)

    val current: Destination get() = backStack.last()
    val currentTab: HomeTab get() = current.tab
    val canGoBack: Boolean get() = backStack.size > 1

    fun navigate(destination: Destination) {
        if (backStack.last() != destination) backStack.add(destination)
    }

    fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun selectTab(tab: HomeTab) {
        if (tab != currentTab) calendar.resetCalendarDate()
        val root = tab.destination
        backStack.clear()
        backStack.add(root)
    }

    override fun notify(text: String) {
        message = if (listOf("unable to resolve host", "unknownhost", "failed to connect", "network is unreachable", "timeout").any { text.contains(it, ignoreCase = true) })
            UiMessage.Resource(R.string.offline_connection, emptyList()) else UiMessage.Raw(text)
    }

    override fun notify(@StringRes resourceId: Int, vararg args: Any) {
        message = UiMessage.Resource(resourceId, args.toList())
    }

    fun consumeMessage() {
        message = null
    }

    private fun runApi(
        @StringRes success: Int,
        @StringRes failure: Int,
        successArgs: List<Any> = emptyList(),
        backAfter: Boolean = false,
        call: suspend () -> Unit
    ) {
        if (busy) return
        busy = true
        sessionScope.launch {
            runCatchingCancellable { call() }
                .onSuccess {
                    val refreshed = runCatchingCancellable {
                        val owner = session ?: return@launch
                        WriteLocks.forAccount(owner.uid).withLock { applySnapshot(api.loadSeason()) }
                    }.isSuccess
                    if (refreshed) notify(success, *successArgs.toTypedArray())
                    else notify(R.string.msg_refresh_after_save_failed)
                    if (backAfter) back()
                }
                .onFailure { error ->
                    if (isConnectivityFailure(error)) {
                        Log.w("FormUpOffline", "This operation needs a connection", error)
                    }
                    notify(failure)
                }
            busy = false
        }
    }

    fun addPlayer(name: String, position: String, dateOfBirth: String?) =
        entities.create(com.formup.app.offline.EntityType.PLAYER, com.formup.app.offline.EntityValues(name = name, position = position, dateOfBirth = dateOfBirth))
    fun editPlayer(id: String, name: String, position: String, dateOfBirth: String?) =
        entities.update(com.formup.app.offline.EntityType.PLAYER, id, com.formup.app.offline.EntityValues(name = name, position = position, dateOfBirth = dateOfBirth))
    fun deletePlayer(id: String) = entities.delete(com.formup.app.offline.EntityType.PLAYER, id)
    fun addMatch(opponent: String, matchDateIso: String, venue: String) =
        entities.create(com.formup.app.offline.EntityType.MATCH, com.formup.app.offline.EntityValues(opponent = opponent, dateIso = matchDateIso, venue = venue))
    fun editMatch(id: String, opponent: String, matchDateIso: String, venue: String) =
        entities.update(com.formup.app.offline.EntityType.MATCH, id, com.formup.app.offline.EntityValues(opponent = opponent, dateIso = matchDateIso, venue = venue))
    fun deleteMatch(id: String) = entities.delete(com.formup.app.offline.EntityType.MATCH, id)
    fun addSession(sessionDateIso: String, notes: String?) =
        entities.create(com.formup.app.offline.EntityType.SESSION, com.formup.app.offline.EntityValues(dateIso = sessionDateIso, notes = notes))
    fun editSession(id: String, sessionDateIso: String, notes: String) =
        entities.update(com.formup.app.offline.EntityType.SESSION, id, com.formup.app.offline.EntityValues(dateIso = sessionDateIso, notes = notes))
    fun deleteSession(id: String) = entities.delete(com.formup.app.offline.EntityType.SESSION, id)

    private suspend fun projectEntity(mutation: com.formup.app.offline.EntityMutation) {
        session?.check()
        val snapshot = com.formup.app.offline.EntityProjection.one(SeasonSnapshot(state.coach, state.team,
            state.playerList.toList(), state.fixtureList.toList(), state.sessionList.toList(), true), mutation)
        state.playerList.clear(); state.playerList.addAll(snapshot.players)
        state.fixtureList.clear(); state.fixtureList.addAll(snapshot.fixtures)
        state.sessionList.clear(); state.sessionList.addAll(snapshot.sessions)
        if (mutation.type == com.formup.app.offline.EntityType.FORMATION) {
            state.formationList.removeAll { it.id == mutation.id }
            if (mutation.action != com.formup.app.offline.EntityAction.DELETE) state.formationList.add(
                SavedFormation(mutation.id, mutation.values.name, mutation.values.code, mutation.values.positions))
        }
    }

    fun saveMatchStats(fixtureId: String, entries: List<PlayerStatEntry>, teamScore: Int, opponentScore: Int) {
        val owner = session ?: return
        val queue = writes ?: return
        if (teamScore !in 0..99 || opponentScore !in 0..99 || entries.any {
            it.goals < 0 || it.assists < 0 || it.minutesPlayed !in 0..120 || !it.rating.isFinite() || it.rating !in 0.0..10.0
        }) { notify(R.string.msg_statistics_save_failed); return }
        val stats = entries.map { StatUpdate(it.id, it.goals, it.assists,
            opponentScore == 0 && it.minutesPlayed > 0, it.minutesPlayed, it.rating) }
        val payload = WritePayloads.report(teamScore, opponentScore, stats)
        sessionScope.launch {
            runCatchingCancellable {
                queue.put(WriteKind.MATCH_REPORT, fixtureId, payload)
                SyncScheduler.schedule(getApplication(), owner.uid)
            }.onSuccess { notify(R.string.msg_saved_pending_sync); back() }
                .onFailure { notify(R.string.msg_statistics_save_failed) }
        }
    }

    fun markNotificationRead(id: String) {
        sessionScope.launch { notificationDao.markRead(id) }
    }

    fun markAllNotificationsRead() {
        sessionScope.launch { notificationDao.markAllRead() }
        notify(R.string.loc_all_read)
    }

    fun openNotification(id: String) {
        val notification = state.notificationList.firstOrNull { it.id == id } ?: return
        markNotificationRead(id)
        when (notification.kind) {
            NotificationKind.Attendance ->
                notification.fixtureId?.let { navigate(Destination.Attendance(it)) }
            NotificationKind.Schedule ->
                notification.fixtureId?.let { navigate(Destination.MatchDetails(it)) }
            NotificationKind.Stats ->
                notification.fixtureId?.let { navigate(Destination.MatchReport(it)) }
            NotificationKind.Fitness -> navigate(Destination.Team)
        }
    }

    fun retryPendingChanges() {
        val owner = session ?: return
        sessionScope.launch {

            // Do not replay an uncertain create through the general Retry action.
            db.pendingSyncDao().getAll().filter { it.lastError != null && it.lastError != "uncertain" }.forEach {
                db.pendingSyncDao().upsert(it.copy(lastError = null))
            }
            foreground.request()
            SyncScheduler.schedule(getApplication(), owner.uid)
            setupPush()
            refreshFromApi()
        }
    }

    fun signOut() {
        session?.let { SyncScheduler.cancel(getApplication(), it.uid) }
        NotificationPrefs.unsubscribeAll()
        com.formup.app.notifications.PushSessionRegistry.invalidate(getApplication())
        androidx.core.app.NotificationManagerCompat.from(getApplication()).cancelAll()
        sessionScope.cancel()
        state.clear()
        AccountSessions.signOut()
    }
}
