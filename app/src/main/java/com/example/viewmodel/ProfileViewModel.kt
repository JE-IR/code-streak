package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.model.User
import com.example.data.model.UserLevelInfo
import com.example.util.StatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val levelInfo: UserLevelInfo = UserLevelInfo(1, "Novice Coder", 0, 300, 0f),
    val soundEnabled: Boolean = true,
    val shakeSensitivity: String = "MEDIUM",
    val themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    val isAdmin: Boolean = false,
    val isEditingGoals: Boolean = false,
    val newDailyGoal: Int = 45,
    val newWeeklyGoal: Int = 300,
    val totalPracticeHours: Float = 0f,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val dbProvider = DatabaseProvider(application)
    private val userDao = dbProvider.userDao
    private val preferences = dbProvider.preferences

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val user = dbProvider.initializeDefaultDataIfNeeded()
            val userId = user.id

            userDao.getUserFlow(userId).collect { updatedUser ->
                val currentUser = updatedUser ?: user
                val level = StatsCalculator.calculateUserLevel(currentUser.totalPracticeMinutes)
                val hours = currentUser.totalPracticeMinutes / 60f

                _uiState.value = _uiState.value.copy(
                    user = currentUser,
                    levelInfo = level,
                    soundEnabled = preferences.soundEnabled,
                    shakeSensitivity = preferences.shakeSensitivity,
                    themeMode = preferences.themeMode,
                    isAdmin = currentUser.isAdmin,
                    newDailyGoal = currentUser.dailyGoalMinutes,
                    newWeeklyGoal = currentUser.weeklyGoalMinutes,
                    totalPracticeHours = hours
                )
            }
        }
    }

    fun setThemeMode(mode: String) {
        preferences.themeMode = mode
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun setEditingGoals(editing: Boolean) {
        _uiState.value = _uiState.value.copy(
            isEditingGoals = editing,
            newDailyGoal = _uiState.value.user?.dailyGoalMinutes ?: 45,
            newWeeklyGoal = _uiState.value.user?.weeklyGoalMinutes ?: 300
        )
    }

    fun updateGoals(daily: Int, weekly: Int) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            userDao.updateGoals(user.id, daily, weekly)
            _uiState.value = _uiState.value.copy(isEditingGoals = false)
        }
    }

    fun setSensitivity(sensitivity: String) {
        preferences.shakeSensitivity = sensitivity
        _uiState.value = _uiState.value.copy(shakeSensitivity = sensitivity)
    }

    fun toggleSound(enabled: Boolean) {
        preferences.soundEnabled = enabled
        _uiState.value = _uiState.value.copy(soundEnabled = enabled)
    }

    fun logout() {
        preferences.clearSession()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }
}
