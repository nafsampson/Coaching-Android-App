// Stores server IDs and upload results for locally created records.

package com.formup.app.offline

import androidx.room.*

@Entity(tableName = "entity_sync_state")
data class EntitySyncState(
    @PrimaryKey val key: String,
    val remoteId: String? = null,
    val attempted: Boolean = false,
    val idempotent: Boolean = false,
    val confirmedPayload: String? = null,
    val deleted: Boolean = false
)

@Dao
interface EntitySyncDao {
    @Query("SELECT * FROM entity_sync_state WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): EntitySyncState?
    @Query("SELECT * FROM entity_sync_state")
    suspend fun getAll(): List<EntitySyncState>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(value: EntitySyncState)
}
