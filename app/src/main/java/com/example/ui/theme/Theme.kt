package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = StreakFlame,
    onPrimary = Color.White,
    primaryContainer = StreakFlameGlow,
    onPrimaryContainer = StreakFlameLight,
    secondary = StreakAmber,
    onSecondary = CharcoalSlate950,
    secondaryContainer = CharcoalSlate800,
    onSecondaryContainer = StreakAmberLight,
    tertiary = DevCyan,
    onTertiary = CharcoalSlate950,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = CharcoalSlate700,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkSurfaceBorder,
    error = DangerRed
)

private val LightColorScheme = lightColorScheme(
    primary = StreakFlame,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDE0),
    onPrimaryContainer = Color(0xFF9A3412),
    secondary = StreakAmber,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightSurfaceBorder,
    error = DangerRed
)

@Composable
fun CodeStreakTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}