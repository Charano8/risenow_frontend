package com.simats.risenow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Force always-dark Midnight Purple scheme — no dynamic color, no light theme
// Glassmorphism Dark Scheme
private val GlassColorScheme = darkColorScheme(
    primary          = PrimaryGradientStart,
    onPrimary        = TextPrimary,
    secondary        = PrimaryGradientEnd,
    onSecondary      = TextPrimary,
    background       = AppBackgroundTop,
    onBackground     = TextPrimary,
    surface          = GlassWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = GlassBorder,
    error            = DangerRed,
    onError          = TextPrimary
)

@Composable
fun RiseNowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlassColorScheme,
        typography  = Typography,
        content     = content
    )
}