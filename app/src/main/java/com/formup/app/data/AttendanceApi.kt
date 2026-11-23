// Loads and saves match and training attendance.

package com.formup.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal class AttendanceApi(private val transport: ApiTransport) {
    suspend fun getAttendance(
        matchId: String
    ): List<AttendanceRecord> = withContext(Dispatchers.IO) {

        val response = transport.request(
            method = "GET",
            path = "/api/match/$matchId/attendance"
        )

        val array = JSONArray(response)

        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    AttendanceRecord(
                        playerId = item.optString("playerId"),
                        status = item.optString("status", ApiValues.NOT_RECORDED)
                    )
                )
            }
        }
    }

    suspend fun saveAttendance(
        matchId: String,
        attendance: List<AttendanceRecord>
    ) = withContext(Dispatchers.IO) {
        val array = JSONArray()

        attendance.forEach { record ->
            array.put(
                JSONObject()
                    .put("playerId", record.playerId)
                    .put("status", record.status)
            )
        }

        transport.request(
            method = "PUT",
            path = "/api/match/$matchId/attendance",
            body = array
        )
    }

    suspend fun getSessionAttendance(
        sessionId: String
    ): List<AttendanceRecord> = withContext(Dispatchers.IO) {

        val response = transport.request(
            method = "GET",
            path = "/api/session/$sessionId/attendance"
        )

        val array = JSONArray(response)

        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)

                add(
                    AttendanceRecord(
                        playerId = item.optString("playerId"),
                        status = item.optString("status", "NotRecorded")
                    )
                )
            }
        }
    }

    suspend fun saveSessionAttendance(
        sessionId: String,
        attendance: List<AttendanceRecord>
    ) = withContext(Dispatchers.IO) {

        val array = JSONArray()

        attendance.forEach { record ->
            array.put(
                JSONObject()
                    .put("playerId", record.playerId)
                    .put("status", record.status)
            )
        }

        transport.request(
            method = "PUT",
            path = "/api/session/$sessionId/attendance",
            body = array
        )
    }

}
