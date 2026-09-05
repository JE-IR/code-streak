package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DevCyan
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.TechPurple

@Composable
fun CategoryBadgeChip(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (category) {
        "Algorithms / LeetCode" -> Pair(StreakFlame.copy(alpha = 0.15f), StreakFlame)
        "Android / Kotlin" -> Pair(MatrixGreen.copy(alpha = 0.15f), MatrixGreen)
        "Web Dev" -> Pair(TechPurple.copy(alpha = 0.15f), TechPurple)
        "System Design" -> Pair(DevCyan.copy(alpha = 0.15f), DevCyan)
        "Open Source" -> Pair(StreakAmber.copy(alpha = 0.15f), StreakAmber)
        else -> Pair(Color(0xFF334155), Color(0xFFE2E8F0))
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

