package com.mylaptopmarket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val UltraModernLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    
    secondary = AccentIndigo,
    onSecondary = SurfaceWhite,
    secondaryContainer = PrimaryLight,
    onSecondaryContainer = PrimaryBlue,
    
    tertiary = AccentTeal,
    onTertiary = SurfaceWhite,
    
    background = BackgroundLight,
    onBackground = TextPrimary,
    
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHover,
    onSurfaceVariant = TextSecondary,
    
    outline = CardBorder,
    outlineVariant = CardBorder.copy(alpha = 0.5f)
)

@Composable
fun MyLaptopMarketTheme(
    darkTheme: Boolean = false, // Enforce light theme as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UltraModernLightColorScheme,
        typography = Typography,
        content = content
    )
}
