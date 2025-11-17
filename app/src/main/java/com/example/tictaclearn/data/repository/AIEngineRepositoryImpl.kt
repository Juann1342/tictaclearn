package com.example.tictaclearn.data.repository

import com.example.tictaclearn.data.datastore.AiMemoryDataStoreManager
import com.example.tictaclearn.data.datastore.MoodDataStoreManager
import com.example.tictaclearn.data.datastore.QTable
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.repository.AIEngineRepository
import com.example.tictaclearn.domain.model.checkGameResult // Importamos la función de extensión
import com.example.tictaclearn.domain.model.Reward // Importamos el objeto Reward
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    // Mutex para asegurar que la Q-Table no sea leída y escrita al mismo tiempo
    private val mutex = Mutex()

    // --- Q-LEARNING CONFIGURACIÓN ---
    private val random = Random.Default
    private var qTable: QTable = emptyMap() // Cache local para la Q-Table

    private companion object {
        // Constantes del algoritmo
        const val LEARNING_RATE_ALPHA = 0.1   // Tasa de Aprendizaje (Alpha)
        const val DISCOUNT_FACTOR_GAMMA = 0.9 // Factor de Descuento (Gamma)
        // Usamos el objeto Reward para las recompensas
        const val BOARD_SIZE = 9 // El número total de celdas (acciones posibles)
    }

    // --- HELPER FUNCTIONS ---

    /**
     * Convierte el tablero en una cadena para la Q-Table usando el método de Board.
     */
    private fun boardToState(board: Board): String {
        // Llama a la función definida en GameState.kt
        return board.toStateString()
    }

    /**
     * Encuentra el índice (0-8) del último movimiento de la IA.
     * La IA siempre es 'O' (Player.AI) según GameState.kt.
     * @return El índice plano (Int) del movimiento de la IA.
     */
    private fun findLastAIMove(previousBoard: Board, currentBoard: Board): Int? {
        val prevCells = previousBoard.cells
        val currCells = currentBoard.cells
        val aiSymbol = Player.AI.symbol // 'O'

        // Iteramos sobre el índice plano (0-8)
        for (i in 0 until BOARD_SIZE) {
            // La IA se mueve si la celda estaba vacía y ahora tiene su símbolo
            if (prevCells[i] == ' ' && currCells[i] == aiSymbol) {
                return i // Retorna el índice plano (la "acción")
            }
        }
        return null
    }

    // --- IMPLEMENTACIÓN DE PERSISTENCIA DEL MOOD ---

    override suspend fun getDailyMood(): Mood {
        // Se asume que el método DataStore Manager devuelve el ID del Mood.
        val moodId = moodDataStoreManager.getMoodId()
        // Usamos la función de ayuda en Mood.kt para encontrar el objeto Mood
        return Mood.fromId(moodId) ?: Mood.getDefaultDailyMood()
    }

    override suspend fun saveDailyMood(mood: Mood) {
        moodDataStoreManager.saveMoodId(mood.id)
    }

    // --- IMPLEMENTACIÓN DEL APRENDIZAJE DE LA IA ---

    /**
     * 1. Decisión de Movimiento (Epsilon-Greedy)
     */
    override suspend fun getNextMove(board: Board, currentMood: Mood): Int? {
        // Cargar Q-Table si es necesario (la primera vez)
        mutex.withLock {
            if (qTable.isEmpty()) {
                qTable = aiMemoryDataStoreManager.getQTable()
            }
        }

        val state = boardToState(board)
        // Usamos la función de Board para obtener los índices planos disponibles (acciones)
        val emptyActions = board.getAvailablePositions()
        if (emptyActions.isEmpty()) return null

        // Lógica Epsilon-Greedy: El Mood (epsilon) decide si explora o explota
        val epsilon = currentMood.epsilon

        if (random.nextDouble() < epsilon) {
            // **EXPLORACIÓN:** Movimiento aleatorio (índice plano)
            return emptyActions.randomOrNull()
        } else {
            // **EXPLOTACIÓN:** Elige el mejor movimiento de la Q-Table
            val qValues = qTable[state] ?: List(BOARD_SIZE) { 0.0 } // La lista de Q-values tiene tamaño 9

            val bestAction = emptyActions
                .maxByOrNull { actionIndex -> // actionIndex es el índice plano (0-8)
                    qValues[actionIndex]
                }

            // Si el mejor movimiento según la tabla no es nulo, lo retorna.
            return bestAction ?: emptyActions.randomOrNull()
        }
    }

    /**
     * 2. Aprendizaje y Actualización de Memoria (Q-Learning)
     * ¡Este es el método que faltaba por descomentar!
     */
    override suspend fun updateMemory(gameHistory: List<Board>) {
        // Bloqueamos con Mutex para asegurar que la Q-Table no se modifique
        // mientras se calcula o se guarda.
        mutex.withLock {
            // Se necesita al menos un estado inicial y un movimiento para aprender
            if (gameHistory.size < 2) return

            // Obtener la Q-Table actual (de la caché) y crear una copia mutable
            val currentQTable = qTable.toMutableMap()
            val finalBoard = gameHistory.last()

            // ✅ USAMOS LA FUNCIÓN DE EXTENSIÓN:
            val gameResult = finalBoard.checkGameResult()

            // 💡 1. Determinar la recompensa final (R) usando el objeto Reward
            val finalReward = Reward.getReward(gameResult, Player.AI)

            // 💡 2. Iterar el historial de movimientos de la IA de forma inversa
            for (i in gameHistory.size - 2 downTo 0) {
                val state = boardToState(gameHistory[i])
                val nextState = boardToState(gameHistory[i + 1])

                // Solo actualizamos si el movimiento que llevó de 'state' a 'nextState' fue de la IA.
                // actionIndex es el índice plano (0-8)
                val actionIndex = findLastAIMove(gameHistory[i], gameHistory[i + 1]) ?: continue

                // 3. Obtener Q-Value actual
                val stateQValues = currentQTable[state]?.toMutableList() ?: MutableList(BOARD_SIZE) { 0.0 }
                val currentQValue = stateQValues[actionIndex]

                // 4. Calcular el Valor Máximo Futuro (max Q(s', a'))
                // Si el estado siguiente (nextState) es terminal (Win o Draw),
                // el valor futuro es 0.0 porque no hay más movimientos.
                val maxFutureQ = if (gameHistory[i + 1].checkGameResult() != GameResult.Playing) {
                    0.0
                } else {
                    val nextStateQValues = currentQTable[nextState] ?: List(BOARD_SIZE) { 0.0 }
                    nextStateQValues.maxOrNull() ?: 0.0
                }

                // 5. Determinar la recompensa (solo se aplica la final al penúltimo estado)
                val reward = if (i == gameHistory.size - 2) finalReward else 0.0

                // 6. Aplicar la Fórmula Q-Learning
                // Q(s, a) <- Q(s, a) + α * [ R + γ * max Q(s', a') - Q(s, a) ]
                val newQValue = currentQValue + LEARNING_RATE_ALPHA * (reward + DISCOUNT_FACTOR_GAMMA * maxFutureQ - currentQValue)

                // 7. Actualizar la tabla en la copia local
                stateQValues[actionIndex] = newQValue
                currentQTable[state] = stateQValues
            }

            // 8. Persistir la Q-Table actualizada en DataStore
            aiMemoryDataStoreManager.saveQTable(currentQTable)
            // 9. Actualizamos la caché local
            qTable = currentQTable
        }
    }

    /**
     * 3. Gestión del Estado (Reseteo)
     */
    override suspend fun clearMemory() {
        mutex.withLock {
            qTable = emptyMap()
            aiMemoryDataStoreManager.clearQTable()
        }
        println("Memoria de la IA (Q-Table) borrada.")
    }
}