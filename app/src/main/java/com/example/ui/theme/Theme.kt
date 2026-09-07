package com.example.ui.theme

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

val OliDarkColorScheme = darkColorScheme(
    primary = OliPrimary,
    onPrimary = OliOnPrimary,
    primaryContainer = OliPrimaryContainer,
    background = OliDarkBackground,
    onBackground = OliText,
    surface = OliDarkSurface,
    onSurface = OliText,
    surfaceVariant = OliDarkCard,
    onSurfaceVariant = OliSecondary,
    secondary = OliSecondary,
    tertiary = OliSuccess,
    error = OliError
)

val OliLightColorScheme = lightColorScheme(
    primary = OliPrimary,
    onPrimary = OliOnPrimary,
    primaryContainer = Color(0xFFDDE4FF),
    background = OliLightBackground,
    onBackground = OliLightText,
    surface = OliLightSurface,
    onSurface = OliLightText,
    surfaceVariant = OliLightCard,
    onSurfaceVariant = OliLightSecondary,
    secondary = OliLightSecondary,
    tertiary = OliSuccess,
    error = OliError
)

val OliAmoledColorScheme = darkColorScheme(
    primary = OliPrimary,
    onPrimary = OliOnPrimary,
    primaryContainer = OliPrimaryContainer,
    background = Color.Black,
    onBackground = OliText,
    surface = Color.Black,
    onSurface = OliText,
    surfaceVariant = Color(0xFF111111),
    onSurfaceVariant = OliSecondary,
    secondary = OliSecondary,
    tertiary = OliSuccess,
    error = OliError
)

@Composable
fun MyApplicationTheme(
    theme: String = "Dark",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        "Light" -> OliLightColorScheme
        "AMOLED" -> OliAmoledColorScheme
        else -> OliDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

