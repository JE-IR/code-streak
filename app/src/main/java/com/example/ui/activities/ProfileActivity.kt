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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.DevCyan
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakFlame
import com.example.util.ShareUtil
import com.example.viewmodel.ProfileUiState
import com.example.viewmodel.ProfileViewModel

class ProfileActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by profileViewModel.uiState.collectAsState()

            CodeStreakTheme(themeMode = uiState.themeMode) {
                LaunchedEffect(uiState.isLoggedOut) {
                    if (uiState.isLoggedOut) {
                        Toast.makeText(this@ProfileActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ProfileActivity, AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                }

                ProfileScreen(
                    uiState = uiState,
                    onEditGoalsPrompt = { profileViewModel.setEditingGoals(true) },
                    onDismissEditGoals = { profileViewModel.setEditingGoals(false) },
                    onSaveGoals = { daily, weekly -> profileViewModel.updateGoals(daily, weekly) },
                    onSensitivityChange = { profileViewModel.setSensitivity(it) },
                    onToggleSound = { profileViewModel.toggleSound(it) },
                    onThemeChange = { profileViewModel.setThemeMode(it) },
                    onShareProfile = {
                        val user = uiState.user
                        if (user != null) {
                            ShareUtil.shareStreak(
                                context = this@ProfileActivity,
                                username = user.username,
                                streakDays = user.currentStreak,
                                totalHours = uiState.totalPracticeHours,
                                levelTitle = uiState.levelInfo.levelTitle
                            )
                        }
                    },
                    onOpenUrl = { url ->
                        ShareUtil.openWebUrl(this@ProfileActivity, url)
                    },
                    onContactSupport = {
                        val user = uiState.user
                        ShareUtil.sendSupportEmail(
                            context = this@ProfileActivity,
                            userEmail = user?.email ?: "developer@example.com",
                            username = user?.username ?: "Developer"
                        )
                    },
                    onOpenAdminPanel = {
                        val intent = Intent(this@ProfileActivity, AdminActivity::class.java)
                        startActivity(intent)
                    },
                    onLogout = { profileViewModel.logout() },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEditGoalsPrompt: () -> Unit,
    onDismissEditGoals: () -> Unit,
    onSaveGoals: (daily: Int, weekly: Int) -> Unit,
    onSensitivityChange: (String) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onThemeChange: (String) -> Unit,
    onShareProfile: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onContactSupport: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Developer Profile & Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onShareProfile,
                        modifier = Modifier.testTag("share_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = StreakFlame
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_user_card"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(StreakFlame, StreakAmber)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState.user?.username?.take(2) ?: "CS").uppercase(),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.user?.username ?: "Developer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = uiState.user?.email ?: "alex.turner@dev.io",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StreakAmber.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, StreakAmber.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Level ${uiState.levelInfo.level} • ${uiState.levelInfo.levelTitle}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StreakAmber,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Theme Mode Selector Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = null,
                            tint = StreakAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Theme Appearance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM" to "System", "DARK" to "Dark", "LIGHT" to "Light").forEach { (mode, label) ->
                            FilterChip(
                                selected = uiState.themeMode.equals(mode, ignoreCase = true),
                                onClick = { onThemeChange(mode) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StreakFlame,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Goals Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Practice Targets",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onEditGoalsPrompt) {
                            Text("Edit Goals", color = StreakFlame, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Daily Goal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${uiState.user?.dailyGoalMinutes ?: 45} mins/day",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text("Weekly Goal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${(uiState.user?.weeklyGoalMinutes ?: 300) / 60} hrs/week",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Haptic Sound Toggle Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = StreakFlame,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Milestone Sound Effects",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Audio beep on milestone shake",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = uiState.soundEnabled,
                        onCheckedChange = { onToggleSound(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StreakFlame
                        )
                    )
                }
            }

            // Sensor & Shake Settings Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = DevCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Accelerometer Shake Sensitivity",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { level ->
                            FilterChip(
                                selected = uiState.shakeSensitivity.equals(level, ignoreCase = true),
                                onClick = { onSensitivityChange(level) },
                                label = { Text(level, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StreakFlame,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // External Coding Practice Links
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Practice Resources (Open in Browser)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ResourceLinkRow(
                        title = "Kotlin Documentation",
                        url = "https://kotlinlang.org/docs/home.html",
                        onClick = { onOpenUrl("https://kotlinlang.org/docs/home.html") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
                    ResourceLinkRow(
                        title = "LeetCode Practice",
                        url = "https://leetcode.com/problemset/all/",
                        onClick = { onOpenUrl("https://leetcode.com/problemset/all/") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
                    ResourceLinkRow(
                        title = "Android Jetpack Compose Docs",
                        url = "https://developer.android.com/jetpack/compose",
                        onClick = { onOpenUrl("https://developer.android.com/jetpack/compose") }
                    )
                }
            }

            // Support & Feedback
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onContactSupport() },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = DevCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Contact Developer Support",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Send questions or feedback via email",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Admin Control Panel Launcher (Exclusive to Administrator role)
            if (uiState.isAdmin || uiState.user?.isAdmin == true) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAdminPanel() },
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, StreakAmber.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = StreakAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Admin Control Panel",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Database management & global metrics",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = StreakAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Logout Button
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x26EF4444),
                    contentColor = Color(0xFFF87171)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Edit Goals Dialog
    if (uiState.isEditingGoals) {
        var dailyMinutes by remember { mutableIntStateOf(uiState.user?.dailyGoalMinutes ?: 45) }
        var weeklyMinutes by remember { mutableIntStateOf(uiState.user?.weeklyGoalMinutes ?: 300) }

        AlertDialog(
            onDismissRequest = onDismissEditGoals,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Update Practice Goals", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Daily Practice Goal: $dailyMinutes minutes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StreakAmber)
                    Slider(
                        value = dailyMinutes.toFloat(),
                        onValueChange = { dailyMinutes = it.toInt() },
                        valueRange = 15f..180f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = StreakFlame,
                            activeTrackColor = StreakFlame,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("daily_goal_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Weekly Practice Goal: ${weeklyMinutes / 60} hours ($weeklyMinutes mins)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StreakAmber)
                    Slider(
                        value = weeklyMinutes.toFloat(),
                        onValueChange = { weeklyMinutes = it.toInt() },
                        valueRange = 60f..1800f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = StreakFlame,
                            activeTrackColor = StreakFlame,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("weekly_goal_slider")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onSaveGoals(dailyMinutes, weeklyMinutes) },
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame),
                    modifier = Modifier.testTag("save_goals_button")
                ) {
                    Text("Save Goals", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEditGoals) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun ResourceLinkRow(
    title: String,
    url: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = url,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open link",
            tint = DevCyan,
            modifier = Modifier.size(18.dp)
        )
    }
}
