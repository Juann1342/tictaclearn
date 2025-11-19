package com.example.tictaclearn.domain.model

enum class Player(val symbol: Char) {
    Human('X'),
    AI('O');

    companion object {
        fun fromSymbol(symbol: Char): Player = entries.first { it.symbol == symbol }
    }
}

sealed class GameResult {
    data object Playing : GameResult()
    data object Draw : GameResult()
    data class Win(val winner: Player, val winningLine: List<Int>) : GameResult()
}

data class GameState(
    val board: Board,
    val currentPlayer: Player,
    val result: GameResult,
    val gameHistory: List<Board>
) {
    // Estado inicial estático (por defecto 3x3)
    companion object {
        fun initial() = GameState(
            board = Board(3), // 3x3 por defecto
            currentPlayer = Player.Human,
            result = GameResult.Playing,
            gameHistory = listOf(Board(3))
        )
    }

    val isFinished: Boolean
        get() = result != GameResult.Playing

    /**
     * ✅ CORRECCIÓN: 'move' ahora acepta 'winningLength' para validar la victoria correctamente.
     */
    fun move(position: Int, player: Player, winningLength: Int): GameState {
        if (!board.isPositionAvailable(position)) return this

        // 1. Crear nuevo tablero
        val newCells = board.cells.toMutableList().apply {
            this[position] = player.symbol
        }
        // El tamaño se preserva implícitamente por la longitud de la lista
        val newBoard = Board(newCells)

        // 2. Verificar resultado usando el largo de victoria dinámico (3 o 5)
        val newResult = newBoard.checkGameResult(winningLength)

        // 3. Cambiar turno
        val gameIsOver = newResult != GameResult.Playing
        val nextPlayer = if (gameIsOver) player else if (player == Player.Human) Player.AI else Player.Human

        // 4. Actualizar historial
        val updatedHistory = gameHistory + newBoard

        return copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            result = newResult,
            gameHistory = updatedHistory
        )
    }
}