package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val isAdmin: Boolean = false,
    val dailyGoalMinutes: Int = 45,
    val weeklyGoalMinutes: Int = 300,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val totalPracticeMinutes: Long = 0,
    val level: Int = 1,
    val lastSessionDateMillis: Long = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
)
