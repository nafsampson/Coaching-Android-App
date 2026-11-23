// Checks queue revisions, failed uploads and interrupted saves.

package com.formup.app.offline

import com.formup.app.data.ApiException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OutboxProcessorTest {
    private fun row(revision: String = "first") = PendingSyncEntity("match_report:m", WriteKind.MATCH_REPORT, "m", "{}", 1L, revision)

    @Test fun partialUploadRemainsDurableAndReplaysUntilBothStepsFinish() = runBlocking {
        val dao = FakeDao(); dao.upsert(row())
        var attempts = 0
        var statisticsWrites = 0
        var scoreWrites = 0
        val processor = OutboxProcessor(dao, {}, {
            statisticsWrites++
            attempts++
            if (attempts == 1) throw IOException("Score upload interrupted after statistics succeeded")
            scoreWrites++
        })
        assertTrue(processor.process().retry)
        assertNotNull(dao.get(row().key))
        assertFalse(processor.process().retry)
        assertNull(dao.get(row().key))
        assertEquals(2, statisticsWrites)
        assertEquals(1, scoreWrites)
    }

    @Test fun acknowledgementOfOlderRevisionCannotDeleteNewEditEvenWithSameTimestamp() = runBlocking {
        val dao = FakeDao(); dao.upsert(row())
        val result = OutboxProcessor(dao, {}, { dao.upsert(row("newer")) }).process()
        assertEquals(0, result.confirmed)
        assertEquals("newer", dao.get(row().key)?.revision)
    }

    @Test fun permanentRejectionIsVisibleAndRetainedUntilExplicitRetry() = runBlocking {
        val dao = FakeDao(); dao.upsert(row())
        val rejected = OutboxProcessor(dao, {}, { throw ApiException(400, "Invalid") }).process()
        assertFalse(rejected.retry)
        assertNotNull(dao.get(row().key)?.lastError)
        var uploads = 0
        val retry = OutboxProcessor(dao, {}, { uploads++ })
        retry.process(); assertEquals(0, uploads)
        dao.retryFailed(); retry.process()
        assertEquals(1, uploads); assertNull(dao.get(row().key))
    }

    @Test fun sessionCancellationDoesNotUploadOrDiscardAnotherAccountsData() = runBlocking {
        val dao = FakeDao(); dao.upsert(row())
        var uploads = 0
        try {
            OutboxProcessor(dao, { throw CancellationException("Signed out") }, { uploads++ }).process()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
        assertEquals(0, uploads); assertNotNull(dao.get(row().key))
        assertNull(dao.get(row().key)?.lastError)
    }

    @Test fun repeatedEditsUseUniqueRevisionsAndCoalescePerTarget() = runBlocking {
        val dao = FakeDao(); val writes = PendingWrites(dao) {}
        writes.put(WriteKind.MATCH_REPORT, "m", "old")
        val first = dao.get(row().key)!!.revision
        writes.put(WriteKind.MATCH_REPORT, "m", "latest")
        assertEquals(1, dao.getAll().size)
        assertNotEquals(first, dao.get(row().key)!!.revision)
        assertEquals("latest", dao.get(row().key)!!.payloadJson)
    }
    @Test fun newWorkAddedDuringPassAutomaticallyRequestsAnotherPass() = runBlocking {
        val dao = FakeDao(); dao.upsert(row())
        val outcome = OutboxProcessor(dao, {}, { dao.upsert(row("other").copy(key = "match_report:other", targetId = "other")) }).process()
        assertTrue(outcome.retry); assertEquals(1, outcome.confirmed)
    }
    @Test fun temporaryServerErrorStaysQueuedForAutomaticRetry() = runBlocking {
        val dao = FakeDao(); dao.upsert(row())
        assertTrue(OutboxProcessor(dao, {}, { throw ApiException(503, "unavailable") }).process().retry)
        assertNull(dao.get(row().key)?.lastError)
    }
    @Test fun ambiguousCreateIsRetainedAndNotAutomaticallyReplayed() = runBlocking {
        val dao = FakeDao(); dao.upsert(row())
        assertFalse(OutboxProcessor(dao, {}, { throw UncertainCreateException() }).process().retry)
        assertEquals("uncertain", dao.get(row().key)?.lastError)
        var calls = 0; OutboxProcessor(dao, {}, { calls++ }).process(); assertEquals(0, calls)
    }

}

private class FakeDao : PendingSyncDao {
    override fun observeAll() = kotlinx.coroutines.flow.flowOf(rows.values.toList())
    override suspend fun retryAuth() {}
    private val rows = linkedMapOf<String, PendingSyncEntity>()
    override suspend fun upsert(item: PendingSyncEntity) { rows[item.key] = item }
    override suspend fun getAll() = rows.values.toList()
    override suspend fun get(key: String) = rows[key]
    override suspend fun deleteIfUnchanged(key: String, revision: String): Int {
        if (rows[key]?.revision != revision) return 0
        rows.remove(key); return 1
    }
    override suspend fun delete(key: String) { rows.remove(key) }
    override suspend fun clear() { rows.clear() }
    override suspend fun markFailed(key: String, revision: String, message: String) {
        rows[key]?.takeIf { it.revision == revision }?.let { rows[key] = it.copy(lastError = message) }
    }
    override suspend fun retryFailed() { rows.keys.toList().forEach { key -> rows[key] = rows.getValue(key).copy(lastError = null) } }
    override fun observeCount(): Flow<Int> = MutableStateFlow(rows.size)
    override fun observeFailedCount(): Flow<Int> = MutableStateFlow(rows.values.count { it.lastError != null })
    override fun observeFailures(): Flow<List<PendingSyncEntity>> = MutableStateFlow(rows.values.filter { it.lastError != null })
}
