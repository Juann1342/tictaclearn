// domain/model/Qtable.kt
package com.chifuz.tictaclearn.data.datastore

import kotlinx.serialization.Serializable

// 💡 Clase Contenedora para la serialización (Wrapper)
@Serializable
data class AiMemory(
    val qTable: QTable = emptyMap(),
    // NUEVO: Contador de partidas jugadas para el modo Classic.
    val gamesPlayedCount: Int = 0
)

// 💡 Typealias para la Q-Table
// El formato es: Estado del Tablero (String) -> Lista de Q-Values (Double) para cada acción.
typealias QTable = Map<String, List<Double>>