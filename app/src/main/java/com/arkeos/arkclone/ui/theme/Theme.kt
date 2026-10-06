package com.arkeos.arkclone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun ArkPalette.darkScheme() = darkColorScheme(
    primary = ink,
    onPrimary = paper,
    primaryContainer = border,
    onPrimaryContainer = ink,
    secondary = soft,
    onSecondary = paper,
    secondaryContainer = surfaceVariant,
    onSecondaryContainer = soft,
    tertiary = subtle,
    onTertiary = paper,
    background = paper,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = subtle,
    outline = border,
    outlineVariant = borderStrong
)

private fun ArkPalette.lightScheme() = lightColorScheme(
    primary = ink,
    onPrimary = paper,
    primaryContainer = border,
    onPrimaryContainer = ink,
    secondary = soft,
    onSecondary = paper,
    secondaryContainer = surfaceVariant,
    onSecondaryContainer = soft,
    tertiary = subtle,
    onTertiary = paper,
    background = paper,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = subtle,
    outline = border,
    outlineVariant = borderStrong
)

@Composable
fun ArkCloneTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette

    CompositionLocalProvider(LocalArkPalette provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) palette.darkScheme() else palette.lightScheme(),
            typography = ArkCloneTypography,
            content = content
        )
    }
}
