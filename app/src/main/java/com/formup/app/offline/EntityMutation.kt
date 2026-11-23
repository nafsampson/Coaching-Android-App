// Defines queued changes and converts them to and from JSON.

package com.formup.app.offline

import com.formup.app.data.FormationSlot
import com.google.gson.Gson

enum class EntityType { PLAYER, MATCH, SESSION, FORMATION }
enum class EntityAction { CREATE, UPDATE, DELETE }
data class EntityValues(val name: String = "", val position: String = "", val dateOfBirth: String? = null,
    val opponent: String = "", val dateIso: String = "", val venue: String = "", val notes: String? = null,
    val code: String = "", val positions: List<FormationSlot> = emptyList())
data class EntityMutation(val type: EntityType, val action: EntityAction, val id: String, val values: EntityValues)

object EntityCodec {
    private val gson = Gson()
    fun kind(type: EntityType) = "entity_${type.name.lowercase()}"
    fun isKind(kind: String) = kind.startsWith("entity_")
    fun local(id: String) = id.startsWith("local-")
    fun requestId(id: String) = id.removePrefix("local-")
    fun key(type: EntityType, id: String) = "${type.name}:$id"
    fun encode(value: EntityMutation): String = gson.toJson(value)
    fun decode(json: String): EntityMutation = requireNotNull(gson.fromJson(json, EntityMutation::class.java))
    fun next(existing: EntityMutation?, type: EntityType, requested: EntityAction, id: String, values: EntityValues) =
        EntityMutation(type, if (requested == EntityAction.UPDATE && existing?.action == EntityAction.CREATE) EntityAction.CREATE else requested, id, values)
}

class DependencyPendingException : Exception("A related record is still waiting to be added")
class UncertainCreateException(cause: Throwable? = null) : Exception("The new record needs checking before it can be retried", cause)
