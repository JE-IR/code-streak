package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseProvider
import com.example.data.model.ProgressLog
import com.example.data.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val allSessions: List<Session> = emptyList(),
    val filteredSessions: List<Session> = emptyList(),
    val selectedCategory: String = "ALL",
    val searchQuery: String = "",
    val expandedSessionId: Long? = null,
    val sessionLogsMap: Map<Long, List<ProgressLog>> = emptyMap(),
    val editingSession: Session? = null,
    val isEditDialogOpen: Boolean = false,
    val sessionToDelete: Session? = null,
    val isDeleteDialogOpen: Boolean = false,
    val isDeletedToastMessage: String? = null,
    val themeMode: String = "SYSTEM"
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dbProvider = DatabaseProvider(application)
    private val sessionDao = dbProvider.sessionDao
    private val progressLogDao = dbProvider.progressLogDao
    private val preferences = dbProvider.preferences

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        _uiState.value = _uiState.value.copy(themeMode = preferences.themeMode)
        viewModelScope.launch {
            val userId = if (preferences.currentUserId > 0) preferences.currentUserId else 1L
            sessionDao.getAllSessionsForUserFlow(userId).collect { list ->
                _uiState.value = _uiState.value.copy(allSessions = list)
                applyFilter()
            }
        }
    }

    fun setCategoryFilter(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilter()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilter()
    }

    private fun applyFilter() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val category = _uiState.value.selectedCategory

        val filtered = _uiState.value.allSessions.filter { session ->
            val matchCategory = (category == "ALL" || session.category.equals(category, ignoreCase = true))
            val matchQuery = query.isBlank() ||
                    session.title.lowercase().contains(query) ||
                    session.notes.lowercase().contains(query) ||
                    session.category.lowercase().contains(query)
            matchCategory && matchQuery
        }

        _uiState.value = _uiState.value.copy(filteredSessions = filtered)
    }

    fun toggleExpandSession(sessionId: Long) {
        if (_uiState.value.expandedSessionId == sessionId) {
            _uiState.value = _uiState.value.copy(expandedSessionId = null)
        } else {
            _uiState.value = _uiState.value.copy(expandedSessionId = sessionId)
            fetchLogsForSession(sessionId)
        }
    }

    private fun fetchLogsForSession(sessionId: Long) {
        viewModelScope.launch {
            val logs = progressLogDao.getLogsForSession(sessionId)
            val updatedMap = _uiState.value.sessionLogsMap.toMutableMap()
            updatedMap[sessionId] = logs
            _uiState.value = _uiState.value.copy(sessionLogsMap = updatedMap)
        }
    }

    fun openEditDialog(session: Session) {
        _uiState.value = _uiState.value.copy(
            editingSession = session,
            isEditDialogOpen = true
        )
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(
            editingSession = null,
            isEditDialogOpen = false
        )
    }

    fun updateSession(title: String, category: String, notes: String) {
        val current = _uiState.value.editingSession ?: return
        viewModelScope.launch {
            val updated = current.copy(
                title = title.ifBlank { current.title },
                category = category,
                notes = notes
            )
            sessionDao.updateSession(updated)
            dismissEditDialog()
        }
    }

    fun promptDeleteSession(session: Session) {
        _uiState.value = _uiState.value.copy(
            sessionToDelete = session,
            isDeleteDialogOpen = true
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            sessionToDelete = null,
            isDeleteDialogOpen = false
        )
    }

    fun confirmDeleteSession() {
        val session = _uiState.value.sessionToDelete ?: return
        viewModelScope.launch {
            progressLogDao.deleteLogsForSession(session.id)
            sessionDao.deleteSession(session)
            _uiState.value = _uiState.value.copy(
                sessionToDelete = null,
                isDeleteDialogOpen = false,
                isDeletedToastMessage = "Session deleted successfully"
            )
        }
    }
}
