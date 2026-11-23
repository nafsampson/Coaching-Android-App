// Checks offline record changes, ID mapping and safe create retries.

package com.formup.app.offline

import com.formup.app.data.*
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EntityOfflineTest {
    private val id = "local-123e4567-e89b-12d3-a456-426614174000"
    private fun mutation(action: EntityAction = EntityAction.CREATE) = EntityMutation(EntityType.PLAYER, action, id, EntityValues(name = "A player", position = "Forward"))
    private fun snapshot() = SeasonSnapshot(CoachProfile("Coach", "en", true, true), TeamProfile("Team", "U12"), emptyList(), emptyList(), fromCache = true)
    private fun row(m: EntityMutation) = PendingSyncEntity("${EntityCodec.kind(m.type)}:${m.id}", EntityCodec.kind(m.type), m.id, EntityCodec.encode(m), 1L, "revision")

    @Test fun editBeforeUploadStaysOneCreate() {
        val edited = EntityCodec.next(mutation(), EntityType.PLAYER, EntityAction.UPDATE, id, EntityValues(name = "Edited"))
        assertEquals(EntityAction.CREATE, edited.action)
        assertEquals("Edited", EntityCodec.decode(EntityCodec.encode(edited)).values.name)
    }
    @Test fun pendingPlayerAppearsAfterOfflineRestart() {
        assertEquals("A player", EntityProjection.apply(snapshot(), listOf(row(mutation())), emptyList()).players.single().name)
    }
    @Test fun serverReceiptKeepsSameUiIdentityWithoutDuplicates() {
        val receipt = EntitySyncState(EntityCodec.key(EntityType.PLAYER, id), remoteId = "remote")
        val server = snapshot().copy(players = listOf(Player("remote", "A player", "Forward")), fromCache = false)
        val result = EntityProjection.apply(server, listOf(row(mutation())), listOf(receipt))
        assertEquals(1, result.players.size); assertEquals(id, result.players.single().id)
    }
    @Test fun confirmedCreateStillAppearsBeforeCacheRefresh() {
        val receipt = EntitySyncState(EntityCodec.key(EntityType.PLAYER, id), "remote", confirmedPayload = EntityCodec.encode(mutation()))
        assertEquals(id, EntityProjection.apply(snapshot(), emptyList(), listOf(receipt)).players.single().id)
    }
    @Test fun pendingDeleteHidesRecordAndDependencies() {
        val m = mutation(EntityAction.DELETE)
        val receipt = EntitySyncState(EntityCodec.key(EntityType.PLAYER, id), "remote")
        val base = snapshot().copy(players = listOf(Player("remote", "A player", "Forward")))
        assertTrue(EntityProjection.apply(base, listOf(row(m)), listOf(receipt)).players.isEmpty())
        assertNull(StableEntityIds(listOf(receipt), listOf(row(m))).resolve(EntityType.PLAYER, id))
    }
    @Test fun unresolvedDependencyWaitsInsteadOfSendingTemporaryId() {
        try { StableEntityIds(emptyList()).resolve(EntityType.PLAYER, id); fail() } catch (_: DependencyPendingException) {}
    }
    @Test fun resolvedDependencyUsesServerId() {
        assertEquals("remote", StableEntityIds(listOf(EntitySyncState(EntityCodec.key(EntityType.PLAYER, id), "remote"))).resolve(EntityType.PLAYER, id))
    }
    @Test fun createThenDeleteNeverCallsServer() = runBlocking {
        val remote = Remote(); EntityUploader(States(), {}, remote).upload(mutation(EntityAction.DELETE))
        assertEquals(0, remote.creates); assertEquals(0, remote.deletes)
    }
    @Test fun successfulCreatePersistsReceiptAndNeverPostsAgain() = runBlocking {
        val remote = Remote(); val states = States(); val uploader = EntityUploader(states, {}, remote)
        uploader.upload(mutation()); uploader.upload(mutation())
        assertEquals(1, remote.creates); assertEquals("remote", states.rows.values.single().remoteId)
        assertEquals(2, remote.updates)
    }
    @Test fun dnsFailureCanRetrySafelyOnLegacyServer() = runBlocking {
        val remote = Remote().apply { error = UnknownHostException() }; val states = States(); val uploader = EntityUploader(states, {}, remote)
        try { uploader.upload(mutation()); fail() } catch (_: UnknownHostException) {}
        assertFalse(states.rows.values.single().attempted)
        remote.error = null; uploader.upload(mutation()); assertEquals(2, remote.creates)
    }
    @Test fun lostResponseOnLegacyServerStopsBlindReplay() = runBlocking {
        val remote = Remote().apply { error = IOException("lost response") }; val uploader = EntityUploader(States(), {}, remote)
        repeat(2) { try { uploader.upload(mutation()); fail() } catch (_: UncertainCreateException) {} }
        assertEquals(1, remote.creates)
    }
    @Test fun idempotentServerRetriesSameRequestUuid() = runBlocking {
        val remote = Remote().apply { supported = true; error = IOException() }; val uploader = EntityUploader(States(), {}, remote)
        try { uploader.upload(mutation()); fail() } catch (_: IOException) {}
        remote.error = null; uploader.upload(mutation())
        assertEquals(listOf(EntityCodec.requestId(id), EntityCodec.requestId(id)), remote.requestIds)
    }
    @Test fun processDeathBeforeReceiptUsesSameIdempotentRequest() = runBlocking {
        val states = States(); states.put(EntitySyncState(EntityCodec.key(EntityType.PLAYER, id), attempted = true, idempotent = true))
        val remote = Remote().apply { supported = true }; EntityUploader(states, {}, remote).upload(mutation())
        assertEquals(1, remote.creates)
    }
    @Test fun processDeathOnLegacyServerRequiresReview() = runBlocking {
        val states = States(); states.put(EntitySyncState(EntityCodec.key(EntityType.PLAYER, id), attempted = true))
        val remote = Remote()
        try { EntityUploader(states, {}, remote).upload(mutation()); fail() } catch (_: UncertainCreateException) {}
        assertEquals(0, remote.creates)
    }
    @Test fun failedPatchAfterCreateRetriesPatchWithoutDuplicatePost() = runBlocking {
        val states = States(); val remote = Remote().apply { patchError = IOException() }; val uploader = EntityUploader(states, {}, remote)
        try { uploader.upload(mutation()); fail() } catch (_: IOException) {}
        remote.patchError = null; uploader.upload(mutation()); assertEquals(1, remote.creates)
    }
    @Test fun deletedRemoteRecordIsSafeToRetry() = runBlocking {
        val states = States(); states.put(EntitySyncState(EntityCodec.key(EntityType.PLAYER, id), "remote"))
        val remote = Remote().apply { deleteError = ApiException(404, "missing") }
        EntityUploader(states, {}, remote).upload(mutation(EntityAction.DELETE))
        assertTrue(states.rows.values.single().deleted)
    }
    @Test fun sslCertificateFailureIsNotOffline() { assertFalse(isConnectivityFailure(javax.net.ssl.SSLHandshakeException("bad cert"))) }
    @Test fun wrappedDnsFailureIsOffline() { assertTrue(isConnectivityFailure(IllegalStateException(UnknownHostException()))) }

    private class States : EntitySyncDao {
        val rows = linkedMapOf<String, EntitySyncState>()
        override suspend fun get(key: String) = rows[key]
        override suspend fun getAll() = rows.values.toList()
        override suspend fun put(value: EntitySyncState) { rows[value.key] = value }
    }
    private class Remote : EntityRemote {
        var supported = false; var creates = 0; var updates = 0; var deletes = 0
        var error: Exception? = null; var patchError: Exception? = null; var deleteError: Exception? = null
        val requestIds = mutableListOf<String>()
        override suspend fun idempotentCreates() = supported
        override suspend fun create(type: EntityType, requestId: String, values: EntityValues): String { creates++; requestIds.add(requestId); error?.let { throw it }; return "remote" }
        override suspend fun update(type: EntityType, id: String, values: EntityValues) { updates++; patchError?.let { throw it } }
        override suspend fun delete(type: EntityType, id: String) { deletes++; deleteError?.let { throw it } }
    }
}
