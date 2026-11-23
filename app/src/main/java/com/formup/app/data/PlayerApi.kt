// Creates, edits and deletes players through the API.

package com.formup.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class PlayerApi(private val transport: ApiTransport) {
    suspend fun createPlayer(
        name: String,
        position: String,
        dateOfBirth: String?,
        clientRequestId: String? = null
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("name", name)
            .put("position", position)

        if (dateOfBirth == null) {
            body.put("dateOfBirth", JSONObject.NULL)
        } else {
            body.put("dateOfBirth", dateOfBirth)
        }

        if (clientRequestId != null) body.put("clientRequestId", clientRequestId)
        val response = transport.request(
            method = "POST",
            path = "/api/players",
            body = body
        )
        JSONObject(response).getString("id")
    }

    suspend fun deletePlayer(id: String) = withContext(Dispatchers.IO) {
        transport.request(
            method = "DELETE",
            path = "/api/players/$id"
        )
    }

    suspend fun updatePlayer(
        id: String,
        name: String,
        position: String,
        dateOfBirth: String?
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("name", name)
            .put("position", position)
        if (dateOfBirth != null) body.put("dateOfBirth", dateOfBirth)

        transport.request(method = "PATCH", path = "/api/players/$id", body = body)
    }

}
