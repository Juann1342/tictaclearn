package com.example.tictaclearn.domain.model


// domain/model/GameState.kt

sealed class GameResult {
    data object Playing : GameResult() // El juego está en curso
    data object Draw : GameResult()    // Empate
    data class Win(val winner: Player) : GameResult() // Alguien ganó
}

data class GameState(
    val board: Board,
    val currentPlayer: Player,
    val result: GameResult
) {
    companion object {
        // Estado inicial de cada nueva partida
        fun initial() = GameState(
            board = Board(),
            currentPlayer = Player.HUMAN, // Siempre empieza el humano
            result = GameResult.Playing
        )
    }
}