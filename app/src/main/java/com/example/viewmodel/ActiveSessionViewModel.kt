package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.model.MotivationalQuote
import com.example.data.model.ProgressLog
import com.example.data.model.Session
import com.example.data.model.User
import com.example.data.remote.QuoteRepository
import com.example.util.StatsCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveSessionUiState(
    val sessionTitle: String = "Algorithm Problem Solving",
    val sessionCategory: String = "Algorithms / LeetCode",
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val shakeCount: Int = 0,
    val logs: List<ProgressLog> = emptyList(),
    val currentQuote: MotivationalQuote = MotivationalQuote("Talk is cheap. Show me the code.", "Linus Torvalds"),
    val recentShakeFeedback: String? = null,
    val isShakeCelebrationVisible: Boolean = false,
    val isFinishDialogOpen: Boolean = false,
    val sessionNotes: String = "",
    val user: User? = null,
    val isSessionSaved: Boolean = false,
    val savedSessionId: Long? = null,
    val themeMode: String = "SYSTEM"
)

class ActiveSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val dbProvider = DatabaseProvider(application)
    private val userDao = dbProvider.userDao
    private val sessionDao = dbProvider.sessionDao
    private val progressLogDao = dbProvider.progressLogDao
    private val preferences = dbProvider.preferences
    private val quoteRepo = QuoteRepository()

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val user = dbProvider.initializeDefaultDataIfNeeded()
            _uiState.value = _uiState.value.copy(
                user = user,
                currentQuote = quoteRepo.getRandomFallbackQuote(),
                themeMode = preferences.themeMode
            )
        }
    }

    fun configureSession(title: String, category: String) {
        _uiState.value = _uiState.value.copy(
            sessionTitle = title.ifBlank { "Coding Practice" },
            sessionCategory = category
        )
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        _uiState.value = _uiState.value.copy(
            isRunning = true,
            isPaused = false,
            startTimeMillis = if (_uiState.value.elapsedSeconds == 0L) System.currentTimeMillis() else _uiState.value.startTimeMillis
        )
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.isRunning && !_uiState.value.isPaused) {
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = _uiState.value.elapsedSeconds + 1
                    )
                }
            }
        }
    }

    fun pauseTimer() {
        _uiState.value = _uiState.value.copy(
            isRunning = false,
            isPaused = true
        )
    }

    fun resumeTimer() {
        startTimer()
    }

    fun onShakeDetected() {
        recordProgressLog(
            logType = "SHAKE_GESTURE",
            defaultNote = "Shake Gesture Triggered: Milestone Logged"
        )
    }

    fun logManualMilestone(type: String, label: String) {
        recordProgressLog(
            logType = type,
            defaultNote = label
        )
    }

    private fun recordProgressLog(logType: String, defaultNote: String) {
        val currentShakeCount = _uiState.value.shakeCount + 1
        val quote = quoteRepo.getRandomFallbackQuote()

        val newLog = ProgressLog(
            sessionId = 0, // Assigned upon save
            userId = _uiState.value.user?.id ?: 1L,
            logType = logType,
            note = defaultNote,
            timestampMillis = System.currentTimeMillis(),
            quoteSnippet = quote.text
        )

        _uiState.value = _uiState.value.copy(
            shakeCount = currentShakeCount,
            logs = _uiState.value.logs + newLog,
            currentQuote = quote,
            recentShakeFeedback = "Milestone #${currentShakeCount} Registered!",
            isShakeCelebrationVisible = true
        )

        // Clear celebratory feedback after 3 seconds
        viewModelScope.launch {
            delay(3000)
            _uiState.value = _uiState.value.copy(isShakeCelebrationVisible = false)
        }
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(sessionNotes = notes)
    }

    fun openFinishDialog() {
        pauseTimer()
        _uiState.value = _uiState.value.copy(isFinishDialogOpen = true)
    }

    fun dismissFinishDialog() {
        _uiState.value = _uiState.value.copy(isFinishDialogOpen = false)
    }

    fun saveAndFinishSession(onFinished: (sessionId: Long) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val user = state.user ?: userDao.getFirstUser() ?: return@launch
            val now = System.currentTimeMillis()

            val session = Session(
                userId = user.id,
                title = state.sessionTitle,
                category = state.sessionCategory,
                startTimeMillis = state.startTimeMillis,
                endTimeMillis = now,
                durationSeconds = state.elapsedSeconds.coerceAtLeast(1L),
                status = "COMPLETED",
                notes = state.sessionNotes.trim(),
                shakeCount = state.shakeCount,
                quoteSnippet = state.currentQuote.text
            )

            val sessionId = sessionDao.insertSession(session)

            // Save all progress logs with the generated sessionId
            val logsToSave = state.logs.map { it.copy(sessionId = sessionId, userId = user.id) }
            if (logsToSave.isNotEmpty()) {
                progressLogDao.insertLogs(logsToSave)
            }

            // Update user streak and stats
            val (updatedStreak, updatedLongest) = StatsCalculator.calculateUpdatedStreak(user, now)
            val addedMinutes = (state.elapsedSeconds / 60).coerceAtLeast(1L)
            val newTotalMinutes = user.totalPracticeMinutes + addedMinutes
            val newLevel = StatsCalculator.calculateUserLevel(newTotalMinutes).level

            userDao.updateStats(
                userId = user.id,
                streak = updatedStreak,
                longest = updatedLongest,
                totalMinutes = newTotalMinutes,
                level = newLevel,
                lastDate = now
            )

            _uiState.value = _uiState.value.copy(
                isFinishDialogOpen = false,
                isSessionSaved = true,
                savedSessionId = sessionId
            )

            onFinished(sessionId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
