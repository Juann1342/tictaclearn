package com.example.tictaclearn.domain.model

// El tipo de jugador y su símbolo
enum class Player(val symbol: Char) {
    Human('X'), // 'X'
    AI('O');     // 'O'

    companion object {
        fun fromSymbol(symbol: Char): Player =
            entries.firstOrNull { it.symbol == symbol } ?: Human
    }
}

// Constante necesaria para el tamaño (puede quedarse aquí o en Board.kt, pero Board lo usa)
// Si está en Board.kt como private, aquí no hace falta.

/**
 * Representa el resultado final o actual de una partida.
 */
sealed class GameResult {
    data object Playing : GameResult()
    data object Draw : GameResult()
    data class Win(val winner: Player, val winningLine: List<Int>) : GameResult()
}

/**
 * Representa el estado completo de la partida.
 *
 * NOTA: La clase 'Board' NO se define aquí. Debe estar en Board.kt
 * para evitar errores de "Redeclaration".
 */
data class GameState(
    val board: Board, // Usa la clase Board importada del mismo paquete
    val currentPlayer: Player,
    val result: GameResult,
    val gameHistory: List<Board>
) {
    companion object {
        fun initial() = GameState(
            board = Board(),
            currentPlayer = Player.Human,
            result = GameResult.Playing,
            gameHistory = listOf(Board())
        )
    }

    val isFinished: Boolean
        get() = result != GameResult.Playing

    /**
     * Realiza un movimiento y devuelve el nuevo estado.
     */
    fun move(position: Int, player: Player): GameState {
        // Validaciones básicas
        if (!board.isPositionAvailable(position) || isFinished) return this

        // 1. Crear nueva lista de celdas
        val newCells = board.cells.toMutableList().apply {
            this[position] = player.symbol
        }
        val newBoard = Board(newCells)

        // 2. Verificar resultado
        val newResult = newBoard.checkGameResult()

        // 3. Cambiar turno
        val nextPlayer = if (newResult != GameResult.Playing) player else {
            if (player == Player.Human) Player.AI else Player.Human
        }

        // 4. Actualizar historial
        val newHistory = gameHistory + newBoard

        return copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            result = newResult,
            gameHistory = newHistory
        )
    }
}