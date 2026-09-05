package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.model.Badge
import com.example.data.model.ProjectionData
import com.example.data.model.Session
import com.example.data.model.User
import com.example.data.model.UserLevelInfo
import com.example.util.StatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsightsUiState(
    val user: User? = null,
    val levelInfo: UserLevelInfo = UserLevelInfo(1, "Novice Coder", 0, 300, 0f),
    val projections: ProjectionData = ProjectionData(30f, 3.5f, 50f, 15f, 100f, 29f, 1000f, 66f, 0f, 2),
    val badges: List<Badge> = emptyList(),
    val categoryDistribution: Map<String, Float> = emptyMap(),
    val totalPracticeSeconds: Long = 0,
    val totalShakesCount: Int = 0,
    val totalSessionsCount: Int = 0,
    val themeMode: String = "SYSTEM"
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val dbProvider = DatabaseProvider(application)
    private val userDao = dbProvider.userDao
    private val sessionDao = dbProvider.sessionDao
    private val preferences = dbProvider.preferences

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            val user = dbProvider.initializeDefaultDataIfNeeded()
            val userId = user.id

            userDao.getUserFlow(userId).collect { updatedUser ->
                val currentUser = updatedUser ?: user
                val level = StatsCalculator.calculateUserLevel(currentUser.totalPracticeMinutes)

                val sessions = sessionDao.getAllSessionsForUser(userId)
                val totalSeconds = sessions.sumOf { it.durationSeconds }
                val totalShakes = sessions.sumOf { it.shakeCount }

                val projections = StatsCalculator.calculateProjections(sessions, totalSeconds)
                val badges = StatsCalculator.evaluateBadges(currentUser, sessions, totalShakes)

                // Category distribution
                val catMap = mutableMapOf<String, Float>()
                val completed = sessions.filter { it.status == "COMPLETED" }
                val totalDuration = completed.sumOf { it.durationSeconds }.toFloat().coerceAtLeast(1f)
                completed.groupBy { it.category }.forEach { (category, list) ->
                    val catDuration = list.sumOf { it.durationSeconds }.toFloat()
                    catMap[category] = (catDuration / totalDuration) * 100f
                }

                _uiState.value = _uiState.value.copy(
                    user = currentUser,
                    levelInfo = level,
                    projections = projections,
                    badges = badges,
                    categoryDistribution = catMap,
                    totalPracticeSeconds = totalSeconds,
                    totalShakesCount = totalShakes,
                    totalSessionsCount = completed.size,
                    themeMode = preferences.themeMode
                )
            }
        }
    }
}
