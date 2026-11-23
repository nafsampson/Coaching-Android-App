// Loads, saves and deletes formation templates.

package com.formup.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal class FormationApi(private val transport: ApiTransport) {
    suspend fun getFormations(): List<SavedFormation> =
        withContext(Dispatchers.IO) {

            val response = transport.request(
                method = "GET",
                path = "/api/formations"
            )

            val array = JSONArray(response)

            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val positionsArray = item.optJSONArray("positions") ?: JSONArray()

                    val positions = buildList {
                        for (positionIndex in 0 until positionsArray.length()) {
                            val position = positionsArray.getJSONObject(positionIndex)

                            add(
                                FormationSlot(
                                    slotId = position.optString("slotId"),
                                    label = position.optString("label"),
                                    x = position.optDouble("x"),
                                    y = position.optDouble("y")
                                )
                            )
                        }
                    }

                    add(
                        SavedFormation(
                            id = item.optString("id"),
                            name = item.optString("name"),
                            code = item.optString("code"),
                            positions = positions
                        )
                    )
                }
            }
        }

    suspend fun saveFormation(
        formationId: String? = null,
        name: String,
        code: String,
        positions: List<FormationSlot>,
        clientRequestId: String? = null
    ): String = withContext(Dispatchers.IO) {

        val positionsArray = JSONArray()

        positions.forEach { position ->
            positionsArray.put(
                JSONObject()
                    .put("slotId", position.slotId)
                    .put("label", position.label)
                    .put("x", position.x)
                    .put("y", position.y)
            )
        }

        val body = JSONObject()
            .put("name", name)
            .put("code", code)
            .put("positions", positionsArray)

        if (clientRequestId != null) body.put("clientRequestId", clientRequestId)
        val isEditing = !formationId.isNullOrBlank()

        val response = transport.request(
            method = if (isEditing) "PUT" else "POST",
            path = if (isEditing) "/api/formations/$formationId" else "/api/formations",
            body = body
        )

        if (isEditing) formationId!! else JSONObject(response).optString("id")
    }

    suspend fun deleteFormation(formationId: String) =
        withContext(Dispatchers.IO) {
            transport.request(
                method = "DELETE",
                path = "/api/formations/$formationId"
            )
        }
}
