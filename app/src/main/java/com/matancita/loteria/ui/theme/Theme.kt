package com.matancita.loteria.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LuckyGreen,
    secondary = GoldAccent,
    tertiary = LightGreen,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = LuckyGreen,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = LuckyGreen,
    secondary = GoldAccent,
    tertiary = LightGreen,
    background = Color(0xFFF5F5F5), // Un blanco hueso o gris muy claro
    surface = Color(0xFFFFFFFF), // Blanco para Cards, etc.
    onPrimary = TextPrimary, // Texto sobre LuckyGreen
    onSecondary = LuckyGreen, // Texto sobre GoldAccent
    onTertiary = LuckyGreen, // Texto sobre LightGreen
    onBackground = Color(0xFF1C1B1F), // Texto oscuro sobre fondos claros
    onSurface = Color(0xFF1C1B1F) // Texto oscuro sobre superficies claras
)

@Composable
fun NumerosDeLaSuerteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Desactivado para forzar nuestros colores temáticos
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
    // Inside your Composable function:
    val view = LocalView.current
    val desiredStatusBarColor = MaterialTheme.colorScheme.primary // Or any dynamic color
    val useDarkIcons = !isSystemInDarkTheme() // Or based on your theme's primary color's luminance

    if (!view.isInEditMode) { // Required check to prevent errors in preview
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                // Set status bar color
                window.statusBarColor = desiredStatusBarColor.toArgb()

                // Set status bar icon colors (light or dark)
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkIcons
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de tener Typography.kt
        content = content
    )
}