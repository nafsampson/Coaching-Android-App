// Stores pending changes, cached data and notifications separately for each account.

package com.formup.app.offline

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.security.MessageDigest
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey val key: String,
    val kind: String,
    val targetId: String,
    val payloadJson: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "''") val revision: String,
    val lastError: String? = null
)

@Entity(tableName = "api_cache")
data class CachedResponseEntity(
    @PrimaryKey val key: String,
    val json: String,
    val updatedAt: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val title: String,
    val body: String,
    val receivedAt: Long,
    val isRead: Boolean = false,
    val fixtureId: String? = null,
    val actionLabel: String? = null
)

@Dao
interface PendingSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PendingSyncEntity)

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingSyncEntity>

    @Query("SELECT * FROM pending_sync WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): PendingSyncEntity?

    // Only acknowledge the revision that was uploaded; keep any newer edit.
    @Query("DELETE FROM pending_sync WHERE `key` = :key AND revision = :revision")
    suspend fun deleteIfUnchanged(key: String, revision: String): Int

    @Query("DELETE FROM pending_sync WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("UPDATE pending_sync SET lastError = :message WHERE `key` = :key AND revision = :revision")
    suspend fun markFailed(key: String, revision: String, message: String)

    @Query("UPDATE pending_sync SET lastError = NULL WHERE lastError != 'uncertain'")
    suspend fun retryFailed()

    @Query("UPDATE pending_sync SET lastError = NULL WHERE lastError = 'auth'")
    suspend fun retryAuth()

    @Query("SELECT * FROM pending_sync WHERE lastError IS NOT NULL ORDER BY createdAt")
    fun observeFailures(): Flow<List<PendingSyncEntity>>

    @Query("SELECT COUNT(*) FROM pending_sync WHERE lastError IS NOT NULL")
    fun observeFailedCount(): Flow<Int>

    @Query("DELETE FROM pending_sync")
    suspend fun clear()

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<PendingSyncEntity>>

    @Query("SELECT COUNT(*) FROM pending_sync")
    fun observeCount(): Flow<Int>
}

@Dao
interface ApiCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(item: CachedResponseEntity)

    @Query("SELECT * FROM api_cache WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CachedResponseEntity?

    @Query("DELETE FROM api_cache")
    suspend fun clear()
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY receivedAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications")
    suspend fun clear()
}

@Database(
    entities = [PendingSyncEntity::class, CachedResponseEntity::class, NotificationEntity::class, EntitySyncState::class],
    version = 3,
    exportSchema = false
)
abstract class FormUpDatabase : RoomDatabase() {
    abstract fun entitySyncDao(): EntitySyncDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun apiCacheDao(): ApiCacheDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pending_sync ADD COLUMN revision TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pending_sync ADD COLUMN lastError TEXT")
                db.execSQL("UPDATE pending_sync SET revision = lower(hex(randomblob(16)))")
            }
        }
        private val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `entity_sync_state` (`key` TEXT NOT NULL, `remoteId` TEXT, `attempted` INTEGER NOT NULL, `idempotent` INTEGER NOT NULL, `confirmedPayload` TEXT, `deleted` INTEGER NOT NULL, PRIMARY KEY(`key`))")
                db.execSQL("UPDATE pending_sync SET lastError = NULL WHERE lastError = 'rejected' AND kind IN ('match_attendance','session_attendance','match_report','match_lineup','profile')")
            }
        }
        private val instances = mutableMapOf<String, FormUpDatabase>()
        fun get(context: Context, ownerUid: String): FormUpDatabase = synchronized(instances) {
            require(ownerUid.isNotBlank())
            val suffix = MessageDigest.getInstance("SHA-256").digest(ownerUid.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            instances.getOrPut(suffix) {
                // Use a separate database for each account.
                Room.databaseBuilder(context.applicationContext, FormUpDatabase::class.java, "formup_$suffix.db")
                    .addMigrations(migration1To2, migration2To3).build()
            }
        }
    }
}
