package com.example.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.MainActivity
import com.example.data.local.DatabaseProvider
import com.example.data.local.PreferencesManager
import com.example.ui.theme.CharcoalSlate900
import com.example.ui.theme.CharcoalSlate950
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.DevCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakAmberLight
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.StreakFlameLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CodeStreakTheme {
                SplashScreenContent()
            }
        }

        lifecycleScope.launch {
            val dbProvider = DatabaseProvider(this@SplashActivity)
            dbProvider.initializeDefaultDataIfNeeded()
            val prefs = PreferencesManager(this@SplashActivity)

            delay(2200) // Engaging celebration duration

            val targetClass = if (prefs.isLoggedIn && prefs.currentUserId > 0) {
                MainActivity::class.java
            } else {
                AuthActivity::class.java
            }

            val intent = Intent(this@SplashActivity, targetClass)
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun SplashScreenContent() {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "celebrationAnimations")

    // Typing hands animation
    val handBounce by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handBounce"
    )

    // Flame celebration pulse
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePulse"
    )

    // Floating celebration particles
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleOffset"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CharcoalSlate950,
                        CharcoalSlate900,
                        CharcoalSlate950
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating Confetti / Sparks Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val count = 24
            for (i in 0 until count) {
                val randSeed = (i * 97) % 100
                val startX = size.width * (randSeed / 100f)
                val baseSpeed = 0.5f + ((i % 5) * 0.15f)
                val currentProgress = (particleOffset * baseSpeed + (randSeed / 100f)) % 1f
                val currentY = size.height * (1f - currentProgress)

                val color = when (i % 4) {
                    0 -> StreakFlame
                    1 -> StreakAmber
                    2 -> DevCyan
                    else -> MatrixGreen
                }

                val pSize = if (i % 3 == 0) 6.dp.toPx() else 4.dp.toPx()
                drawCircle(
                    color = color.copy(alpha = (1f - currentProgress * 0.8f).coerceIn(0f, 1f)),
                    radius = pSize,
                    center = Offset(startX + (Math.sin(currentProgress * 6.28 + i).toFloat() * 20f), currentY)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp)
        ) {
            // Happy Developer Coding & Celebration Illustration Canvas
            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // 1. Ambient Glow Aura behind coder
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StreakFlame.copy(alpha = 0.35f),
                                StreakAmber.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy - 20f),
                            radius = w * 0.5f
                        )
                    )

                    // 2. Happy Coder Head & Smile
                    val headRadius = 26.dp.toPx()
                    val headCenter = Offset(cx, cy - 40.dp.toPx())

                    // Head
                    drawCircle(
                        color = Color(0xFFFFDBAC),
                        radius = headRadius,
                        center = headCenter
                    )

                    // Hair / Beanie
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(headCenter.x - headRadius, headCenter.y - headRadius),
                        size = Size(headRadius * 2, headRadius * 2)
                    )

                    // Happy Eyes (^ ^)
                    drawArc(
                        color = Color(0xFF0F172A),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(headCenter.x - 14.dp.toPx(), headCenter.y - 6.dp.toPx()),
                        size = Size(8.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF0F172A),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(headCenter.x + 6.dp.toPx(), headCenter.y - 6.dp.toPx()),
                        size = Size(8.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )

                    // Big Happy Smile
                    drawArc(
                        color = Color(0xFFE11D48),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(headCenter.x - 7.dp.toPx(), headCenter.y + 4.dp.toPx()),
                        size = Size(14.dp.toPx(), 10.dp.toPx())
                    )

                    // 3. Body / Hoodie
                    val bodyPath = Path().apply {
                        moveTo(cx - 28.dp.toPx(), cy - 14.dp.toPx())
                        lineTo(cx + 28.dp.toPx(), cy - 14.dp.toPx())
                        lineTo(cx + 38.dp.toPx(), cy + 26.dp.toPx())
                        lineTo(cx - 38.dp.toPx(), cy + 26.dp.toPx())
                        close()
                    }
                    drawPath(
                        path = bodyPath,
                        color = StreakFlame
                    )

                    // 4. Laptop Screen & Base
                    val laptopWidth = 90.dp.toPx()
                    val laptopHeight = 52.dp.toPx()
                    val laptopLeft = cx - laptopWidth / 2f
                    val laptopTop = cy + 6.dp.toPx()

                    // Laptop Screen outer
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(laptopLeft, laptopTop),
                        size = Size(laptopWidth, laptopHeight),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )

                    // Screen Display (Glowing Matrix Cyan Code)
                    drawRoundRect(
                        color = Color(0xFF0A0F1D),
                        topLeft = Offset(laptopLeft + 4.dp.toPx(), laptopTop + 4.dp.toPx()),
                        size = Size(laptopWidth - 8.dp.toPx(), laptopHeight - 8.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )

                    // Code syntax lines on screen
                    drawLine(
                        color = DevCyan,
                        start = Offset(laptopLeft + 10.dp.toPx(), laptopTop + 14.dp.toPx()),
                        end = Offset(laptopLeft + 44.dp.toPx(), laptopTop + 14.dp.toPx()),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = MatrixGreen,
                        start = Offset(laptopLeft + 10.dp.toPx(), laptopTop + 22.dp.toPx()),
                        end = Offset(laptopLeft + 65.dp.toPx(), laptopTop + 22.dp.toPx()),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = StreakAmber,
                        start = Offset(laptopLeft + 18.dp.toPx(), laptopTop + 30.dp.toPx()),
                        end = Offset(laptopLeft + 52.dp.toPx(), laptopTop + 30.dp.toPx()),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // Laptop Keyboard Base
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(cx - 55.dp.toPx(), laptopTop + laptopHeight - 4.dp.toPx()),
                        size = Size(110.dp.toPx(), 8.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )

                    // 5. Typing Hands (Bouncing with animation)
                    drawCircle(
                        color = Color(0xFFFFDBAC),
                        radius = 8.dp.toPx(),
                        center = Offset(cx - 24.dp.toPx(), cy + 42.dp.toPx() + handBounce)
                    )
                    drawCircle(
                        color = Color(0xFFFFDBAC),
                        radius = 8.dp.toPx(),
                        center = Offset(cx + 24.dp.toPx(), cy + 42.dp.toPx() - handBounce)
                    )
                }

                // Celebrating Streak Flame floating on top
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .scale(flamePulse)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(StreakFlame, StreakAmber)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Flame",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Celebration Badge on top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DevCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = "Celebrate",
                        tint = DevCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "CodeStreak",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = StreakAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Build Habit. Log Practice. Master Code.",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = StreakAmberLight
                )
            }
        }
    }
}
