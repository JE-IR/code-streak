package com.example.ui.components

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DevCyan
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakFlame

@Composable
fun ShakePulseVisualizer(
    isSensorActive: Boolean,
    isCelebration: Boolean,
    modifier: Modifier = Modifier
) {
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(isCelebration) {
        if (isCelebration) {
            pulseScale.animateTo(
                targetValue = 1.35f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val ring1 = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )

    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Canvas rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isSensorActive) {
                val radius1 = (size.minDimension / 3f) * ring1.value
                val alpha1 = (1f - (ring1.value - 0.8f) / 0.8f).coerceIn(0f, 1f) * 0.4f
                drawCircle(
                    color = if (isCelebration) StreakAmber.copy(alpha = alpha1) else DevCyan.copy(alpha = alpha1),
                    radius = radius1,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Inner core icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isCelebration) {
                            listOf(StreakAmber, StreakFlame)
                        } else {
                            listOf(StreakFlame, Color(0xFFC2410C))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Vibration,
                contentDescription = "Shake sensor active",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}
