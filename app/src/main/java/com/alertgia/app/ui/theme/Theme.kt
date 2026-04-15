package com.alertgia.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AlertgiaColorScheme = lightColorScheme(
    primary              = BrandGreen,
    onPrimary            = Color.White,
    primaryContainer     = BrandGreenLight,
    onPrimaryContainer   = BrandGreenDark,

    secondary            = TextSecondary,
    onSecondary          = Color.White,
    secondaryContainer   = SurfaceSubtle,
    onSecondaryContainer = TextPrimary,

    background           = SurfaceBg,
    onBackground         = TextPrimary,

    surface              = SurfaceCard,
    onSurface            = TextPrimary,
    surfaceVariant       = SurfaceSubtle,
    onSurfaceVariant     = TextSecondary,

    outline              = BorderLight,
    outlineVariant       = BorderMedium,

    error                = StatusDanger,
    onError              = Color.White,
    errorContainer       = StatusDangerBg,
    onErrorContainer     = Color(0xFFB71C1C),

    inverseSurface       = TextPrimary,
    inverseOnSurface     = SurfaceCard,
    inversePrimary       = BrandGreenDark,
)

// Line-height 1.5× for bodyMedium / bodyLarge improves medical reading comfort.
// No hardcoded text colours in Typography — the color scheme handles them.
private val AlertgiaTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),

    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),

    titleLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 22.sp, lineHeight = 33.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.1.sp),

    bodyLarge   = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.25.sp),
    bodySmall   = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),

    labelLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

/**
 * Root theme wrapper. Provides [LocalAppLanguage] with a safe "es" fallback so
 * any composable — including Previews — always has a language value.
 * [AlertgiaNavHost] overrides it with the real dynamic value from DataStore;
 * an inner CompositionLocalProvider always wins over the outer one.
 */
@Composable
fun AlertgiaTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLanguage provides "es") {
        MaterialTheme(
            colorScheme = AlertgiaColorScheme,
            typography  = AlertgiaTypography,
            content     = content
        )
    }
}
