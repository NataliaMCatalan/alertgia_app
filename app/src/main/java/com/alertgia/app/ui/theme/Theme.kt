package com.alertgia.app.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = AlertgiaGreen,
    onPrimary = Color.White,
    primaryContainer = AlertgiaGreenLight,
    onPrimaryContainer = NavyDeep,
    secondary = NavyMid,
    onSecondary = Color.White,
    secondaryContainer = NavyLight,
    onSecondaryContainer = Color.White,
    surface = Color(0xFFF8FAF7),
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFE8F0E4),
    onSurfaceVariant = Color(0xFF3A4A36),
    error = DangerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = AlertgiaGreenLight,
    onPrimary = NavyDeep,
    primaryContainer = AlertgiaGreenDark,
    onPrimaryContainer = AlertgiaGreenLight,
    secondary = NavyLight,
    onSecondary = Color.White,
    secondaryContainer = NavyMid,
    onSecondaryContainer = Color.White,
    surface = NavyDeep,
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = NavyMid,
    onSurfaceVariant = Color(0xFFBFC9C3),
    error = Color(0xFFFFB4AB)
)

@Composable
fun AlertgiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
