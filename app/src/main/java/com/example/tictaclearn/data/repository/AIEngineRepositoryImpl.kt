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

@Singleton
class AIEngineRepositoryImpl @Inject constructor(
    private val moodDataStoreManager: MoodDataStoreManager,
    private val aiMemoryDataStoreManager: AiMemoryDataStoreManager
) : AIEngineRepository {

    private val mutex = Mutex()
    private val random = Random.Default
    private var qTable: QTable = emptyMap()

    private companion object {
        const val LEARNING_RATE_ALPHA = 0.1
        const val DISCOUNT_FACTOR_GAMMA = 0.9
    }

    override suspend fun getNextMove(board: Board, currentMood: Mood): Int? {
        mutex.withLock {
            if (qTable.isEmpty()) {
                qTable = aiMemoryDataStoreManager.getQTable()
            }
        }

        val availableMoves = board.getAvailablePositions()
        if (availableMoves.isEmpty()) return null

        // 1. APERTURA NATURAL PARA GOMOKU
        // Si el tablero está vacío o tiene muy pocas fichas, jugamos cerca del centro pero con variedad.
        if (board.sideSize > 3 && board.cells.count { it != ' ' } < 2) {
            // Filtramos movimientos que estén en el cuadro central de 3x3 o 5x5
            val centerRange = (board.sideSize / 2 - 2)..(board.sideSize / 2 + 2)
            val centerMoves = availableMoves.filter {
                val row = it / board.sideSize
                val col = it % board.sideSize
                row in centerRange && col in centerRange
            }
            if (centerMoves.isNotEmpty()) return centerMoves.random()
        }

        // 2. Exploración (Aleatorio según Mood)
        if (random.nextDouble() < currentMood.epsilon) {
            return availableMoves.random()
        }

        // 3. Explotación (Memoria + Instinto)
        val stateKey = board.toStateString()
        // Si no hay memoria previa, usamos la heurística avanzada
        val qValues = qTable[stateKey] ?: getHeuristicQValues(board)

        var bestMoves = mutableListOf<Int>()
        var maxQ = -Double.MAX_VALUE

        for (moveIndex in availableMoves) {
            val qVal = if (moveIndex < qValues.size) qValues[moveIndex] else 0.0

            if (qVal > maxQ) {
                maxQ = qVal
                bestMoves.clear()
                bestMoves.add(moveIndex)
            } else if (qVal == maxQ) {
                bestMoves.add(moveIndex)
            }
        }

        return bestMoves.ifEmpty { availableMoves }.random()
    }

    /**
     * Calcula valores base. Distingue entre TicTacToe (simple) y Gomoku (complejo).
     */
    private fun getHeuristicQValues(board: Board): List<Double> {
        val size = board.sideSize

        // Lógica Tic-Tac-Toe (Simple)
        if (size == 3) {
            return getTicTacToeHeuristics(board)
        }

        // Lógica Gomoku (Avanzada - Detección de Patrones)
        return getGomokuHeuristics(board)
    }

    /**
     * 🧠 CEREBRO GOMOKU
     * Analiza líneas, diagonales y patrones abiertos/cerrados.
     */
    private fun getGomokuHeuristics(board: Board): List<Double> {
        val size = board.sideSize
        val heuristics = MutableList(board.cells.size) { 0.0 }
        val availableMoves = board.getAvailablePositions()

        for (pos in availableMoves) {
            // Simulamos poner nuestra ficha (Ataque)
            val attackScore = evaluatePositionScore(board, pos, Player.AI)

            // Simulamos que el oponente pone su ficha ahí (Defensa)
            // Multiplicamos por un factor (ej. 1.2) si queremos priorizar defensa sobre ataque
            val defenseScore = evaluatePositionScore(board, pos, Player.Human) * 1.2

            // La puntuación final es una mezcla.
            // Si bloquear al humano da 10,000 puntos, eso dominará la decisión.
            heuristics[pos] = attackScore + defenseScore
        }
        return heuristics
    }

    /**
     * Evalúa qué tan peligrosa o buena es una posición buscando patrones de líneas.
     */
    private fun evaluatePositionScore(board: Board, pos: Int, player: Player): Double {
        val size = board.sideSize
        val row = pos / size
        val col = pos % size
        val symbol = player.symbol

        var totalScore = 0.0

        // Direcciones: Horizontal, Vertical, Diagonal \, Diagonal /
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)

        for ((dr, dc) in directions) {
            // Contamos fichas consecutivas y espacios libres en los extremos
            var consecutive = 0
            var openEnds = 0

            // Hacia adelante
            var r = row + dr
            var c = col + dc
            while (r in 0 until size && c in 0 until size && board.cells[r * size + c] == symbol) {
                consecutive++
                r += dr
                c += dc
            }
            if (r in 0 until size && c in 0 until size && board.cells[r * size + c] == ' ') {
                openEnds++
            }

            // Hacia atrás
            r = row - dr
            c = col - dc
            while (r in 0 until size && c in 0 until size && board.cells[r * size + c] == symbol) {
                consecutive++
                r -= dr
                c -= dc
            }
            if (r in 0 until size && c in 0 until size && board.cells[r * size + c] == ' ') {
                openEnds++
            }

            // --- SISTEMA DE PUNTUACIÓN DE PATRONES ---
            // La ficha que acabamos de poner cuenta como 1, así que consecutive + 1
            val currentLength = consecutive + 1

            if (currentLength >= 5) {
                totalScore += 100_000.0 // ¡Victoria o Bloqueo de Victoria!
            } else if (currentLength == 4) {
                if (openEnds > 0) totalScore += 10_000.0 // 4 con espacio (Imparable)
                else totalScore += 5_000.0 // 4 cerrado (Hay que bloquear ya)
            } else if (currentLength == 3) {
                if (openEnds == 2) totalScore += 3_000.0 // 3 Abierto (Muy peligroso: _XXX_)
                else if (openEnds == 1) totalScore += 100.0 // 3 Cerrado (Poco peligroso)
            } else if (currentLength == 2) {
                if (openEnds == 2) totalScore += 50.0 // 2 Abierto
            }
        }

        // Factor posicional leve: Preferir el centro si no hay amenazas
        val center = size / 2.0
        val dist = abs(row - center) + abs(col - center)
        totalScore += (20.0 - dist) // Pequeño empujón hacia el centro

        return totalScore
    }

    private fun getTicTacToeHeuristics(board: Board): List<Double> {
        val heuristics = MutableList(board.cells.size) { 0.01 }
        val availableMoves = board.getAvailablePositions()

        // ... (Lógica TicTacToe simplificada para ahorrar espacio,
        // se basa en Win/Block/Center como antes)
        for (pos in availableMoves) {
            if (canWinNextMove(board, pos, Player.AI, 3)) heuristics[pos] += 100.0
            else if (canWinNextMove(board, pos, Player.Human, 3)) heuristics[pos] += 50.0
            else if (pos == 4) heuristics[pos] += 5.0
        }
        return heuristics
    }

    private fun canWinNextMove(board: Board, position: Int, player: Player, winLength: Int): Boolean {
        val simulated = board.simulateMove(position, player.symbol)
        val result = simulated.checkGameResult(winLength)
        return result is GameResult.Win && result.winner == player
    }

    override suspend fun updateMemory(gameHistory: List<Board>) {
        mutex.withLock {
            if (qTable.isEmpty()) qTable = aiMemoryDataStoreManager.getQTable()
            val currentQTable = qTable.toMutableMap()
            var learningEvents = 0

            for (i in 0 until gameHistory.size - 1) {
                val state = gameHistory[i]
                val nextState = gameHistory[i + 1]
                val actionIndex = findDiffIndex(state, nextState)
                if (actionIndex == -1 || nextState.cells[actionIndex] != Player.AI.symbol) continue

                val stateKey = state.toStateString()
                val winLength = if (state.sideSize > 3) 5 else 3

                var reward = 0.0
                var maxFutureQ = 0.0
                val result = nextState.checkGameResult(winLength)

                if (result is GameResult.Win && result.winner == Player.AI) reward = Reward.WIN
                else if (result is GameResult.Draw) reward = Reward.TIE
                else {
                    if (i + 2 < gameHistory.size) {
                        val stateAfterHuman = gameHistory[i + 2]
                        val resultHuman = stateAfterHuman.checkGameResult(winLength)
                        if (resultHuman is GameResult.Win && resultHuman.winner == Player.Human) reward = Reward.LOSE
                        else {
                            val nextKey = stateAfterHuman.toStateString()
                            // Usamos el heurístico actualizado para predecir valor futuro
                            val nextQ = currentQTable[nextKey] ?: getHeuristicQValues(stateAfterHuman)
                            maxFutureQ = nextQ.maxOrNull() ?: 0.0
                        }
                    }
                }

                val currentQValues = (currentQTable[stateKey] ?: getHeuristicQValues(state)).toMutableList()
                if (actionIndex < currentQValues.size) {
                    val oldQ = currentQValues[actionIndex]
                    val newQ = oldQ + LEARNING_RATE_ALPHA * (reward + DISCOUNT_FACTOR_GAMMA * maxFutureQ - oldQ)
                    currentQValues[actionIndex] = newQ
                    currentQTable[stateKey] = currentQValues
                    learningEvents++
                }
            }
            if (learningEvents > 0) {
                aiMemoryDataStoreManager.saveQTable(currentQTable)
                qTable = currentQTable
            }
        }
    }

    override suspend fun clearMemory() {
        mutex.withLock {
            qTable = emptyMap()
            aiMemoryDataStoreManager.clearQTable()
        }
    }

    override suspend fun getDailyMood(): Mood { return moodDataStoreManager.getMood() }
    override suspend fun saveDailyMood(mood: Mood) { moodDataStoreManager.saveMoodId(mood.id) }

    // Helpers necesarios
    private suspend fun MoodDataStoreManager.getMood(): Mood {
        val id = this.getMoodId()
        return Mood.ALL_MOODS_CLASSIC.find { it.id == id }
            ?: Mood.ALL_MOODS_GOMOKU.find { it.id == id }
            ?: Mood.NORMAL
    }
    private fun findDiffIndex(board1: Board, board2: Board): Int {
        val c1 = board1.cells; val c2 = board2.cells
        if (c1.size != c2.size) return -1
        for (i in c1.indices) if (c1[i] != c2[i]) return i
        return -1
    }
}