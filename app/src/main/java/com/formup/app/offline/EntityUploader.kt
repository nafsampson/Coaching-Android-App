// Uploads record changes and prevents unsafe retries of new records.

package com.formup.app.offline

import com.formup.app.data.ApiException
import java.net.ConnectException
import java.net.UnknownHostException
import com.google.firebase.FirebaseNetworkException

interface EntityRemote {
    suspend fun idempotentCreates(): Boolean
    suspend fun create(type: EntityType, requestId: String, values: EntityValues): String
    suspend fun update(type: EntityType, id: String, values: EntityValues)
    suspend fun delete(type: EntityType, id: String)
}

class EntityUploader(private val states: EntitySyncDao, private val checkSession: () -> Unit, private val remote: EntityRemote) {
    suspend fun upload(mutation: EntityMutation) {
        checkSession()
        val key = EntityCodec.key(mutation.type, mutation.id)
        var state = states.get(key) ?: EntitySyncState(key)
        var id = state.remoteId ?: mutation.id.takeUnless(EntityCodec::local)
        when (mutation.action) {
            EntityAction.DELETE -> {
                if (id != null) try { remote.delete(mutation.type, id) } catch (error: ApiException) { if (error.code != 404) throw error }
                checkSession()
                states.put(state.copy(deleted = true, confirmedPayload = EntityCodec.encode(mutation)))
                return
            }
            EntityAction.CREATE, EntityAction.UPDATE -> {
                if (id == null) {
                    val supported = remote.idempotentCreates()
                    // A lost create response is only safe to retry when the API supports the same request ID.
                    if (state.attempted && (!state.idempotent || !supported)) throw UncertainCreateException()
                    state = state.copy(attempted = true, idempotent = supported)
                    // Store the attempt before sending it so an app restart cannot cause a blind retry.
                    states.put(state)
                    checkSession()
                    try {
                        id = remote.create(mutation.type, EntityCodec.requestId(mutation.id), mutation.values)
                        require(!id.isNullOrBlank()) { "No ID returned for created record" }
                    } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        if (definitelyNotSent(error) || (error is ApiException && error.code in 400..499 && error.code != 408)) {
                            states.put(state.copy(attempted = false))
                            throw error
                        }
                        if (!supported) throw UncertainCreateException(error)
                        throw error
                    }
                    checkSession()
                    // Save the server ID before updating fields or uploading dependent records.
                    state = state.copy(remoteId = id, attempted = false)
                    states.put(state)
                }
                remote.update(mutation.type, requireNotNull(id), mutation.values)
                checkSession()
                states.put(state.copy(remoteId = id, deleted = false, confirmedPayload = EntityCodec.encode(mutation)))
            }
        }
    }

    private fun definitelyNotSent(error: Throwable): Boolean {
        var current: Throwable? = error
        repeat(8) {
            if (current is UnknownHostException || current is ConnectException || current is FirebaseNetworkException) return true
            current = current?.cause
        }
        return false
    }
}
