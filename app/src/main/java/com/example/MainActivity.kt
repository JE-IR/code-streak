package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesManager
import com.example.data.model.Session
import com.example.ui.activities.ActiveSessionActivity
import com.example.ui.activities.AdminActivity
import com.example.ui.activities.HistoryActivity
import com.example.ui.activities.InsightsActivity
import com.example.ui.activities.ProfileActivity
import com.example.ui.components.CategoryBadgeChip
import com.example.ui.components.QuoteBannerCard
import com.example.ui.components.SessionItemRow
import com.example.ui.components.StatMetricCards
import com.example.ui.components.StreakHeaderCard
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakFlame
import com.example.util.ShareUtil
import com.example.util.TimeFormatters
import com.example.viewmodel.DashboardUiState
import com.example.viewmodel.MainDashboardViewModel

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: MainDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(this)
        if (prefs.isAdmin) {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
            return
        }

        setContent {
            val uiState by dashboardViewModel.uiState.collectAsState()

            CodeStreakTheme(themeMode = uiState.themeMode) {
                DashboardScreen(
                    uiState = uiState,
                    onRefreshQuote = { dashboardViewModel.fetchQuote() },
                    onStartSession = { title, category ->
                        val intent = Intent(this@MainActivity, ActiveSessionActivity::class.java).apply {
                            putExtra("EXTRA_TITLE", title)
                            putExtra("EXTRA_CATEGORY", category)
                        }
                        startActivity(intent)
                    },
                    onNavigateHistory = {
                        val intent = Intent(this@MainActivity, HistoryActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateInsights = {
                        val intent = Intent(this@MainActivity, InsightsActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateProfile = {
                        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    },
                    onShareStreak = {
                        val user = uiState.user
                        if (user != null) {
                            val totalHours = uiState.totalPracticeSeconds / 3600f
                            ShareUtil.shareStreak(
                                context = this@MainActivity,
                                username = user.username,
                                streakDays = user.currentStreak,
                                totalHours = totalHours,
                                levelTitle = uiState.levelInfo.levelTitle
                            )
                        }
                    },
                    onShareSession = { session ->
                        val durationStr = TimeFormatters.formatSecondsToHoursAndMinutes(session.durationSeconds)
                        ShareUtil.shareSession(
                            context = this@MainActivity,
                            title = session.title,
                            category = session.category,
                            durationStr = durationStr,
                            shakes = session.shakeCount
                        )
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferencesManager(this)
        if (prefs.isAdmin) {
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
            return
        }
        dashboardViewModel.loadDashboardData()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onRefreshQuote: () -> Unit,
    onStartSession: (String, String) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateInsights: () -> Unit,
    onNavigateProfile: () -> Unit,
    onShareStreak: () -> Unit,
    onShareSession: (Session) -> Unit
) {
    var isNewSessionDialogOpen by remember { mutableStateOf(false) }
    var sessionTitleInput by remember { mutableStateOf("Algorithm Problem Solving") }
    var selectedCategory by remember { mutableStateOf("Algorithms / LeetCode") }

    val categories = listOf(
        "Algorithms / LeetCode",
        "Android / Kotlin",
        "Web Dev",
        "System Design",
        "Open Source"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CodeStreak",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "READY TO PRACTICE?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Header Avatar Circle
                        val initials = (uiState.user?.username?.take(2) ?: "DEV").uppercase()
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StreakFlame.copy(alpha = 0.15f))
                                .border(1.5.dp, StreakFlame, CircleShape)
                                .shadow(2.dp, CircleShape)
                                .clickable { onNavigateProfile() }
                                .testTag("profile_avatar_top"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StreakFlame
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onShareStreak,
                        modifier = Modifier.testTag("share_streak_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Streak",
                            tint = StreakFlame
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isNewSessionDialogOpen = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Start Practice",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                containerColor = StreakFlame,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .testTag("start_practice_fab")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on dashboard */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StreakFlame,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = StreakFlame.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_history")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateInsights,
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Insights") },
                    label = { Text("Insights") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_insights")
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // Streak Hero Card
                StreakHeaderCard(
                    currentStreak = uiState.user?.currentStreak ?: 1,
                    longestStreak = uiState.user?.longestStreak ?: 1,
                    levelInfo = uiState.levelInfo
                )
            }

            item {
                // Goals & Total Stat Metrics
                StatMetricCards(
                    todaySeconds = uiState.todayPracticeSeconds,
                    todayGoalMinutes = uiState.todayGoalMinutes,
                    weekSeconds = uiState.weekPracticeSeconds,
                    weekGoalMinutes = uiState.weekGoalMinutes,
                    totalSeconds = uiState.totalPracticeSeconds,
                    totalShakes = uiState.totalShakesCount
                )
            }

            item {
                // Quick Start Action Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { isNewSessionDialogOpen = true }
                        .testTag("quick_start_card"),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(StreakFlame),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Start Practice Session",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Shake device to log milestones",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Launch",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                // Wisdom Quote Banner
                QuoteBannerCard(
                    quote = uiState.quote,
                    isLoading = uiState.isLoadingQuote,
                    onRefresh = onRefreshQuote
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Sessions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateHistory) {
                        Text(
                            text = "View All (${uiState.recentSessions.size})",
                            color = StreakFlame,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (uiState.recentSessions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No sessions logged yet.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start your first coding practice session above!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentSessions) { session ->
                    SessionItemRow(
                        session = session,
                        isExpanded = false,
                        logs = null,
                        onToggleExpand = onNavigateHistory,
                        onEdit = onNavigateHistory,
                        onDelete = onNavigateHistory,
                        onShare = { onShareSession(session) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // New Session Dialog
    if (isNewSessionDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewSessionDialogOpen = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Start Practice Session",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sessionTitleInput,
                        onValueChange = { sessionTitleInput = it },
                        label = { Text("Session Topic / Focus") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("session_topic_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedCategory == cat) StreakFlame.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadgeChip(category = cat)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (selectedCategory == cat) "✓ Selected" else "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StreakFlame
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isNewSessionDialogOpen = false
                        onStartSession(sessionTitleInput, selectedCategory)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("confirm_start_session_button")
                ) {
                    Text("Start Timer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewSessionDialogOpen = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
