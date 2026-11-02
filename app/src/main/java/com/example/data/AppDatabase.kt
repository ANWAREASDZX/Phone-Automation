package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val commandText: String,
    val planSummary: String,
    val riskLevel: String,
    val executionResult: String,
    val latencyMs: Long,
    val isDryRun: Boolean,
    val verifiedState: String?
)

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 5")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun pruneLogsOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM audit_logs")
    suspend fun clearAll()
}

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val descriptionAr: String,
    val triggerType: String,
    val triggerTime: String,
    val daysOfWeek: String, // comma-separated e.g. "1,2,3,4,5"
    val priority: Int,
    val isEnabled: Boolean,
    val planJson: String,
    val conflictWarning: String?
)

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY priority ASC, triggerTime ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(routine: RoutineEntity)

    @Update
    suspend fun update(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Entity(tableName = "learned_patterns")
data class LearnedPatternEntity(
    @PrimaryKey val id: String,
    val triggerPhrase: String,
    val intentName: String,
    val defaultParamsJson: String,
    val executionCount: Int,
    val consecutiveSuccesses: Int,
    val status: String, // "OBSERVING", "PROPOSED", "ADOPTED", "PAUSED"
    val rationaleAr: String,
    val driftScore: Float,
    val lastObserved: Long
)

@Dao
interface LearnedPatternDao {
    @Query("SELECT * FROM learned_patterns ORDER BY lastObserved DESC")
    fun getAllPatterns(): Flow<List<LearnedPatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(pattern: LearnedPatternEntity)

    @Query("UPDATE learned_patterns SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: String, newStatus: String)

    @Query("DELETE FROM learned_patterns")
    suspend fun clearAllPatterns()
}

@Entity(tableName = "cached_intents")
data class CachedIntentEntity(
    @PrimaryKey val patternKey: String,
    val intentName: String,
    val actionType: String,
    val defaultTarget: String,
    val riskLevel: String,
    val descriptionAr: String,
    val hitCount: Int = 0
)

@Dao
interface CachedIntentDao {
    @Query("SELECT * FROM cached_intents")
    suspend fun getAllCached(): List<CachedIntentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(intents: List<CachedIntentEntity>)

    @Query("UPDATE cached_intents SET hitCount = hitCount + 1 WHERE patternKey = :key")
    suspend fun incrementHit(key: String)
}

@Database(
    entities = [
        AuditLogEntity::class,
        RoutineEntity::class,
        LearnedPatternEntity::class,
        CachedIntentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun auditLogDao(): AuditLogDao
    abstract fun routineDao(): RoutineDao
    abstract fun learnedPatternDao(): LearnedPatternDao
    abstract fun cachedIntentDao(): CachedIntentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "phone_automation.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
