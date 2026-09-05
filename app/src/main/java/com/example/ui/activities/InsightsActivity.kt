package com.example.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.Badge
import com.example.ui.components.StreakHeaderCard
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.DevCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.StreakFlameLight
import com.example.ui.theme.TechPurple
import com.example.util.ShareUtil
import com.example.viewmodel.InsightsUiState
import com.example.viewmodel.InsightsViewModel

class InsightsActivity : ComponentActivity() {

    private val insightsViewModel: InsightsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(this)

        setContent {
            val uiState by insightsViewModel.uiState.collectAsState()

            CodeStreakTheme(themeMode = uiState.themeMode) {
                InsightsScreen(
                    uiState = uiState,
                    onShareMilestone = {
                        val user = uiState.user
                        if (user != null) {
                            val totalHours = uiState.totalPracticeSeconds / 3600f
                            ShareUtil.shareStreak(
                                context = this@InsightsActivity,
                                username = user.username,
                                streakDays = user.currentStreak,
                                totalHours = totalHours,
                                levelTitle = uiState.levelInfo.levelTitle
                            )
                        }
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        insightsViewModel.loadInsights()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onShareMilestone: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress Insights & Projections",
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
                actions = {
                    IconButton(
                        onClick = onShareMilestone,
                        modifier = Modifier.testTag("share_insights_button")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Rank Overview
                StreakHeaderCard(
                    currentStreak = uiState.user?.currentStreak ?: 1,
                    longestStreak = uiState.user?.longestStreak ?: 1,
                    levelInfo = uiState.levelInfo
                )
            }

            // Future Projection Engine Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("projection_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        DevCyan.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DevCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoGraph,
                                        contentDescription = null,
                                        tint = DevCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FUTURE PROJECTIONS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = DevCyan
                                    )
                                    Text(
                                        text = "Pace: ~${uiState.projections.dailyPaceMinutes.toInt()} min/day (${String.format("%.1f", uiState.projections.weeklyPaceHours)} hrs/week)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 100 Hours Target
                            ProjectionRow(
                                title = "100 Hours Deliberate Practice",
                                remainingHours = uiState.projections.hoursTo100Goal,
                                timeEstimate = "${uiState.projections.weeksTo100Goal.toInt()} weeks",
                                progressFraction = (uiState.projections.totalHoursLogged / 100f).coerceIn(0f, 1f),
                                accentColor = StreakFlame
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 1000 Hours Mastery Target
                            ProjectionRow(
                                title = "1,000 Hours Senior Engineer Milestone",
                                remainingHours = uiState.projections.hoursTo1000Goal,
                                timeEstimate = "${uiState.projections.monthsTo1000Goal.toInt()} months",
                                progressFraction = (uiState.projections.totalHoursLogged / 1000f).coerceIn(0f, 1f),
                                accentColor = TechPurple
                            )
                        }
                    }
                }
            }

            // Category Breakdown Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Practice Focus Distribution",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.categoryDistribution.isEmpty()) {
                            Text(
                                text = "Start sessions to populate your category focus chart.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.categoryDistribution.forEach { (cat, pct) ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cat, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            text = "${pct.toInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DevCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { pct / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = DevCyan,
                                        trackColor = MaterialTheme.colorScheme.surface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Achievement Badges Header
            item {
                Text(
                    text = "Achievement Badges (${uiState.badges.count { it.isUnlocked }} / ${uiState.badges.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Badges Grid items
            items(uiState.badges.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { badge ->
                        BadgeCard(
                            badge = badge,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("badge_${badge.id}")
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun ProjectionRow(
    title: String,
    remainingHours: Float,
    timeEstimate: String,
    progressFraction: Float,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (remainingHours <= 0) "✅ Achieved" else "in ~$timeEstimate",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${String.format("%.1f", remainingHours)} hours remaining",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BadgeCard(badge: Badge, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (badge.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (badge.isUnlocked) StreakAmber.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) StreakAmber.copy(alpha = 0.2f)
                        else Color.Gray.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (badge.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) StreakAmber else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (badge.isUnlocked) "UNLOCKED" else "${badge.currentValue}/${badge.requiredValue}",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = if (badge.isUnlocked) MatrixGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
