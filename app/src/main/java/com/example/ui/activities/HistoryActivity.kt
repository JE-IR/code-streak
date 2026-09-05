package com.example.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesManager
import com.example.data.model.Session
import com.example.ui.components.CategoryBadgeChip
import com.example.ui.components.SessionItemRow
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.StreakFlame
import com.example.util.ShareUtil
import com.example.util.TimeFormatters
import com.example.viewmodel.HistoryUiState
import com.example.viewmodel.HistoryViewModel

class HistoryActivity : ComponentActivity() {

    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val highlightId = intent.getLongExtra("EXTRA_HIGHLIGHT_SESSION_ID", -1L)
        if (highlightId > 0) {
            historyViewModel.toggleExpandSession(highlightId)
        }

        val prefs = PreferencesManager(this)

        setContent {
            val uiState by historyViewModel.uiState.collectAsState()

            CodeStreakTheme(themeMode = uiState.themeMode) {
                LaunchedEffect(uiState.isDeletedToastMessage) {
                    uiState.isDeletedToastMessage?.let {
                        Toast.makeText(this@HistoryActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }

                HistoryScreen(
                    uiState = uiState,
                    onSearchChange = { historyViewModel.setSearchQuery(it) },
                    onCategorySelect = { historyViewModel.setCategoryFilter(it) },
                    onToggleExpand = { historyViewModel.toggleExpandSession(it) },
                    onEditPrompt = { historyViewModel.openEditDialog(it) },
                    onDismissEdit = { historyViewModel.dismissEditDialog() },
                    onConfirmEdit = { title, cat, notes -> historyViewModel.updateSession(title, cat, notes) },
                    onDeletePrompt = { historyViewModel.promptDeleteSession(it) },
                    onDismissDelete = { historyViewModel.dismissDeleteDialog() },
                    onConfirmDelete = { historyViewModel.confirmDeleteSession() },
                    onShareSession = { session ->
                        ShareUtil.shareSession(
                            context = this@HistoryActivity,
                            title = session.title,
                            category = session.category,
                            durationStr = TimeFormatters.formatSecondsToHoursAndMinutes(session.durationSeconds),
                            shakes = session.shakeCount
                        )
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        historyViewModel.loadSessions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onToggleExpand: (Long) -> Unit,
    onEditPrompt: (Session) -> Unit,
    onDismissEdit: () -> Unit,
    onConfirmEdit: (title: String, category: String, notes: String) -> Unit,
    onDeletePrompt: (Session) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onShareSession: (Session) -> Unit,
    onBack: () -> Unit
) {
    val categories = listOf("ALL", "Algorithms / LeetCode", "Android / Kotlin", "Web Dev", "System Design", "Open Source")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Session History & Logs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by topic, keyword, category...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("history_search_input")
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = uiState.selectedCategory.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StreakFlame.copy(alpha = 0.2f),
                            selectedLabelColor = StreakFlame
                        )
                    )
                }
            }

            // Results Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Found ${uiState.filteredSessions.size} sessions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sessions List
            if (uiState.filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching sessions found.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing filters or start a new practice session!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    items(uiState.filteredSessions, key = { it.id }) { session ->
                        SessionItemRow(
                            session = session,
                            isExpanded = uiState.expandedSessionId == session.id,
                            logs = uiState.sessionLogsMap[session.id],
                            onToggleExpand = { onToggleExpand(session.id) },
                            onEdit = { onEditPrompt(session) },
                            onDelete = { onDeletePrompt(session) },
                            onShare = { onShareSession(session) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    // Edit Session Dialog (CRUD: Update)
    if (uiState.isEditDialogOpen && uiState.editingSession != null) {
        val session = uiState.editingSession
        var title by remember(session) { mutableStateOf(session.title) }
        var category by remember(session) { mutableStateOf(session.category) }
        var notes by remember(session) { mutableStateOf(session.notes) }

        val editCategories = listOf("Algorithms / LeetCode", "Android / Kotlin", "Web Dev", "System Design", "Open Source")

        AlertDialog(
            onDismissRequest = onDismissEdit,
            title = { Text("Edit Practice Session", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Topic Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_session_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        editCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("edit_session_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmEdit(title, category, notes) },
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame),
                    modifier = Modifier.testTag("confirm_edit_session_button")
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEdit) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog (CRUD: Delete)
    if (uiState.isDeleteDialogOpen && uiState.sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Session?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete \"${uiState.sessionToDelete.title}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_dialog_button")
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text("Cancel")
                }
            }
        )
    }
}
