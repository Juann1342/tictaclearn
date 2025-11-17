package com.example.tictaclearn.data.repository

import com.example.tictaclearn.data.datastore.AiMemoryDataStoreManager
import com.example.tictaclearn.data.datastore.MoodDataStoreManager
import com.example.tictaclearn.data.datastore.QTable // Importación resuelta
import com.example.tictaclearn.domain.model.Cell
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.repository.AIEngineRepository
import com.example.tictaclearn.domain.model.Board // Importación resuelta
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AIEngineRepositoryImpl @Inject constructor(
    // 💡 DataStore para el Mood (Configuración)
    private val moodDataStoreManager: MoodDataStoreManager,
    // 💡 DataStore para la Q-Table (Memoria de la IA)
    private val aiMemoryDataStoreManager: AiMemoryDataStoreManager
) : AIEngineRepository {

    // --- Q-LEARNING CONFIGURACIÓN ---
    private val random = Random.Default
    private var qTable: QTable = emptyMap() // Cache local para la Q-Table

    private companion object {
        // Constantes del algoritmo
        const val LEARNING_RATE_ALPHA = 0.5   // Tasa de Aprendizaje (Alpha)
        const val DISCOUNT_FACTOR_GAMMA = 0.9 // Factor de Descuento (Gamma)
        const val REWARD_AI_WIN = 1.0
        const val REWARD_HUMAN_WIN = -1.0
        const val REWARD_DRAW = 0.1
    }

    // --- HELPER FUNCTIONS ---

    private fun findEmptyCells(board: Board): List<Pair<Int, Int>> {
        return board.cells.mapIndexedNotNull { row, rowCells ->
            rowCells.mapIndexedNotNull { col, cell ->
                if (cell == Cell.EMPTY) Pair(row, col) else null
            }
        }.flatten()
    }

    private fun boardToState(board: Board): String {
        return board.cells.flatten().joinToString(separator = "") {
            when (it) {
                Cell.X -> "X"
                Cell.O -> "O"
                Cell.EMPTY -> "."
            }
        }
    }

    private fun findLastAIMove(previousBoard: Board, currentBoard: Board): Pair<Int, Int>? {
        for (row in 0 until Board.Companion.SIZE) {
            for (col in 0 until Board.Companion.SIZE) {
                // La IA siempre es 'X' en el modelo
                if (previousBoard.cells[row][col] == Cell.EMPTY && currentBoard.cells[row][col] == Cell.X) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    // 💡 ELIMINAMOS la función 'checkGameStatus' duplicada, ya que está en la clase Board.

    // --- IMPLEMENTACIÓN DE PERSISTENCIA DEL MOOD ---

    override suspend fun getDailyMood(): Mood {
        val moodId = moodDataStoreManager.getMoodId()
        return Mood.ALL_MOODS.find { it.id == moodId } ?: Mood.getDefaultDailyMood()
    }

    override suspend fun saveDailyMood(mood: Mood) {
        moodDataStoreManager.saveMoodId(mood.id)
    }

    // --- IMPLEMENTACIÓN DEL APRENDIZAJE DE LA IA ---

    /**
     * 1. Decisión de Movimiento (Epsilon-Greedy)
     */
    override suspend fun getNextMove(board: Board, currentMood: Mood): Pair<Int, Int>? {
        // Cargar Q-Table si es necesario (la primera vez)
        if (qTable.isEmpty()) { qTable = aiMemoryDataStoreManager.getQTable() }

        val state = boardToState(board)
        val emptyCells = findEmptyCells(board)
        if (emptyCells.isEmpty()) return null

        // Lógica Epsilon-Greedy: El Mood (epsilon) decide si explora o explota
        val epsilon = currentMood.epsilon

        if (random.nextDouble() < epsilon) {
            // **EXPLORACIÓN:** Movimiento aleatorio
            return emptyCells.randomOrNull()
        } else {
            // **EXPLOTACIÓN:** Elige el mejor movimiento de la Q-Table
            val qValues = qTable[state] ?: List(Board.SIZE * Board.SIZE) { 0.0 } // 0.0 si el estado es nuevo

            val bestMove = emptyCells
                .maxByOrNull { (row, col) ->
                    val index = row * Board.SIZE + col
                    qValues[index]
                }

            // Si el mejor movimiento según la tabla está ocupado, usamos un respaldo aleatorio.
            return bestMove ?: emptyCells.randomOrNull()
        }
    }

    /**
     * 2. Aprendizaje y Actualización de Memoria (Q-Learning)
     */
    override suspend fun updateMemory(gameHistory: List<Board>) {
        if (gameHistory.size < 3) return

        // Obtener la Q-Table actual y crear una copia mutable para las actualizaciones
        val currentQTable = aiMemoryDataStoreManager.getQTable().toMutableMap()
        val finalBoard = gameHistory.last()

        // ✅ USAMOS EL MÉTODO DE BOARD:
        val gameResult = finalBoard.checkGameStatus()

        // 💡 1. Determinar la recompensa final (R)
        val finalReward = when (gameResult) {
            is GameResult.Win -> if (gameResult.winner == Player.AI) REWARD_AI_WIN else REWARD_HUMAN_WIN
            GameResult.Draw -> REWARD_DRAW
            GameResult.Playing -> 0.0
        }

        // 💡 2. Iterar el historial de movimientos de la IA (X) de forma inversa
        for (i in gameHistory.size - 2 downTo 0) {
            val state = boardToState(gameHistory[i])
            val nextState = boardToState(gameHistory[i + 1])

            // Solo actualizamos si el movimiento que llevó de 'state' a 'nextState' fue de la IA (X).
            val (actionRow, actionCol) = findLastAIMove(gameHistory[i], gameHistory[i + 1]) ?: continue
            val actionIndex = actionRow * Board.SIZE + actionCol

            // 3. Obtener Q-Value actual
            val stateQValues = currentQTable[state]?.toMutableList() ?: MutableList(Board.SIZE * Board.SIZE) { 0.0 }
            val currentQValue = stateQValues[actionIndex]

            // 4. Calcular el Valor Máximo Futuro (max Q(s', a'))
            val nextStateQValues = currentQTable[nextState] ?: List(Board.SIZE * Board.SIZE) { 0.0 }

            val maxFutureQ = if (i == gameHistory.size - 2) 0.0 else nextStateQValues.maxOrNull() ?: 0.0

            // 5. Determinar la recompensa (solo se aplica la final al penúltimo estado)
            val reward = if (i == gameHistory.size - 2) finalReward else 0.0

            // 6. Aplicar la Fórmula Q-Learning
            // Q(s, a) <- Q(s, a) + α * [ R + γ * max Q(s', a') - Q(s, a) ]
            val newQValue = currentQValue + LEARNING_RATE_ALPHA * (reward + DISCOUNT_FACTOR_GAMMA * maxFutureQ - currentQValue)

            // 7. Actualizar la tabla en el caché local
            stateQValues[actionIndex] = newQValue
            currentQTable[state] = stateQValues
        }

        // 8. Persistir la Q-Table actualizada
        aiMemoryDataStoreManager.saveQTable(currentQTable)
        qTable = currentQTable // Actualizamos el caché
    }

    /**
     * 3. Gestión del Estado (Reseteo)
     */
    override suspend fun clearMemory() {
        qTable = emptyMap()
        aiMemoryDataStoreManager.clearQTable()
        println("Memoria de la IA (Q-Table) borrada.")
    }
}