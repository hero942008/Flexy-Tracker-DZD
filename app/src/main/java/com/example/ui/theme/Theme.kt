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

private val DarkColorScheme = darkColorScheme(
    primary = VibrantPurpleLight,
    onPrimary = Color.Black,
    primaryContainer = VibrantPurpleDark,
    onPrimaryContainer = VibrantPurpleContainer,
    secondary = VibrantPeachContainer,
    onSecondary = OnVibrantPeach,
    secondaryContainer = VibrantDarkPeachContainer,
    onSecondaryContainer = VibrantDarkOnPeach,
    error = VibrantFeeRed,
    background = VibrantDarkBg,
    surface = VibrantDarkSurface,
    surfaceVariant = VibrantDarkSurfaceVariant,
    onBackground = VibrantDarkTextPrimary,
    onSurface = VibrantDarkTextPrimary,
    onSurfaceVariant = VibrantDarkTextSecondary,
    outline = VibrantDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = VibrantPurpleContainer,
    onPrimaryContainer = OnVibrantPurpleContainer,
    secondary = VibrantPeachContainer,
    onSecondary = OnVibrantPeach,
    secondaryContainer = VibrantPeachContainer,
    onSecondaryContainer = OnVibrantPeach,
    error = VibrantFeeRed,
    background = VibrantCanvasBg,
    surface = VibrantCardSurface,
    surfaceVariant = VibrantSurfaceVariant,
    onBackground = VibrantTextPrimary,
    onSurface = VibrantTextPrimary,
    onSurfaceVariant = VibrantTextSecondary,
    outline = VibrantBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent crafted brand theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

