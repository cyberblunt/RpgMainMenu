package com.zerotoler.rpgmenu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RpgDarkColors = darkColorScheme(
    primary = CyanGlow,
    onPrimary = NavyBackground,
    primaryContainer = PanelBlue,
    secondary = PurpleAccent,
    onSecondary = TextPrimary,
    tertiary = YellowAccent,
    onTertiary = NavyBackground,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = PanelBlue,
    onSurface = TextPrimary,
    surfaceVariant = PanelBlueBright,
    onSurfaceVariant = TextMuted,
    error = RedBadge,
    onError = TextPrimary,
)

@Composable
fun RpgMainMenuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RpgDarkColors,
        typography = RpgTypography,
        content = content,
    )
}
