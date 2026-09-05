package com.example.util

import com.example.data.model.Badge
import com.example.data.model.ProjectionData
import com.example.data.model.Session
import com.example.data.model.User
import com.example.data.model.UserLevelInfo
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max

object StatsCalculator {

    private val LEVEL_TITLES = listOf(
        "Novice Coder",          // Lv 1: 0 - 5 hrs (300 mins)
        "Script Explorer",       // Lv 2: 5 - 15 hrs (900 mins)
        "Syntax Sorcerer",       // Lv 3: 15 - 30 hrs (1800 mins)
        "Algorithm Artisan",     // Lv 4: 30 - 60 hrs (3600 mins)
        "Full-Stack Phoenix",    // Lv 5: 60 - 100 hrs (6000 mins)
        "System Architect",      // Lv 6: 100 - 250 hrs (15000 mins)
        "Principal Engineer",    // Lv 7: 250 - 500 hrs (30000 mins)
        "Code Titan"             // Lv 8: 500+ hrs
    )

    private val LEVEL_THRESHOLDS_MINUTES = listOf(
        0L, 300L, 900L, 1800L, 3600L, 6000L, 15000L, 30000L
    )

    fun calculateUserLevel(totalMinutes: Long): UserLevelInfo {
        var level = 1
        for (i in LEVEL_THRESHOLDS_MINUTES.indices.reversed()) {
            if (totalMinutes >= LEVEL_THRESHOLDS_MINUTES[i]) {
                level = i + 1
                break
            }
        }
        val cappedLevel = level.coerceIn(1, LEVEL_TITLES.size)
        val title = LEVEL_TITLES[cappedLevel - 1]

        val currentTierStart = LEVEL_THRESHOLDS_MINUTES[cappedLevel - 1]
        val nextTierEnd = if (cappedLevel < LEVEL_THRESHOLDS_MINUTES.size) {
            LEVEL_THRESHOLDS_MINUTES[cappedLevel]
        } else {
            currentTierStart + 15000L
        }

        val range = (nextTierEnd - currentTierStart).coerceAtLeast(1L)
        val progressInTier = (totalMinutes - currentTierStart).coerceAtLeast(0L)
        val fraction = (progressInTier.toFloat() / range.toFloat()).coerceIn(0f, 1f)

        return UserLevelInfo(
            level = cappedLevel,
            levelTitle = title,
            currentXp = totalMinutes,
            nextLevelXp = nextTierEnd,
            progressFraction = fraction
        )
    }

    fun calculateProjections(sessions: List<Session>, totalPracticeSeconds: Long): ProjectionData {
        val totalHoursLogged = totalPracticeSeconds / 3600f

        // Calculate average daily practice over the last 14 days or available sessions
        val now = System.currentTimeMillis()
        val fourteenDaysAgo = now - 14L * 24 * 60 * 60 * 1000
        val recentSessions = sessions.filter { it.startTimeMillis >= fourteenDaysAgo && it.status == "COMPLETED" }

        val totalRecentSeconds = recentSessions.sumOf { it.durationSeconds }
        val daysSpan = 14f
        val dailyPaceMinutes = (totalRecentSeconds / 60f) / daysSpan
        val safeDailyMinutes = if (dailyPaceMinutes < 5f) 30f else dailyPaceMinutes // assume 30m if just started
        val weeklyPaceHours = (safeDailyMinutes * 7f) / 60f

        // Goal projections
        val hoursTo50 = max(0f, 50f - totalHoursLogged)
        val weeksTo50 = if (weeklyPaceHours > 0) hoursTo50 / weeklyPaceHours else 0f

        val hoursTo100 = max(0f, 100f - totalHoursLogged)
        val weeksTo100 = if (weeklyPaceHours > 0) hoursTo100 / weeklyPaceHours else 0f

        val hoursTo1000 = max(0f, 1000f - totalHoursLogged)
        val monthsTo1000 = if (weeklyPaceHours > 0) (hoursTo1000 / (weeklyPaceHours * 4.33f)) else 0f

        val projectedLevelIn30Days = calculateUserLevel(
            ((totalHoursLogged + (safeDailyMinutes * 30f / 60f)) * 60).toLong()
        ).level

        return ProjectionData(
            dailyPaceMinutes = safeDailyMinutes,
            weeklyPaceHours = weeklyPaceHours,
            hoursTo50Goal = hoursTo50,
            weeksTo50Goal = ceil(weeksTo50),
            hoursTo100Goal = hoursTo100,
            weeksTo100Goal = ceil(weeksTo100),
            hoursTo1000Goal = hoursTo1000,
            monthsTo1000Goal = ceil(monthsTo1000),
            totalHoursLogged = totalHoursLogged,
            projectedLevelIn30Days = projectedLevelIn30Days
        )
    }

    fun evaluateBadges(
        user: User,
        completedSessions: List<Session>,
        totalShakes: Int
    ): List<Badge> {
        val totalHours = (user.totalPracticeMinutes / 60).toInt()
        val sessionCount = completedSessions.size
        val streak = user.currentStreak
        val longestStreak = user.longestStreak

        return listOf(
            Badge(
                id = "streak_1",
                title = "Ignition",
                description = "Start your coding habit with your first practice day",
                iconName = "LocalFireDepartment",
                requiredValue = 1,
                currentValue = streak,
                isUnlocked = streak >= 1 || sessionCount >= 1,
                category = "STREAK"
            ),
            Badge(
                id = "streak_3",
                title = "Hat-Trick Flame",
                description = "Maintain a 3-day coding streak without breaking",
                iconName = "Whatshot",
                requiredValue = 3,
                currentValue = max(streak, longestStreak),
                isUnlocked = max(streak, longestStreak) >= 3,
                category = "STREAK"
            ),
            Badge(
                id = "streak_7",
                title = "Weekly Legend",
                description = "Achieve a full 7-day uninterrupted coding streak",
                iconName = "Stars",
                requiredValue = 7,
                currentValue = max(streak, longestStreak),
                isUnlocked = max(streak, longestStreak) >= 7,
                category = "STREAK"
            ),
            Badge(
                id = "streak_30",
                title = "Iron Devotion",
                description = "Reach a legendary 30-day coding streak",
                iconName = "WorkspacePremium",
                requiredValue = 30,
                currentValue = max(streak, longestStreak),
                isUnlocked = max(streak, longestStreak) >= 30,
                category = "STREAK"
            ),
            Badge(
                id = "hours_10",
                title = "10 Hours Deep",
                description = "Log 10 total hours of deliberate practice",
                iconName = "Timer",
                requiredValue = 10,
                currentValue = totalHours,
                isUnlocked = totalHours >= 10,
                category = "HOURS"
            ),
            Badge(
                id = "hours_50",
                title = "Half-Century Club",
                description = "Log 50 total hours of software crafting",
                iconName = "MilitaryTech",
                requiredValue = 50,
                currentValue = totalHours,
                isUnlocked = totalHours >= 50,
                category = "HOURS"
            ),
            Badge(
                id = "hours_100",
                title = "Centurion Coder",
                description = "Master 100 hours of focused coding sessions",
                iconName = "Diamond",
                requiredValue = 100,
                currentValue = totalHours,
                isUnlocked = totalHours >= 100,
                category = "HOURS"
            ),
            Badge(
                id = "shakes_5",
                title = "Shake Starter",
                description = "Use the shake gesture 5 times to log milestones",
                iconName = "Vibration",
                requiredValue = 5,
                currentValue = totalShakes,
                isUnlocked = totalShakes >= 5,
                category = "SHAKES"
            ),
            Badge(
                id = "shakes_25",
                title = "Kinetic Dev",
                description = "Shake 25 times to mark coding breakthroughs",
                iconName = "Bolt",
                requiredValue = 25,
                currentValue = totalShakes,
                isUnlocked = totalShakes >= 25,
                category = "SHAKES"
            ),
            Badge(
                id = "sessions_20",
                title = "Consistent Builder",
                description = "Complete 20 focused coding sessions",
                iconName = "Code",
                requiredValue = 20,
                currentValue = sessionCount,
                isUnlocked = sessionCount >= 20,
                category = "SESSIONS"
            )
        )
    }

    fun calculateUpdatedStreak(user: User, newSessionEndTimeMillis: Long): Pair<Int, Int> {
        val lastMillis = user.lastSessionDateMillis
        if (lastMillis == 0L) {
            return Pair(1, max(1, user.longestStreak))
        }

        val lastCal = Calendar.getInstance().apply { timeInMillis = lastMillis }
        val nowCal = Calendar.getInstance().apply { timeInMillis = newSessionEndTimeMillis }

        val lastDayOfYear = lastCal.get(Calendar.DAY_OF_YEAR)
        val lastYear = lastCal.get(Calendar.YEAR)
        val nowDayOfYear = nowCal.get(Calendar.DAY_OF_YEAR)
        val nowYear = nowCal.get(Calendar.YEAR)

        if (nowYear == lastYear && nowDayOfYear == lastDayOfYear) {
            // Same day, streak unchanged
            return Pair(user.currentStreak, user.longestStreak)
        }

        val dayDiff = ((newSessionEndTimeMillis - lastMillis) / (1000 * 60 * 60 * 24)).toInt()

        val newStreak = if (dayDiff <= 1 || (nowYear == lastYear && nowDayOfYear == lastDayOfYear + 1)) {
            user.currentStreak + 1
        } else {
            1 // Reset streak if more than 1 day missed
        }

        val newLongest = max(newStreak, user.longestStreak)
        return Pair(newStreak, newLongest)
    }
}
