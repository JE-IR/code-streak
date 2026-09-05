package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakAmberLight
import com.example.ui.theme.StreakFlame
import com.example.util.TimeFormatters

@Composable
fun StatMetricCards(
    todaySeconds: Long,
    todayGoalMinutes: Int,
    weekSeconds: Long,
    weekGoalMinutes: Int,
    totalSeconds: Long,
    totalShakes: Int,
    modifier: Modifier = Modifier
) {
    val todayMinutes = todaySeconds / 60
    val todayFraction = (todayMinutes.toFloat() / todayGoalMinutes.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val todayPct = (todayFraction * 100).toInt()

    val weekHours = weekSeconds / 3600f
    val weekGoalHours = weekGoalMinutes / 60f
    val weekFraction = (weekHours / weekGoalHours.coerceAtLeast(0.1f)).coerceIn(0f, 1f)
    val weekPct = (weekFraction * 100).toInt()

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(modifier = modifier.fillMaxWidth()) {
        // Bento 2-Column Grid: Left Daily Goal (Circular flame dial) + Right Weekly Goal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Daily Goal Tile (Circular Progress Dial)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(175.dp)
                    .testTag("today_metric_card"),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DAILY GOAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = StreakAmber,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Circular Flame Dial
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 8.dp.toPx()
                            // Background track
                            drawCircle(
                                color = trackColor,
                                style = Stroke(width = strokeWidth)
                            )
                            // Progress arc with Flame gradient
                            drawArc(
                                color = StreakFlame,
                                startAngle = -90f,
                                sweepAngle = 360f * todayFraction,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$todayPct%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${todayMinutes} / ${todayGoalMinutes} mins",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Weekly Goal Tile
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(175.dp)
                    .testTag("week_metric_card"),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WEEKLY GOAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = StreakFlame
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%.1fh", weekHours),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: ${String.format("%.1fh", weekGoalHours)} ($weekPct%)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(trackColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(weekFraction)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(StreakFlame, StreakAmber)
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bento Bottom Stats Tiles (Total Practice Time + Milestones Shaken)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("total_stats_card"),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Time Bento Tile
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "TOTAL TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TimeFormatters.formatSecondsToHoursAndMinutes(totalSeconds),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Milestones Shaken Bento Tile
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "SHAKES LOGGED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalShakes milestones",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StreakAmber
                    )
                }
            }
        }
    }
}
