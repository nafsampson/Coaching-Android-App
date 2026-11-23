// Defines how API responses are stored for offline use.

package com.formup.app.data

interface ResponseCache {
    suspend fun put(key: String, json: String)
    suspend fun get(key: String): String?
}
