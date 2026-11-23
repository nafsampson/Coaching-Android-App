// Sends queued player, event and formation changes to the API.

package com.formup.app.offline

import com.formup.app.data.FormUpApi

internal class ApiEntityRemote(private val api: FormUpApi) : EntityRemote {
    override suspend fun idempotentCreates() = api.idempotentCreates()
    override suspend fun create(type: EntityType, requestId: String, values: EntityValues): String = with(values) {
        when (type) {
            EntityType.PLAYER -> api.createPlayer(name, position, dateOfBirth, requestId)
            EntityType.MATCH -> api.createMatch(opponent, dateIso, venue, requestId)
            EntityType.SESSION -> api.createSession(dateIso, notes, requestId)
            EntityType.FORMATION -> api.saveFormation(null, name, code, positions, requestId)
        }
    }
    override suspend fun update(type: EntityType, id: String, values: EntityValues) { with(values) {
        when (type) {
            EntityType.PLAYER -> api.updatePlayer(id, name, position, dateOfBirth)
            EntityType.MATCH -> api.updateMatch(id, opponent, dateIso, venue)
            EntityType.SESSION -> api.updateSession(id, dateIso, notes.orEmpty())
            EntityType.FORMATION -> api.saveFormation(id, name, code, positions)
        }
    } }
    override suspend fun delete(type: EntityType, id: String) { when (type) {
        EntityType.PLAYER -> api.deletePlayer(id)
        EntityType.MATCH -> api.deleteMatch(id)
        EntityType.SESSION -> api.deleteSession(id)
        EntityType.FORMATION -> api.deleteFormation(id)
    } }
}
