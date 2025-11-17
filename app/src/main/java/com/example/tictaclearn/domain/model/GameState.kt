package com.example.tictaclearn.domain.model

// El tipo de jugador (Human es 'X', AI es 'O')
enum class Player(val symbol: Char) {
    Human('X'), // 'X'
    AI('O');     // 'O'

    companion object {
        fun fromSymbol(symbol: Char): Player =
            entries.first { it.symbol == symbol }
    }
}

// El tamaño de la cuadrícula de Tic-Tac-Toe
private const val BOARD_SIZE = 9

/**
 * Representa el resultado final o actual de una partida.
 */
sealed class GameResult {
    data object Playing : GameResult()
    data object Draw : GameResult()
    // Añadimos winningLine, esencial para la UI y la IA.
    data class Win(val winner: Player, val winningLine: List<Int>) : GameResult()
}

/**
 * Representa el estado actual del tablero de juego (las 9 celdas).
 */
data class Board(
    val cells: List<Char> = List(BOARD_SIZE) { ' ' }
) {
    val isFull: Boolean
        get() = cells.none { it == ' ' }

    fun isPositionAvailable(position: Int): Boolean {
        return position in 0 until BOARD_SIZE && cells[position] == ' '
    }

    /**
     * Devuelve la lista de índices (0-8) de las celdas vacías disponibles para un movimiento.
     */
    fun getAvailablePositions(): List<Int> {
        return cells.withIndex()
            .filter { it.value == ' ' }
            .map { it.index }
    }

    /**
     * Devuelve una representación del estado actual del tablero como una cadena,
     * requerida por el agente de IA para usarla como clave en la Q-Table.
     * Ejemplo: "X_O_X_O__" (donde '_' es una celda vacía).
     */
    fun toStateString(): String {
        return cells.joinToString("") { if (it == ' ') "_" else it.toString() }
    }
}

/**
 * Chequea si existe una línea ganadora (3 en raya).
 * Esta es una función de extensión para hacer la lógica de chequeo más limpia.
 * (Las celdas del tablero van de 0 a 8)
 */
fun Board.checkGameResult(): GameResult {
    // Todas las líneas posibles: horizontales, verticales, diagonales.
    val winningLines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Horizontales
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Verticales
        listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonales
    )

    for (line in winningLines) {
        val (i1, i2, i3) = line
        val c1 = cells[i1]
        val c2 = cells[i2]
        val c3 = cells[i3]

        // Si las tres celdas no están vacías y son iguales, hay un ganador.
        if (c1 != ' ' && c1 == c2 && c2 == c3) {
            val winner = Player.fromSymbol(c1)
            return GameResult.Win(winner, line)
        }
    }

    // Si no hay ganador y el tablero está lleno, es un empate.
    if (isFull) {
        return GameResult.Draw
    }

    // Si no ha terminado, el juego sigue.
    return GameResult.Playing
}


/**
 * Representa el estado completo de la partida, incluyendo el tablero y el historial.
 */
data class GameState(
    val board: Board,
    val currentPlayer: Player,
    val result: GameResult,
    val gameHistory: List<Board> // Historial de tableros
) {
    // Propiedad computada para verificar si el juego ha terminado.
    // Útil para la lógica del ViewModel/UI.
    val isFinished: Boolean
        get() = result != GameResult.Playing

    /**
     * Mueve el jugador a la posición especificada y retorna el nuevo GameState.
     */
    fun move(position: Int, player: Player): GameState {
        require(board.isPositionAvailable(position)) { "Posición no disponible: $position" }

        // 1. Crear nuevo tablero (inmutable)
        val newCells = board.cells.toMutableList().apply {
            this[position] = player.symbol
        }
        val newBoard = Board(cells = newCells)

        // 2. Determinar el resultado después del movimiento
        val newResult = newBoard.checkGameResult()

        // 3. Determinar el siguiente jugador
        // CORRECCIÓN: Usamos 'newResult != GameResult.Playing' para saber si el juego terminó,
        // ya que 'newResult' es de tipo GameResult y no tiene la propiedad 'isFinished'.
        val gameIsOver = newResult != GameResult.Playing
        val nextPlayer = if (gameIsOver) player else if (player == Player.Human) Player.AI else Player.Human

        // 4. Actualizar el historial
        val updatedHistory = gameHistory + newBoard

        // 5. Crear nuevo GameState
        return copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            result = newResult,
            gameHistory = updatedHistory
        )
    }

    companion object {
        fun initial() = GameState(
            board = Board(),
            currentPlayer = Player.Human,
            result = GameResult.Playing,
            gameHistory = emptyList()
        )
    }
}