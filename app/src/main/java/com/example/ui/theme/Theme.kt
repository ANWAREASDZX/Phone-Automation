package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ThemeAccent,
    onPrimary = ThemeBg,
    primaryContainer = ThemeAccentSoft,
    onPrimaryContainer = ThemeAccent,
    secondary = ThemeSurface2,
    onSecondary = ThemeText,
    background = ThemeBg,
    onBackground = ThemeText,
    surface = ThemeSurface,
    onSurface = ThemeText,
    surfaceVariant = ThemeSurface2,
    onSurfaceVariant = ThemeMuted,
    error = ThemeDanger,
    onError = ThemeText
)

@Composable
fun PhoneAutomationTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ThemeBg.toArgb()
            window.navigationBarColor = ThemeBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
