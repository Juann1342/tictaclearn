package com.example.tictaclearn.domain.model

// domain/model/Cell.kt

enum class Cell {
    EMPTY, // Celda vacía
    X,     // Ficha de la IA
    O;     // Ficha del Jugador

    // Función de ayuda para cambiar de jugador (por ejemplo, O a X y viceversa)
    fun opponent(): Cell {
        return when (this) {
            X -> O
            O -> X
            EMPTY -> EMPTY
        }
    }
}