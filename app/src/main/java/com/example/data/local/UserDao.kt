package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getFirstUser(): User?

    @Query("SELECT * FROM users ORDER BY createdAtMillis DESC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY createdAtMillis DESC")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT SUM(totalPracticeMinutes) FROM users")
    suspend fun getTotalSystemPracticeMinutes(): Long?

    @Query("UPDATE users SET dailyGoalMinutes = :daily, weeklyGoalMinutes = :weekly WHERE id = :userId")
    suspend fun updateGoals(userId: Long, daily: Int, weekly: Int)

    @Query("UPDATE users SET currentStreak = :streak, longestStreak = :longest, totalPracticeMinutes = :totalMinutes, level = :level, lastSessionDateMillis = :lastDate WHERE id = :userId")
    suspend fun updateStats(userId: Long, streak: Int, longest: Int, totalMinutes: Long, level: Int, lastDate: Long)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)
}
