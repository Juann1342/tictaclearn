package com.example.tictaclearn.domain.model

// domain/model/Player.kt

enum class Player(val cell: Cell) {
    HUMAN(Cell.O), // El jugador humano
    AI(Cell.X);    // La Inteligencia Artificial

    // Una función de ayuda muy útil para alternar turnos
    fun nextPlayer(): Player {
        return when (this) {
            HUMAN -> AI
            AI -> HUMAN
        }
    }
}