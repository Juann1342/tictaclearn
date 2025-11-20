package com.example.tictaclearn.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definimos SOLO un esquema oscuro personalizado
private val GameColorScheme = darkColorScheme(
    primary = NeonOrange,
    onPrimary = BackgroundDark,
    secondary = NeonCyan,
    onSecondary = BackgroundDark,
    tertiary = NeonGreen,
    error = NeonRed,

    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun TicTacLearnTheme(
    darkTheme: Boolean = true, // Forzamos Dark Mode
    dynamicColor: Boolean = false, // Deshabilitamos color dinámico para mantener la marca
    content: @Composable () -> Unit
) {
    val colorScheme = GameColorScheme // Usamos nuestro esquema de colores fijo
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asumimos que Typography está definido
        content = content
    )
}