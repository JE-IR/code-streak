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

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val dbProvider = DatabaseProvider(application)
    private val userDao = dbProvider.userDao
    private val preferences = dbProvider.preferences

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter both email and password.")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = userDao.getUserByEmail(trimmedEmail)
                if (user != null && user.passwordHash == trimmedPassword) {
                    preferences.currentUserId = user.id
                    preferences.isLoggedIn = true
                    preferences.isAdmin = user.isAdmin
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Invalid credentials. Please check your email or password.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Login error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedUsername.length < 2) {
            _uiState.value = AuthUiState.Error("Username must be at least 2 characters.")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = AuthUiState.Error("Please provide a valid email.")
            return
        }

        if (trimmedPassword.length < 4) {
            _uiState.value = AuthUiState.Error("Password must be at least 4 characters.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val existing = userDao.getUserByEmail(trimmedEmail)
                if (existing != null) {
                    _uiState.value = AuthUiState.Error("An account with this email already exists.")
                    return@launch
                }

                val newUser = User(
                    username = trimmedUsername,
                    email = trimmedEmail,
                    passwordHash = trimmedPassword,
                    isAdmin = false,
                    dailyGoalMinutes = 45,
                    weeklyGoalMinutes = 300,
                    currentStreak = 1,
                    longestStreak = 1,
                    totalPracticeMinutes = 0,
                    level = 1,
                    lastSessionDateMillis = 0
                )
                val id = userDao.insertUser(newUser)
                val savedUser = newUser.copy(id = id)
                preferences.currentUserId = id
                preferences.isLoggedIn = true
                preferences.isAdmin = false
                _uiState.value = AuthUiState.Success(savedUser)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Registration failed: ${e.localizedMessage}")
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val user = dbProvider.initializeDefaultDataIfNeeded()
            preferences.isAdmin = user.isAdmin
            _uiState.value = AuthUiState.Success(user)
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
