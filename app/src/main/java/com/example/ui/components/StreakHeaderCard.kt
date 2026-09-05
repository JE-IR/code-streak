package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserLevelInfo
import com.example.ui.theme.CharcoalSlate700
import com.example.ui.theme.CharcoalSlate800
import com.example.ui.theme.CharcoalSlate900
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakAmberLight
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.StreakFlameGlow
import com.example.ui.theme.StreakFlameLight

@Composable
fun StreakHeaderCard(
    currentStreak: Int,
    longestStreak: Int,
    levelInfo: UserLevelInfo,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_header_card"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    StreakFlame.copy(alpha = 0.6f),
                    StreakAmber.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.outline
                )
            )
        ),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            StreakFlameGlow,
                            Color.Transparent
                        ),
                        radius = 450f
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row with Charcoal Badge Pill & Flame Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flame Ember Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(StreakFlame, StreakAmber)
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CURRENT STREAK",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // High-Energy Pulsing Flame Icon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(flameScale)
                            .clip(CircleShape)
                            .background(StreakFlame.copy(alpha = 0.18f))
                            .border(1.dp, StreakFlame.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Flame",
                            tint = StreakFlame,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main Big Number Display
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$currentStreak",
                        fontSize = 62.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 62.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (currentStreak == 1) "day streak" else "days streak",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StreakAmber,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Milestone bonus text
                val daysToMilestone = (7 - (currentStreak % 7)).let { if (it == 7 && currentStreak > 0) 7 else it }
                Text(
                    text = "🔥 $daysToMilestone days until next milestone bonus • All-time Best: $longestStreak days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Level Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level ${levelInfo.level} • ${levelInfo.levelTitle}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StreakAmber
                        )
                        Text(
                            text = "${levelInfo.currentXp}/${levelInfo.nextLevelXp} mins",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { levelInfo.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape),
                        color = StreakFlame,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}


