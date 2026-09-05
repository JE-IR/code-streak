package com.example.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesManager
import com.example.sensor.ShakeDetector
import com.example.ui.components.CategoryBadgeChip
import com.example.ui.components.ShakePulseVisualizer
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.DevCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakFlame
import com.example.util.ShareUtil
import com.example.util.TimeFormatters
import com.example.viewmodel.ActiveSessionUiState
import com.example.viewmodel.ActiveSessionViewModel

class ActiveSessionActivity : ComponentActivity() {

    private val sessionViewModel: ActiveSessionViewModel by viewModels()
    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Algorithm Problem Solving"
        val category = intent.getStringExtra("EXTRA_CATEGORY") ?: "Algorithms / LeetCode"
        sessionViewModel.configureSession(title, category)

        // Initialize Shake Detector sensor
        val prefs = PreferencesManager(this)
        shakeDetector = ShakeDetector(this) {
            runOnUiThread {
                sessionViewModel.onShakeDetected()
            }
        }.apply {
            setSensitivity(prefs.shakeSensitivity)
        }

        setContent {
            val uiState by sessionViewModel.uiState.collectAsState()

            CodeStreakTheme(themeMode = uiState.themeMode) {
                DisposableEffect(Unit) {
                    shakeDetector?.startListening()
                    sessionViewModel.startTimer()
                    onDispose {
                        shakeDetector?.stopListening()
                    }
                }

                ActiveSessionScreen(
                    uiState = uiState,
                    isSensorAvailable = shakeDetector?.isSensorAvailable == true,
                    onStartResume = { sessionViewModel.resumeTimer() },
                    onPause = { sessionViewModel.pauseTimer() },
                    onFinishPrompt = { sessionViewModel.openFinishDialog() },
                    onDismissFinishDialog = { sessionViewModel.dismissFinishDialog() },
                    onConfirmFinish = {
                        sessionViewModel.saveAndFinishSession { sessionId ->
                            Toast.makeText(this@ActiveSessionActivity, "🎉 Session Completed & Saved!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ActiveSessionActivity, HistoryActivity::class.java).apply {
                                putExtra("EXTRA_HIGHLIGHT_SESSION_ID", sessionId)
                            }
                            startActivity(intent)
                            finish()
                        }
                    },
                    onSimulateShake = { sessionViewModel.onShakeDetected() },
                    onLogMilestone = { type, label -> sessionViewModel.logManualMilestone(type, label) },
                    onUpdateNotes = { sessionViewModel.updateNotes(it) },
                    onShareActive = {
                        ShareUtil.shareSession(
                            context = this@ActiveSessionActivity,
                            title = uiState.sessionTitle,
                            category = uiState.sessionCategory,
                            durationStr = TimeFormatters.formatDurationHhMmSs(uiState.elapsedSeconds),
                            shakes = uiState.shakeCount
                        )
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector?.startListening()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector?.stopListening()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    uiState: ActiveSessionUiState,
    isSensorAvailable: Boolean,
    onStartResume: () -> Unit,
    onPause: () -> Unit,
    onFinishPrompt: () -> Unit,
    onDismissFinishDialog: () -> Unit,
    onConfirmFinish: () -> Unit,
    onSimulateShake: () -> Unit,
    onLogMilestone: (type: String, label: String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onShareActive: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Practice In Progress",
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
                        onClick = onShareActive,
                        modifier = Modifier.testTag("share_active_session_button")
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category & Title
            CategoryBadgeChip(category = uiState.sessionCategory)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uiState.sessionTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pulse Visualizer + Shake Sensor Core
            ShakePulseVisualizer(
                isSensorActive = uiState.isRunning,
                isCelebration = uiState.isShakeCelebrationVisible
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer Display
            Text(
                text = TimeFormatters.formatDurationHhMmSs(uiState.elapsedSeconds),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = if (uiState.isRunning) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("timer_display")
            )

            Text(
                text = if (uiState.isRunning) "● Session Active • Shake device to log" else "⏸ Session Paused",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (uiState.isRunning) MatrixGreen else StreakAmber
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Controls Row (Pause/Resume, Finish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isRunning) {
                    Button(
                        onClick = onPause,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1f)
                            .testTag("pause_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null,
                            tint = StreakAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStartResume,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen),
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1f)
                            .testTag("resume_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resume", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onFinishPrompt,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .testTag("finish_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finish", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Shake Celebration Notification Card
            AnimatedVisibility(
                visible = uiState.isShakeCelebrationVisible,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = StreakAmber.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StreakAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = StreakAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.recentShakeFeedback ?: "Shake detected! Milestone logged.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Sensor Milestone Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Milestones Logged",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.shakeCount} Total",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StreakAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isSensorAvailable) {
                            "💡 Tip: Give your phone a quick shake whenever you complete a pomodoro or solve a bug."
                        } else {
                            "⚠️ Accelerometer sensor not detected on this device. Use manual buttons below."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Milestone Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onSimulateShake,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_shake_button")
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shake", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { onLogMilestone("PROBLEM_SOLVED", "Problem Solved") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("log_problem_button")
                        ) {
                            Text("+1 Problem", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { onLogMilestone("POMODORO_LAP", "Pomodoro 25m Lap") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("log_pomodoro_button")
                        ) {
                            Text("25m Lap", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Motivational Quote Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = DevCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MOTIVATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = DevCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"${uiState.currentQuote.text}\"",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "— ${uiState.currentQuote.author}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session Notes Input
            OutlinedTextField(
                value = uiState.sessionNotes,
                onValueChange = onUpdateNotes,
                label = { Text("Session Notes & Key Learnings") },
                placeholder = { Text("E.g. Learned how to optimize space complexity using two pointers...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("session_notes_input"),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Finish Confirmation Dialog
    if (uiState.isFinishDialogOpen) {
        val earnedMinutes = (uiState.elapsedSeconds / 60).coerceAtLeast(1L)
        AlertDialog(
            onDismissRequest = onDismissFinishDialog,
            title = {
                Text("Complete Session?", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Great work! You logged:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⏱️ Time: ${TimeFormatters.formatSecondsToHoursAndMinutes(uiState.elapsedSeconds)}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "📳 Shakes: ${uiState.shakeCount} milestones",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "⚡ XP Earned: +$earnedMinutes minutes",
                        fontWeight = FontWeight.SemiBold,
                        color = StreakFlame
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlame),
                    modifier = Modifier.testTag("confirm_finish_dialog_button")
                ) {
                    Text("Save & Log", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissFinishDialog) {
                    Text("Continue Practicing")
                }
            }
        )
    }
}
