package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.model.MotivationalQuote
import com.example.data.model.Session
import com.example.data.model.User
import com.example.data.model.UserLevelInfo
import com.example.data.remote.QuoteRepository
import com.example.util.StatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val user: User? = null,
    val levelInfo: UserLevelInfo = UserLevelInfo(1, "Novice Coder", 0, 300, 0f),
    val todayPracticeSeconds: Long = 0,
    val weekPracticeSeconds: Long = 0,
    val totalPracticeSeconds: Long = 0,
    val todayGoalMinutes: Int = 45,
    val weekGoalMinutes: Int = 300,
    val themeMode: String = "SYSTEM",
    val quote: MotivationalQuote = MotivationalQuote("Talk is cheap. Show me the code.", "Linus Torvalds"),
    val recentSessions: List<Session> = emptyList(),
    val totalShakesCount: Int = 0,
    val isLoadingQuote: Boolean = false
)

class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dbProvider = DatabaseProvider(application)
    private val userDao = dbProvider.userDao
    private val sessionDao = dbProvider.sessionDao
    private val preferences = dbProvider.preferences
    private val quoteRepo = QuoteRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        fetchQuote()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            val user = dbProvider.initializeDefaultDataIfNeeded()
            val userId = user.id

            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val startOfWeek = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Observe continuous updates
            launch {
                userDao.getUserFlow(userId).collect { updatedUser ->
                    val currentUser = updatedUser ?: user
                    val level = StatsCalculator.calculateUserLevel(currentUser.totalPracticeMinutes)
                    _uiState.value = _uiState.value.copy(
                        user = currentUser,
                        levelInfo = level,
                        todayGoalMinutes = currentUser.dailyGoalMinutes,
                        weekGoalMinutes = currentUser.weeklyGoalMinutes,
                        themeMode = preferences.themeMode
                    )
                }
            }

            launch {
                sessionDao.getTodayPracticeSecondsFlow(userId, startOfDay).collect { sec ->
                    _uiState.value = _uiState.value.copy(todayPracticeSeconds = sec)
                }
            }

            launch {
                sessionDao.getWeekPracticeSecondsFlow(userId, startOfWeek).collect { sec ->
                    _uiState.value = _uiState.value.copy(weekPracticeSeconds = sec)
                }
            }

            launch {
                sessionDao.getTotalPracticeSecondsFlow(userId).collect { sec ->
                    _uiState.value = _uiState.value.copy(totalPracticeSeconds = sec)
                }
            }

            launch {
                sessionDao.getAllSessionsForUserFlow(userId).collect { sessions ->
                    _uiState.value = _uiState.value.copy(recentSessions = sessions.take(5))
                }
            }

            launch {
                sessionDao.getTotalShakesCountFlow(userId).collect { shakes ->
                    _uiState.value = _uiState.value.copy(totalShakesCount = shakes)
                }
            }
        }
    }

    fun fetchQuote() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingQuote = true)
            try {
                val quote = quoteRepo.getMotivationalQuote()
                _uiState.value = _uiState.value.copy(quote = quote, isLoadingQuote = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    quote = quoteRepo.getRandomFallbackQuote(),
                    isLoadingQuote = false
                )
            }
        }
    }
}
