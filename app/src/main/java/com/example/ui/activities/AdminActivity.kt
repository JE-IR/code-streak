package com.example.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesManager
import com.example.data.model.User
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DevCyan
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakAmberLight
import com.example.ui.theme.StreakFlame
import com.example.util.ShareUtil
import com.example.viewmodel.AdminUiState
import com.example.viewmodel.AdminViewModel

class AdminActivity : ComponentActivity() {

    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(this)
        if (!prefs.isAdmin) {
            Toast.makeText(this, "Access Denied: Administrator role required.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            val uiState by adminViewModel.uiState.collectAsState()

            CodeStreakTheme(themeMode = uiState.themeMode) {
                LaunchedEffect(uiState.message) {
                    uiState.message?.let {
                        Toast.makeText(this@AdminActivity, it, Toast.LENGTH_SHORT).show()
                        adminViewModel.clearMessage()
                    }
                }

                LaunchedEffect(uiState.isLoggedOut) {
                    if (uiState.isLoggedOut) {
                        Toast.makeText(this@AdminActivity, "Admin logged out.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@AdminActivity, AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                }

                AdminDashboardScreen(
                    uiState = uiState,
                    onRefresh = { adminViewModel.loadAdminDashboard() },
                    onDeleteUser = { adminViewModel.deleteUserAccount(it) },
                    onResetUserStats = { adminViewModel.resetUserStats(it) },
                    onSearchChange = { adminViewModel.setSearchQuery(it) },
                    onFilterChange = { adminViewModel.setFilterRole(it) },
                    onThemeChange = { adminViewModel.setThemeMode(it) },
                    onSeedDemoUser = { name, email -> adminViewModel.seedDemoUser(name, email) },
                    onPurgeSessions = { adminViewModel.purgeAllSessions() },
                    onContactSupport = {
                        val admin = uiState.currentAdminUser
                        ShareUtil.sendSupportEmail(
                            context = this@AdminActivity,
                            userEmail = admin?.email ?: "admin@codestreak.dev",
                            username = admin?.username ?: "Admin"
                        )
                    },
                    onLogout = { adminViewModel.logout() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    uiState: AdminUiState,
    onRefresh: () -> Unit,
    onDeleteUser: (User) -> Unit,
    onResetUserStats: (User) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onSeedDemoUser: (String, String) -> Unit,
    onPurgeSessions: () -> Unit,
    onContactSupport: () -> Unit,
    onLogout: () -> Unit
) {
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var userToReset by remember { mutableStateOf<User?>(null) }
    var isSeedDialogOpen by remember { mutableStateOf(false) }
    var isPurgeDialogOpen by remember { mutableStateOf(false) }
    var isThemeDialogOpen by remember { mutableStateOf(false) }

    var seedName by remember { mutableStateOf("") }
    var seedEmail by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(StreakFlame.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = StreakFlame,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Console",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Database & User Operations",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isThemeDialogOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = "Change Theme",
                            tint = StreakAmber
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out",
                            tint = DangerRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.usersList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StreakFlame)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Admin Banner Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = StreakFlame,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Administrator Session",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "LIVE SQLite DB",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Logged in as: ${uiState.currentAdminUser?.username ?: "System Administrator"} (${uiState.currentAdminUser?.email ?: "admin@codestreak.dev"})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        seedName = ""
                                        seedEmail = ""
                                        isSeedDialogOpen = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Learner", fontSize = 12.sp, color = Color.White)
                                }

                                Button(
                                    onClick = { isPurgeDialogOpen = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Purge Sessions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                item {
                    // System Aggregate Metrics 2x2 Grid
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = StreakFlame,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "System Aggregate Metrics",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AdminMetricBox(
                                    title = "Total Users",
                                    value = "${uiState.totalUsers}",
                                    icon = Icons.Default.Group,
                                    tint = DevCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricBox(
                                    title = "Sessions",
                                    value = "${uiState.totalSessions}",
                                    icon = Icons.Default.Timer,
                                    tint = StreakAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AdminMetricBox(
                                    title = "System Hours",
                                    value = String.format("%.1f h", uiState.totalHoursLogged),
                                    icon = Icons.Default.HourglassBottom,
                                    tint = StreakFlame,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricBox(
                                    title = "Sensor Shakes",
                                    value = "${uiState.totalMilestonesShaken}",
                                    icon = Icons.Default.Vibration,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    // Contact Support Tester
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContactSupport() },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = DevCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Test Support Email Intent", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Launches external email client (support@codestreak.dev)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                item {
                    // Directory Header & Search Bar
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "Database Accounts Directory (${uiState.filteredUsers.size} of ${uiState.usersList.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("Search by name or email...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = StreakFlame,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("ALL" to "All Accounts", "USERS" to "Learners Only", "ADMINS" to "Admins Only").forEach { (roleKey, label) ->
                                FilterChip(
                                    selected = uiState.filterRole == roleKey,
                                    onClick = { onFilterChange(roleKey) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StreakFlame,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, if (uiState.filterRole == roleKey) StreakFlame else MaterialTheme.colorScheme.outline)
                                )
                            }
                        }
                    }
                }

                if (uiState.filteredUsers.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No database accounts match the current filter.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(uiState.filteredUsers, key = { it.id }) { user ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (user.isAdmin) Brush.linearGradient(listOf(StreakFlame, StreakAmber))
                                            else Brush.linearGradient(listOf(DevCyan, Color(0xFF0284C7)))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.username.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (user.isAdmin) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = StreakFlame.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, StreakFlame.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = "ADMIN",
                                                    color = StreakFlame,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = user.email,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Level ${user.level} • Streak: ${user.currentStreak}d • ${(user.totalPracticeMinutes / 60f).let { String.format("%.1fh", it) }}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StreakAmber
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = { userToReset = user }) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset Stats",
                                        tint = StreakAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (!user.isAdmin) {
                                    IconButton(onClick = { userToDelete = user }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Account",
                                            tint = DangerRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Theme Selection Dialog
    if (isThemeDialogOpen) {
        AlertDialog(
            onDismissRequest = { isThemeDialogOpen = false },
            title = { Text("Theme Appearance", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select your preferred interface appearance:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf("SYSTEM" to "System Default (Follow OS)", "DARK" to "Developer Dark (Charcoal & Amber)", "LIGHT" to "Clean Light (Slate & Flame)").forEach { (mode, title) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeChange(mode)
                                    isThemeDialogOpen = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.themeMode.equals(mode, ignoreCase = true)) StreakFlame.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (uiState.themeMode.equals(mode, ignoreCase = true)) StreakFlame else MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (uiState.themeMode.equals(mode, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.themeMode.equals(mode, ignoreCase = true)) StreakFlame else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isThemeDialogOpen = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Demo User Dialog
    if (isSeedDialogOpen) {
        AlertDialog(
            onDismissRequest = { isSeedDialogOpen = false },
            title = { Text("Add Learner Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Create a new test account with simulated learning history:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = seedName,
                        onValueChange = { seedName = it },
                        label = { Text("Learner Name") },
                        placeholder = { Text("e.g. Jordan Lee") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = seedEmail,
                        onValueChange = { seedEmail = it },
                        label = { Text("Learner Email") },
                        placeholder = { Text("e.g. jordan@dev.io") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSeedDemoUser(seedName, seedEmail)
                        isSeedDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame)
                ) {
                    Text("Create Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isSeedDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Purge Sessions Dialog
    if (isPurgeDialogOpen) {
        AlertDialog(
            onDismissRequest = { isPurgeDialogOpen = false },
            title = { Text("Purge Practice Sessions?", fontWeight = FontWeight.Bold) },
            text = { Text("This will delete all completed practice sessions and accelerometer logs across all database accounts. User profiles and accounts will remain intact.") },
            confirmButton = {
                Button(
                    onClick = {
                        onPurgeSessions()
                        isPurgeDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Purge Sessions", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isPurgeDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete User Confirmation Dialog
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete User Account?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete user '${user.username}' (${user.email})? All associated session logs will be deleted via cascade.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(user)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset User Confirmation Dialog
    userToReset?.let { user ->
        AlertDialog(
            onDismissRequest = { userToReset = null },
            title = { Text("Reset User Practice Stats?", fontWeight = FontWeight.Bold) },
            text = { Text("Reset streak, practice hours, and clear session logs for '${user.username}' back to initial zero state?") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetUserStats(user)
                        userToReset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame)
                ) {
                    Text("Reset Stats", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToReset = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminMetricBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

