package com.chifuz.tictaclearn.ui.theme

import androidx.compose.ui.graphics.Color

// Colores Base "Neural Arena"
val BackgroundDark = Color(0xFF0F172A) // Azul muy oscuro (Fondo principal)
val SurfaceDark = Color(0xFF1E293B)    // Un tono más claro para Contenedores y Celdas del Tablero
val SurfaceLight = Color(0xFF334155)   // Para bordes o elementos inactivos

// Acentos de Neón (Tanto el color como el glow)
val NeonOrange = Color(0xFFFF7D00)     // Jugador Humano (X) - Botón Primario
val NeonCyan = Color(0xFF00E5FF)       // IA (O) - Acento Secundario
val NeonGreen = Color(0xFF00E676)      // Éxito / Victoria Humana
val NeonRed = Color(0xFFFF1744)        // Reset / Derrota IA

// 🚨 AGREGADOS PARA PARTY MODE
val NeonPurple = Color(0xFFD946EF)     // Jugador 3 (Triángulo)
val NeonYellow = Color(0xFFFFD700)     // Jugador 4 (Estrella)

// Textos (Usamos colores puros para alta legibilidad)
val TextWhite = Color(0xFFF8FAFC)
val TextGray = Color(0xFF94A3B8)

// ---- Estados del Oponente (Neón) ----
val StateSomnoliento = Color(0xFFD1B3FF)   // Violeta suave
val StateRelajado = Color(0xFF7FFFD4)      // Menta suave
val StateNormal = Color(0xFFCCFF66)        // Lima suave
val StateAtento = Color(0xFFFFE266)        // Amarillo vivo
val StateConcentrado = Color(0xFFFF8C42)   // Naranja intenso

val StateGomokuFacil = Color(0xFFD1B3FF)
val StateGomokuMedio = Color(0xFFFFD466)
val StateGomokuDificil = Color(0xFFFF7842)