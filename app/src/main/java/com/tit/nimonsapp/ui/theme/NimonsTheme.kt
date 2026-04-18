package com.tit.nimonsapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NimonsGreen = Color(0xFF4CAF50)
private val NimonsGreenDark = Color(0xFF3AB02C)
private val NimonsGreenContainer = Color(0xFFC8E6C9)
private val NimonsOnGreenContainer = Color(0xFF1B5E20)
private val NimonsError = Color(0xFFB00020)

private val NimonsBg = Color(0xFFF2F2F2)

private val LightColorScheme = lightColorScheme(
    background = NimonsBg,
    surface = Color.White,
    surfaceTint = Color.Transparent,
    primary = NimonsGreen,
    onPrimary = Color.White,
    primaryContainer = NimonsGreenContainer,
    onPrimaryContainer = NimonsOnGreenContainer,
    secondary = NimonsGreen,
    onSecondary = Color.White,
    secondaryContainer = NimonsGreenContainer,
    onSecondaryContainer = NimonsOnGreenContainer,
    tertiary = NimonsGreen,
    onTertiary = Color.White,
    tertiaryContainer = NimonsGreenContainer,
    onTertiaryContainer = NimonsOnGreenContainer,
    error = NimonsError,
)

private val DarkColorScheme = darkColorScheme(
    primary = NimonsGreenDark,
    onPrimary = Color.White,
    primaryContainer = NimonsOnGreenContainer,
    onPrimaryContainer = NimonsGreenContainer,
    secondary = NimonsGreenDark,
    onSecondary = Color.White,
    secondaryContainer = NimonsOnGreenContainer,
    onSecondaryContainer = NimonsGreenContainer,
    tertiary = NimonsGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = NimonsOnGreenContainer,
    onTertiaryContainer = NimonsGreenContainer,
    error = NimonsError,
)

@Composable
fun NimonsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
