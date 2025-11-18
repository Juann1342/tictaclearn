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
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = NeonRed
)

@Composable
fun TicTacLearnTheme(
    // Ignoramos el parámetro darkTheme del sistema, siempre será oscuro
    content: @Composable () -> Unit
) {
    val colorScheme = GameColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pintamos la barra de estado del color de fondo para inmersión total
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            // Forzamos iconos claros en la barra de estado
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de que tu archivo Type.kt exista, sino usa el default
        content = content
    )
}