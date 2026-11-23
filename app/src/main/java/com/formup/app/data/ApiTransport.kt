// Sends authenticated HTTPS requests for the signed-in account.

package com.formup.app.data

import com.formup.app.security.AccountSessions
import com.formup.app.security.SessionLease
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import org.json.JSONObject

internal class ApiTransport(
    private val auth: FirebaseAuth,
    private val session: SessionLease?
) {
    fun request(method: String, path: String, body: Any? = null): String {
        // Check the same account before sending a request or accepting its response.
        val lease = session ?: AccountSessions.requireSession()
        lease.check()
        val user = auth.currentUser?.takeIf { it.uid == lease.uid }
            ?: throw kotlinx.coroutines.CancellationException("The account session changed")
        val token = Tasks.await(user.getIdToken(false)).token
            ?: throw ApiException(401, "Firebase did not return an ID token.")
        lease.check()
        val url = URL(FormUpApi.BASE_URL + path)
        require(url.protocol == "https") { "API transport requires HTTPS" }
        val connection = (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = false
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }
        try {
            if (body != null && method !in listOf("GET", "DELETE")) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                lease.check()
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            lease.check()
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            lease.check()
            if (code !in 200..299) {
                val title = runCatching { JSONObject(response).optString("title") }.getOrNull()

                // Keep server error pages out of messages shown to the coach.
                throw ApiException(code, title?.takeIf { it.isNotBlank() } ?: "API request failed ($code)")
            }
            return response
        } finally {
            connection.disconnect()
        }
    }
}
