package com.example.data.local

import android.content.Context
import com.example.data.model.ProgressLog
import com.example.data.model.Session
import com.example.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseProvider(private val context: Context) {
    val database = CodeStreakDatabase.getDatabase(context)
    val userDao = database.userDao()
    val sessionDao = database.sessionDao()
    val progressLogDao = database.progressLogDao()
    val preferences = PreferencesManager(context)

    suspend fun initializeDefaultDataIfNeeded(): User = withContext(Dispatchers.IO) {
        // Ensure dedicated admin account exists
        val existingAdmin = userDao.getUserByEmail("admin@codestreak.dev")
        if (existingAdmin == null) {
            val adminUser = User(
                username = "System Administrator",
                email = "admin@codestreak.dev",
                passwordHash = "admin123",
                isAdmin = true,
                dailyGoalMinutes = 60,
                weeklyGoalMinutes = 420,
                currentStreak = 12,
                longestStreak = 30,
                totalPracticeMinutes = 4800, // 80 hours
                level = 5,
                lastSessionDateMillis = System.currentTimeMillis()
            )
            userDao.insertUser(adminUser)
        }

        var user = if (preferences.currentUserId > 0) {
            userDao.getUserById(preferences.currentUserId)
        } else {
            userDao.getUserByEmail("alex.turner@dev.io") ?: userDao.getFirstUser()
        }

        if (user == null) {
            // Seed a starter developer user
            val starterUser = User(
                username = "Alex Turner",
                email = "alex.turner@dev.io",
                passwordHash = "streak123",
                isAdmin = false,
                dailyGoalMinutes = 45,
                weeklyGoalMinutes = 300,
                currentStreak = 4,
                longestStreak = 7,
                totalPracticeMinutes = 750, // 12.5 hours
                level = 2,
                lastSessionDateMillis = System.currentTimeMillis() - (6 * 60 * 60 * 1000)
            )
            val userId = userDao.insertUser(starterUser)
            val savedUser = starterUser.copy(id = userId)
            preferences.currentUserId = userId
            preferences.isLoggedIn = true
            preferences.isAdmin = false

            // Seed a few realistic sessions across recent days
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            val sampleSessions = listOf(
                Session(
                    userId = userId,
                    title = "Two Sum & Sliding Window Practice",
                    category = "Algorithms / LeetCode",
                    startTimeMillis = now - (3 * dayMillis + 14 * 3600 * 1000),
                    endTimeMillis = now - (3 * dayMillis + 13 * 3600 * 1000),
                    durationSeconds = 3600,
                    status = "COMPLETED",
                    notes = "Solved 3 Medium sliding window problems. Optimized O(n) space.",
                    shakeCount = 3,
                    quoteSnippet = "First, solve the problem. Then, write the code."
                ),
                Session(
                    userId = userId,
                    title = "Compose Navigation & Animations",
                    category = "Android / Kotlin",
                    startTimeMillis = now - (2 * dayMillis + 15 * 3600 * 1000),
                    endTimeMillis = now - (2 * dayMillis + 14 * 3600 * 1000),
                    durationSeconds = 4200,
                    status = "COMPLETED",
                    notes = "Built animated transitions between details and dashboard screens.",
                    shakeCount = 4,
                    quoteSnippet = "Make it work, make it right, make it fast."
                ),
                Session(
                    userId = userId,
                    title = "Redis Cache & Rate Limiting",
                    category = "System Design",
                    startTimeMillis = now - (1 * dayMillis + 16 * 3600 * 1000),
                    endTimeMillis = now - (1 * dayMillis + 15 * 3600 * 1000),
                    durationSeconds = 2700,
                    status = "COMPLETED",
                    notes = "Designed token bucket algorithm for API throttling.",
                    shakeCount = 2,
                    quoteSnippet = "Simplicity is prerequisite for reliability."
                ),
                Session(
                    userId = userId,
                    title = "Bug Fixing in Open Source Repo",
                    category = "Open Source",
                    startTimeMillis = now - (5 * 3600 * 1000),
                    endTimeMillis = now - (4 * 3600 * 1000),
                    durationSeconds = 3100,
                    status = "COMPLETED",
                    notes = "Fixed memory leak in background worker task.",
                    shakeCount = 3,
                    quoteSnippet = "Talk is cheap. Show me the code."
                )
            )

            for (session in sampleSessions) {
                val sId = sessionDao.insertSession(session)
                // Seed shake progress logs
                for (k in 1..session.shakeCount) {
                    progressLogDao.insertLog(
                        ProgressLog(
                            sessionId = sId,
                            userId = userId,
                            logType = "SHAKE_GESTURE",
                            note = "Shake Milestone #$k: Focused burst completed",
                            timestampMillis = session.startTimeMillis + (k * 15 * 60 * 1000),
                            quoteSnippet = session.quoteSnippet
                        )
                    )
                }
            }

            user = savedUser
        } else {
            preferences.currentUserId = user.id
            preferences.isLoggedIn = true
            preferences.isAdmin = user.isAdmin
        }

        user
    }
}