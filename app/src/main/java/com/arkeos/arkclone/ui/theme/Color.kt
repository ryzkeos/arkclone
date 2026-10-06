package com.arkeos.arkclone.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

@Immutable
class ArkPalette(
    val isDark: Boolean,
    val paper: Color,
    val ink: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val borderStrong: Color,
    val muted: Color,
    val subtle: Color,
    val soft: Color
)

val DarkPalette = ArkPalette(
    isDark = true,
    paper = Color(0xFF000000),
    ink = Color(0xFFFFFFFF),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF181818),
    border = Color(0xFF262626),
    borderStrong = Color(0xFF404040),
    muted = Color(0xFF666666),
    subtle = Color(0xFFBDBDBD),
    soft = Color(0xFFE0E0E0)
)

val LightPalette = ArkPalette(
    isDark = false,
    paper = Color(0xFFFFFFFF),
    ink = Color(0xFF000000),
    surface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFFEDEDED),
    border = Color(0xFFD6D6D6),
    borderStrong = Color(0xFFBDBDBD),
    muted = Color(0xFF6B6B6B),
    subtle = Color(0xFF4D4D4D),
    soft = Color(0xFF262626)
)

val LocalArkPalette = staticCompositionLocalOf { DarkPalette }

val ArkPaper: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.paper
val ArkInk: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.ink
val ArkSurface: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.surface
val ArkSurfaceVariant: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.surfaceVariant
val ArkBorder: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.border
val ArkBorderStrong: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.borderStrong
val ArkMuted: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.muted
val ArkSubtle: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.subtle
val ArkSoft: Color @Composable @ReadOnlyComposable get() = LocalArkPalette.current.soft

private val InvertColorFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

val ArkLogoFilter: ColorFilter?
    @Composable @ReadOnlyComposable
    get() = if (LocalArkPalette.current.isDark) null else InvertColorFilter
