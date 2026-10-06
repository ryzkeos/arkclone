package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Futuristic Monochrome Dark Color Scheme (Default)
private val ArkDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = GrayDark,
    onPrimaryContainer = PureWhite,
    secondary = SilverAccent,
    onSecondary = PureBlack,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = SilverAccent,
    tertiary = GrayLight,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = GraySubtle,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorderHover
)

// Monochrome Light Color Scheme
private val ArkLightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = SilverAccent,
    onPrimaryContainer = PureBlack,
    secondary = GrayDark,
    onSecondary = PureWhite,
    secondaryContainer = SilverAccent,
    onSecondaryContainer = PureBlack,
    tertiary = GrayMedium,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = SilverAccent,
    onSurfaceVariant = GrayDark,
    outline = GraySubtle,
    outlineVariant = GrayLight
)

@Composable
fun ArkCloneTheme(
    darkTheme: Boolean = true, // Dark mode is default as per prompt
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ArkDarkColorScheme else ArkLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArkCloneTypography,
        content = content
    )
}
