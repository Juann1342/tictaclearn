package com.example.tictaclearn.domain.model

// domain/model/GameMode.kt

/**
 * Define los modos de juego disponibles.
 */
data class GameMode(
    val id: String,
    val displayName: String,
    val boardSize: Int, // 3 para TicTacToe, 9 para Gomoku
    val winningLength: Int // 3 para TicTacToe, 5 para Gomoku
) {
    companion object {
        // Modo 1: Tic-Tac-Toe 3x3 (Q-Learning)
        val CLASSIC = GameMode(
            id = "classic_3x3",
            displayName = "Classic (3x3) - Q-Learning",
            boardSize = 3,
            winningLength = 3
        )

        // Modo 2: Gomoku / Five in a Row 9x9 (Minimax o Heurística)
        val GOMOKU = GameMode(
            id = "gomoku_9x9",
            displayName = "Gomoku (9x9) - Minimax",
            boardSize = 9,
            winningLength = 5
        )

        val ALL_MODES = listOf(CLASSIC, GOMOKU)

        fun fromId(id: String): GameMode? {
            return ALL_MODES.find { it.id == id }
        }
    }
}