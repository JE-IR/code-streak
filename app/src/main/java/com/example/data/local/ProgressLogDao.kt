package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ProgressLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ProgressLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ProgressLog>)

    @Query("SELECT * FROM progress_logs WHERE sessionId = :sessionId ORDER BY timestampMillis ASC")
    fun getLogsForSessionFlow(sessionId: Long): Flow<List<ProgressLog>>

    @Query("SELECT * FROM progress_logs WHERE sessionId = :sessionId ORDER BY timestampMillis ASC")
    suspend fun getLogsForSession(sessionId: Long): List<ProgressLog>

    @Query("SELECT * FROM progress_logs WHERE userId = :userId ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentLogsForUserFlow(userId: Long, limit: Int = 50): Flow<List<ProgressLog>>

    @Query("SELECT COUNT(*) FROM progress_logs WHERE userId = :userId AND logType = 'SHAKE_GESTURE'")
    fun getShakeCountForUserFlow(userId: Long): Flow<Int>

    @Query("DELETE FROM progress_logs WHERE sessionId = :sessionId")
    suspend fun deleteLogsForSession(sessionId: Long)

    @Query("DELETE FROM progress_logs")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM progress_logs WHERE userId = :userId")
    suspend fun deleteLogsForUser(userId: Long)

    @Delete
    suspend fun deleteLog(log: ProgressLog)
}
