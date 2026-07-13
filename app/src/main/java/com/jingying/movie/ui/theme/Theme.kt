package com.jingying.movie.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryText,
    secondary = SecondaryText,
    tertiary = TertiaryText,
    background = BackgroundWhite,
    surface = CardBackground,
    error = AccentRed,
    onPrimary = White,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    outline = BorderGray
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    secondary = SecondaryText,
    tertiary = TertiaryText,
    background = PrimaryText,
    surface = PrimaryText,
    error = AccentRed,
    onPrimary = PrimaryText,
    onBackground = White,
    onSurface = White,
    outline = BorderGray
)

@Composable
fun JingyingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = White.toArgb()
            window.navigationBarColor = White.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
