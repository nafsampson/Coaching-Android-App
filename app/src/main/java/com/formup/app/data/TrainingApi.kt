// Creates, edits and deletes training sessions.

package com.formup.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class TrainingApi(private val transport: ApiTransport) {
    suspend fun createSession(
        sessionDateIso: String,
        notes: String?,
        clientRequestId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val body = JSONObject().put("sessionDate", sessionDateIso)
        body.put("notes", notes ?: JSONObject.NULL)
        if (clientRequestId != null) body.put("clientRequestId", clientRequestId)
        val response = transport.request(method = "POST", path = "/api/session", body = body)
        JSONObject(response).optString("id")
    }

    suspend fun updateSession(
        sessionId: String,
        sessionDateIso: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        transport.request(
            method = "PATCH",
            path = "/api/session/$sessionId",
            body = JSONObject()
                .put("sessionDate", sessionDateIso)
                .put("notes", notes)
        )
    }

    suspend fun deleteSession(id: String) = withContext(Dispatchers.IO) {
        transport.request(method = "DELETE", path = "/api/session/$id")
    }

}
