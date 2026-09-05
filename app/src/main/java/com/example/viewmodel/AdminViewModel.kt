package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val currentAdminUser: User? = null,
    val totalUsers: Int = 0,
    val totalSessions: Int = 0,
    val totalHoursLogged: Float = 0f,
    val totalMilestonesShaken: Int = 0,
    val usersList: List<User> = emptyList(),
    val searchQuery: String = "",
    val filterRole: String = "ALL", // "ALL", "USERS", "ADMINS"
    val themeMode: String = "SYSTEM",
    val isLoggedOut: Boolean = false,
    val message: String? = null
) {
    val filteredUsers: List<User>
        get() = usersList.filter { user ->
            val matchesQuery = searchQuery.isBlank() ||
                    user.username.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true)
            val matchesRole = when (filterRole) {
                "ADMINS" -> user.isAdmin
                "USERS" -> !user.isAdmin
                else -> true
            }
            matchesQuery && matchesRole
        }
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val dbProvider = DatabaseProvider(application)
    private val userDao = dbProvider.userDao
    private val sessionDao = dbProvider.sessionDao
    private val logDao = dbProvider.progressLogDao
    private val preferences = dbProvider.preferences

    private val _uiState = MutableStateFlow(
        AdminUiState(themeMode = preferences.themeMode)
    )
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAdminDashboard()
    }

    fun loadAdminDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentAdmin = if (preferences.currentUserId > 0) {
                    userDao.getUserById(preferences.currentUserId)
                } else null

                val users = userDao.getAllUsers()
                val userCount = userDao.getUserCount()
                val sessionCount = sessionDao.getTotalGlobalSessionsCount()
                val totalSeconds = sessionDao.getTotalGlobalDurationSeconds()
                val totalShakes = sessionDao.getTotalGlobalShakesCount()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentAdminUser = currentAdmin,
                    totalUsers = userCount,
                    totalSessions = sessionCount,
                    totalHoursLogged = totalSeconds / 3600f,
                    totalMilestonesShaken = totalShakes,
                    usersList = users,
                    themeMode = preferences.themeMode
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to load admin metrics: ${e.localizedMessage}"
                )
            }
        }
    }

    fun setThemeMode(mode: String) {
        preferences.themeMode = mode
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setFilterRole(role: String) {
        _uiState.value = _uiState.value.copy(filterRole = role)
    }

    fun deleteUserAccount(user: User) {
        viewModelScope.launch {
            try {
                if (user.id == preferences.currentUserId) {
                    _uiState.value = _uiState.value.copy(message = "Cannot delete currently logged-in administrator account.")
                    return@launch
                }
                logDao.deleteLogsForUser(user.id)
                sessionDao.deleteAllSessionsForUser(user.id)
                userDao.deleteUser(user.id)
                loadAdminDashboard()
                _uiState.value = _uiState.value.copy(message = "Account '${user.username}' deleted successfully.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Error deleting user: ${e.localizedMessage}")
            }
        }
    }

    fun resetUserStats(user: User) {
        viewModelScope.launch {
            try {
                logDao.deleteLogsForUser(user.id)
                sessionDao.deleteAllSessionsForUser(user.id)
                userDao.updateStats(
                    userId = user.id,
                    streak = 0,
                    longest = 0,
                    totalMinutes = 0,
                    level = 1,
                    lastDate = 0
                )
                loadAdminDashboard()
                _uiState.value = _uiState.value.copy(message = "Reset stats and logs for '${user.username}'.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Error resetting stats: ${e.localizedMessage}")
            }
        }
    }

    fun seedDemoUser(name: String, email: String) {
        viewModelScope.launch {
            try {
                val existing = userDao.getUserByEmail(email)
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(message = "User with email '$email' already exists.")
                    return@launch
                }
                val newUser = User(
                    username = name.ifBlank { "Test Learner" },
                    email = email.ifBlank { "learner_${System.currentTimeMillis() % 1000}@dev.io" },
                    passwordHash = "streak123",
                    isAdmin = false,
                    dailyGoalMinutes = 45,
                    weeklyGoalMinutes = 300,
                    currentStreak = (1..10).random(),
                    longestStreak = (10..25).random(),
                    totalPracticeMinutes = (120L..3600L).random(),
                    level = (1..4).random(),
                    lastSessionDateMillis = System.currentTimeMillis()
                )
                userDao.insertUser(newUser)
                loadAdminDashboard()
                _uiState.value = _uiState.value.copy(message = "Created demo learner '${newUser.username}'.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Failed to create demo user: ${e.localizedMessage}")
            }
        }
    }

    fun purgeAllSessions() {
        viewModelScope.launch {
            try {
                logDao.deleteAllLogs()
                sessionDao.deleteAllSessions()
                loadAdminDashboard()
                _uiState.value = _uiState.value.copy(message = "All practice sessions and gesture logs purged.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Failed to purge sessions: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        preferences.clearSession()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

