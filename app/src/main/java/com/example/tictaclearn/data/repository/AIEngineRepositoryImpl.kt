package com.example.tictaclearn.data.repository

import android.util.Log
import com.example.tictaclearn.data.datastore.AiMemoryDataStoreManager
import com.example.tictaclearn.data.datastore.MoodDataStoreManager
import com.example.tictaclearn.data.datastore.QTable
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.repository.AIEngineRepository
import com.example.tictaclearn.domain.model.checkGameResult
import com.example.tictaclearn.domain.model.Reward
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.math.abs

private const val TAG = "AIEngineRepo"

// Constantes de Algoritmo para Q-Learning
private const val LEARNING_RATE_ALPHA = 0.1
private const val DISCOUNT_FACTOR_GAMMA = 0.9

@Singleton
class AIEngineRepositoryImpl @Inject constructor(
    private val moodDataStoreManager: MoodDataStoreManager,
    private val aiMemoryDataStoreManager: AiMemoryDataStoreManager
) : AIEngineRepository {

    private val mutex = Mutex()
    private val random = Random.Default
    private var qTable: QTable = emptyMap()

    // --- HEURÍSTICA Y MINIMAX PARA GOMOKU (9x9) ---

    // Valores de evaluación (para Minimax)
    private val MINIMAX_WIN_SCORE = 10000000
    private val MINIMAX_BLOCK_SCORE = -MINIMAX_WIN_SCORE

    /**
     * Evalúa la puntuación de un tablero de Gomoku (9x9) para Minimax.
     */
    private fun evaluateGomokuBoard(board: Board, player: Player): Int {
        var score = 0
        val size = board.sideSize

        // 1. Prioridad al Centro (Movimiento de apertura más fuerte)
        val centerIndex = size * size / 2
        if (board.cells[centerIndex] == player.symbol) {
            score += 100 // Impulso por el centro
        }

        // 2. Evaluación de Patrones de Líneas (Heurística central)
        score += getPatternScore(board, player.symbol, size)
        score -= getPatternScore(board, Player.Human.symbol, size) * 2 // Penalizamos el doble la amenaza del oponente

        return score
    }

    /**
     * Calcula la puntuación heurística de patrones de 2, 3 y 4 en línea.
     */
    private fun getPatternScore(board: Board, symbol: Char, size: Int): Int {
        var patternScore = 0
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1) // H, V, D1, D2

        // Pesos para las formaciones
        val scoreMap = mapOf(
            4 to 50000,
            3 to 1000,
            2 to 100
        )

        // Iterar sobre cada celda y dirección para buscar patrones
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (board.cells[row * size + col] != symbol) continue

                for ((dr, dc) in directions) {
                    for ((length, scoreValue) in scoreMap) {
                        if (isPatternOpen(board, symbol, size, row, col, dr, dc, length)) {
                            patternScore += scoreValue
                        }
                    }
                }
            }
        }
        return patternScore
    }

    /**
     * Verifica si existe un patrón de 'length' fichas seguidas y si tiene al menos un extremo abierto.
     */
    private fun isPatternOpen(board: Board, symbol: Char, size: Int, startR: Int, startC: Int, dr: Int, dc: Int, length: Int): Boolean {
        var count = 0

        // 1. Verificar si el patrón existe
        for (k in 0 until length) {
            val r = startR + k * dr
            val c = startC + k * dc

            if (r in 0 until size && c in 0 until size && board.cells[r * size + c] == symbol) {
                count++
            } else {
                break
            }
        }

        if (count < length) return false

        // 2. Verificar si al menos un extremo está abierto (vacío)
        val prevR = startR - dr
        val prevC = startC - dc
        val nextR = startR + length * dr
        val nextC = startC + length * dc

        val isPrevOpen = isCellEmpty(board, size, prevR, prevC)
        val isNextOpen = isCellEmpty(board, size, nextR, nextC)

        return isPrevOpen || isNextOpen
    }

    /**
     * Helper para verificar si una celda está vacía y dentro de los límites.
     */
    private fun isCellEmpty(board: Board, size: Int, r: Int, c: Int): Boolean {
        if (r !in 0 until size || c !in 0 until size) return false
        return board.cells[r * size + c] == ' '
    }

    // --- ALGORITMO MINIMAX RECURSIVO ---

    private fun minimax(board: Board, depth: Int, isMaximizingPlayer: Boolean, playerToMove: Player): Int {
        val opponent = if (playerToMove == Player.AI) Player.Human else Player.AI
        val result = board.checkGameResult(5)

        // Caso Base: Fin del juego o límite de profundidad
        if (result is GameResult.Win) {
            return if (result.winner == Player.AI) MINIMAX_WIN_SCORE - (100 * depth) else MINIMAX_BLOCK_SCORE + (100 * depth)
        }
        if (result is GameResult.Draw) return 0
        if (depth == 0) return evaluateGomokuBoard(board, Player.AI)

        val availableMoves = board.getAvailablePositions()
        if (availableMoves.isEmpty()) return 0

        if (isMaximizingPlayer) { // Turno de la IA (Player.AI)
            var maxEval = Int.MIN_VALUE
            for (move in availableMoves) {
                val newBoard = board.simulateMove(move, Player.AI.symbol)
                val eval = minimax(newBoard, depth - 1, false, opponent)
                maxEval = maxOf(maxEval, eval)
            }
            return maxEval
        } else { // Turno del Jugador Humano (Player.Human)
            var minEval = Int.MAX_VALUE
            for (move in availableMoves) {
                val newBoard = board.simulateMove(move, Player.Human.symbol)
                val eval = minimax(newBoard, depth - 1, true, opponent)
                minEval = minOf(minEval, eval)
            }
            return minEval
        }
    }

    // --- LÓGICA PRINCIPAL (get Best Move) ---

    override suspend fun getNextMove(board: Board, currentMood: Mood): Int? {
        // Decide qué IA usar
        return if (currentMood.minimaxDepth > 0 && board.sideSize == 9) {
            // --- GOMOKU (MINIMAX) ---
            findBestGomokuMove(board, currentMood.minimaxDepth, currentMood.gomokuExplorationRate)
        } else {
            // --- CLÁSICO (Q-LEARNING) ---
            mutex.withLock {
                if (qTable.isEmpty()) { qTable = aiMemoryDataStoreManager.getQTable() }
            }
            findBestClassicMove(board, currentMood.epsilon)
        }
    }

    /**
     * Lógica para el Gomoku (9x9) usando Minimax con exploración (para hacerlo más fácil).
     */
    private fun findBestGomokuMove(board: Board, depth: Int, explorationRate: Double): Int? {
        val availableMoves = board.getAvailablePositions()
        if (availableMoves.isEmpty()) return null

        // 🚨 PASO CRÍTICO: Exploración Epsilon-Greedy para Minimax
        if (random.nextDouble() < explorationRate) {
            Log.d(TAG, "Gomoku: Movimiento aleatorio (Exploración) debido a tasa $explorationRate.")
            return availableMoves.random(random) // Movimiento de "error"
        }

        // Lógica Minimax (Explotación)
        var bestMove: Int? = null
        var bestEvaluation = Int.MIN_VALUE

        val moveEvaluations = mutableMapOf<Int, Int>()

        for (move in availableMoves) {
            val newBoard = board.simulateMove(move, Player.AI.symbol)
            // Llama a minimax para evaluar el tablero resultante después del movimiento de la IA
            val evaluation = minimax(newBoard, depth - 1, false, Player.Human)
            moveEvaluations[move] = evaluation

            if (evaluation > bestEvaluation) {
                bestEvaluation = evaluation
                bestMove = move
            }
        }

        val tiedMoves = moveEvaluations.filterValues { it == bestEvaluation }.keys

        // Manejar el caso especial del movimiento inicial al centro
        if (board.cells.all { it == ' ' } && 40 in availableMoves) {
            return 40
        }

        // Devolvemos un movimiento al azar entre los mejores
        return tiedMoves.randomOrNull(random) ?: availableMoves.randomOrNull(random)
    }

    // --- LÓGICA Q-LEARNING (CLÁSICO 3x3) (Se mantiene igual) ---

    private fun findBestClassicMove(board: Board, epsilon: Double): Int? {
        val availableMoves = board.getAvailablePositions()
        if (availableMoves.isEmpty()) return null

        val state = boardToState(board)
        val qValues = qTable[state] ?: getHeuristicQValues(state)
        val bestAction = qValues.withIndex()
            .filter { it.index in availableMoves }
            .maxByOrNull { it.value }

        return if (random.nextDouble() < epsilon) {
            availableMoves.random(random)
        } else {
            bestAction?.index ?: availableMoves.random(random)
        }
    }

    private fun getHeuristicQValues(state: String): List<Double> {
        val values = MutableList(9) { 0.0 }
        if (state[4] == ' ') {
            values[4] = 0.1
        }
        listOf(0, 2, 6, 8).filter { state[it] == ' ' }.forEach { values[it] = 0.05 }

        qTable = qTable.toMutableMap().apply { this[state] = values }
        return values
    }

    private fun boardToState(board: Board): String {
        return board.toStateString().replace(Player.AI.symbol, 'O').replace(Player.Human.symbol, 'X')
    }

    // --- LÓGICA DE MEMORIA (updateMemory) ---

    // La implementación de updateMemory para Q-Learning se deja tal cual la proporcionaste
    override suspend fun updateMemory(gameHistory: List<Board>) {
        // Solo actualizamos la memoria si el juego anterior fue Tic-Tac-Toe (tablero de 9 celdas)
        if (gameHistory.isEmpty() || gameHistory.last().sideSize != 3) {
            Log.d(TAG, "No se actualiza la memoria (no es Tic-Tac-Toe).")
            return
        }

        val currentQTable = qTable.toMutableMap()
        var learningEvents = 0

        mutex.withLock {
            for (i in 0 until gameHistory.size - 1) {
                val state = boardToState(gameHistory[i])
                val stateAfterAiMove = gameHistory[i + 1]
                val stateAfterHumanMove = if (i + 2 < gameHistory.size) gameHistory[i + 2] else null

                val available = gameHistory[i].getAvailablePositions()
                val occupied = stateAfterAiMove.getAvailablePositions()
                val actionIndex = available.firstOrNull { it !in occupied } ?: continue

                var reward = Reward.DEFAULT
                var maxFutureQ = 0.0
                val stateKey = boardToState(gameHistory[i])

                if (stateAfterHumanMove == null) {
                    val finalResult = stateAfterAiMove.checkGameResult(3)
                    reward = Reward.getReward(finalResult, Player.AI)
                    maxFutureQ = 0.0
                } else {
                    val resultAfterHuman = stateAfterHumanMove.checkGameResult(3)
                    if (resultAfterHuman is GameResult.Win && resultAfterHuman.winner == Player.Human) {
                        reward = Reward.LOSE
                        maxFutureQ = 0.0
                    } else if (resultAfterHuman is GameResult.Draw) {
                        reward = Reward.TIE
                    } else {
                        val nextAiStateKey = boardToState(stateAfterHumanMove)
                        val nextQValues = currentQTable[nextAiStateKey] ?: getHeuristicQValues(nextAiStateKey)
                        maxFutureQ = nextQValues.maxOrNull() ?: 0.0
                    }
                }

                val currentQValues = currentQTable[stateKey]?.toMutableList() ?: getHeuristicQValues(stateKey).toMutableList()
                val oldQ = currentQValues[actionIndex]
                val newQ = oldQ + LEARNING_RATE_ALPHA * (reward + DISCOUNT_FACTOR_GAMMA * maxFutureQ - oldQ)

                currentQValues[actionIndex] = newQ
                currentQTable[stateKey] = currentQValues
                learningEvents++
            }

            aiMemoryDataStoreManager.saveQTable(currentQTable)
            qTable = currentQTable
            Log.d(TAG, "Memoria actualizada ($learningEvents pasos).")
        }
    }


    override suspend fun clearMemory() {
        mutex.withLock {
            qTable = emptyMap()
            aiMemoryDataStoreManager.clearQTable()
        }
    }

    // --- MÉTODOS MOOD (Delegación a DataStore) ---
    override suspend fun getDailyMood(): Mood {
        val id = moodDataStoreManager.getMoodId()
        return Mood.fromId(id) ?: Mood.getDefaultDailyMood()
    }

    override suspend fun saveDailyMood(mood: Mood) {
        moodDataStoreManager.saveMoodId(mood.id)
    }

}