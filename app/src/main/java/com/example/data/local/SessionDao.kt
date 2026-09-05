package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): Session?

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionFlow(sessionId: Long): Flow<Session?>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTimeMillis DESC")
    fun getAllSessionsForUserFlow(userId: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTimeMillis DESC")
    suspend fun getAllSessionsForUser(userId: Long): List<Session>

    @Query("SELECT * FROM sessions WHERE userId = :userId AND startTimeMillis >= :startTimeMillis ORDER BY startTimeMillis DESC")
    suspend fun getSessionsSince(userId: Long, startTimeMillis: Long): List<Session>

    @Query("SELECT * FROM sessions WHERE userId = :userId AND category = :category ORDER BY startTimeMillis DESC")
    fun getSessionsByCategoryFlow(userId: Long, category: String): Flow<List<Session>>

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getTotalGlobalSessionsCount(): Int

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions")
    suspend fun getTotalGlobalDurationSeconds(): Long

    @Query("SELECT COALESCE(SUM(shakeCount), 0) FROM sessions")
    suspend fun getTotalGlobalShakesCount(): Int

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE userId = :userId AND status = 'COMPLETED'")
    fun getTotalPracticeSecondsFlow(userId: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE userId = :userId AND startTimeMillis >= :startOfDayMillis AND status = 'COMPLETED'")
    fun getTodayPracticeSecondsFlow(userId: Long, startOfDayMillis: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM sessions WHERE userId = :userId AND startTimeMillis >= :startOfWeekMillis AND status = 'COMPLETED'")
    fun getWeekPracticeSecondsFlow(userId: Long, startOfWeekMillis: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM sessions WHERE userId = :userId AND status = 'COMPLETED'")
    fun getTotalCompletedSessionsCountFlow(userId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(shakeCount), 0) FROM sessions WHERE userId = :userId")
    fun getTotalShakesCountFlow(userId: Long): Flow<Int>
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM sessions WHERE userId = :userId")
    suspend fun deleteAllSessionsForUser(userId: Long)
}
