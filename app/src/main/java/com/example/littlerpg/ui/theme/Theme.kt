package com.example.littlerpg.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RpgDarkColorScheme = darkColorScheme(
    primary = RpgPrimary,
    onPrimary = RpgOnPrimary,
    primaryContainer = RpgPrimaryContainer,
    onPrimaryContainer = RpgOnPrimaryContainer,

    secondary = RpgSecondary,
    onSecondary = RpgOnSecondary,
    secondaryContainer = RpgSecondaryContainer,
    onSecondaryContainer = RpgOnSecondaryContainer,

    tertiary = RpgTertiary,
    onTertiary = RpgOnTertiary,
    tertiaryContainer = RpgTertiaryContainer,
    onTertiaryContainer = RpgOnTertiaryContainer,

    background = RpgBackground,
    onBackground = RpgOnBackground,

    surface = RpgSurface,
    onSurface = RpgOnSurface,

    surfaceVariant = RpgSurfaceVariant,
    onSurfaceVariant = RpgOnSurfaceVariant,

    error = RpgError,
    onError = RpgOnError,
    errorContainer = RpgErrorContainer,
    onErrorContainer = RpgOnErrorContainer,

    outline = RpgOutline,
    outlineVariant = RpgOutlineVariant
)

@Composable
fun LittleRpgTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RpgDarkColorScheme,
        typography = Typography,
        content = content
    )
}
