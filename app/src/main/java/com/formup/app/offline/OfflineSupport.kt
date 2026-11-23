// Tracks connectivity and provides helpers for cached responses and attendance JSON.

package com.formup.app.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.formup.app.data.ResponseCache
import com.google.firebase.FirebaseNetworkException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG = "FormUpOffline"

object SyncKind {
    const val MATCH_ATTENDANCE = "match_attendance"
    const val SESSION_ATTENDANCE = "session_attendance"
}

object PayloadCodec {
    private val gson = Gson()
    private val type = object : TypeToken<Map<String, String>>() {}.type

    fun encode(statuses: Map<String, String>): String = gson.toJson(statuses)

    fun decode(json: String): Map<String, String> =
        requireNotNull(gson.fromJson<Map<String, String>>(json, type)) { "Invalid queued attendance" }

    fun keyFor(kind: String, targetId: String) = "$kind:$targetId"
}

fun isConnectivityFailure(error: Throwable?): Boolean {
    val causes = generateSequence(error) { it.cause }.take(8).toList()
    if (causes.any { it is javax.net.ssl.SSLHandshakeException || it is javax.net.ssl.SSLPeerUnverifiedException }) return false
    return causes.any { it is IOException || it is FirebaseNetworkException }
}

class RoomResponseCache(private val dao: ApiCacheDao, private val session: com.formup.app.security.SessionLease) : ResponseCache {
    override suspend fun put(key: String, json: String) {
        session.check()
        dao.put(CachedResponseEntity(key, json, System.currentTimeMillis()))
        session.check()
    }

    override suspend fun get(key: String): String? {
        session.check()
        return dao.get(key)?.json.also { session.check() }
    }
}

object ConnectivityObserver {

    fun isOnlineNow(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun observe(context: Context): Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                trySend(isOnlineNow(context))
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(isOnlineNow(context))
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                trySend(isOnlineNow(context))
            }
        }
        trySend(isOnlineNow(context))
        cm.registerDefaultNetworkCallback(callback)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
