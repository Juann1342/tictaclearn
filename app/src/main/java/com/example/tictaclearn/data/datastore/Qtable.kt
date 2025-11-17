package com.example.tictaclearn.data.datastore

import kotlinx.serialization.Serializable

// 💡 Clase Contenedora para la serialización (Wrapper)
@Serializable
data class AiMemory(
    val qTable: QTable = emptyMap()
)

// 💡 Typealias para la Q-Table
// El formato es: Estado del Tablero (String) -> Lista de Q-Values (Double) para cada acción.
typealias QTable = Map<String, List<Double>>