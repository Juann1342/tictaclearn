package com.example.tictaclearn.domain.ai

import android.util.Log
import com.example.tictaclearn.data.datastore.QTable
import com.example.tictaclearn.domain.model.Mood
import kotlin.random.Random

private const val TAG = "TicTacToeQAgent"

/**
 * Clase que implementa el algoritmo Q-Learning para jugar al TicTacToe.
 *
 * @property qTable La tabla de memoria (estado -> valores Q)
 * @property mood El estado de ánimo actual, que define el parámetro Epsilon.
 * @property learningRate (α) Tasa de aprendizaje: qué tan rápido debe ajustarse el valor Q.
 * @property discountFactor (γ) Factor de descuento: importancia de las recompensas futuras.
 */
class TicTacToeQAgent(
    private var qTable: QTable,
    private val mood: Mood,
    private val learningRate: Double = 0.1,
    private val discountFactor: Double = 0.9
) {
    // El parámetro epsilon que controla el balance entre exploración (movimiento aleatorio)
    // y explotación (movimiento basado en la memoria).
    private val epsilon: Double = mood.epsilon

    // --- Lógica de Movimiento ---

    /**
     * Elige el mejor movimiento basado en la política Epsilon-Greedy.
     * @param state El estado actual del tablero (como String, ej: "X_O_X_O__")
     * @param possibleActions La lista de índices de celdas vacías (ej: [1, 3, 7])
     * @return El índice de la celda elegida (acción).
     */
    fun selectAction(state: String, possibleActions: List<Int>): Int {
        if (possibleActions.isEmpty()) {
            return -1 // No hay movimientos posibles
        }

        if (Random.nextDouble() < epsilon) {
            // **Exploración**: Movimiento aleatorio (influenciado por el mood/epsilon)
            val action = possibleActions.random()
            Log.d(TAG, "Exploración (epsilon=${epsilon}): Elegida acción $action")
            return action
        } else {
            // **Explotación**: Movimiento basado en la mejor Q-Value
            val action = selectBestAction(state, possibleActions)
            Log.d(TAG, "Explotación: Elegida acción $action")
            return action
        }
    }

    /**
     * Encuentra la mejor acción (índice de celda) para el estado dado,
     * eligiendo la acción con el valor Q más alto.
     */
    private fun selectBestAction(state: String, possibleActions: List<Int>): Int {
        // Inicializa el mejor valor Q con el mínimo posible y el mejor índice como -1
        var maxQValue = Double.NEGATIVE_INFINITY
        var bestAction = -1

        // Asegura que el estado esté en la QTable, si no, lo inicializa
        val qValues = qTable[state] ?: initializeState(state)

        for (action in possibleActions) {
            // La acción (índice de la celda) se mapea directamente al índice en la lista de qValues
            val qValue = qValues[action]

            if (qValue > maxQValue) {
                maxQValue = qValue
                bestAction = action
            }
        }

        // Si no se encuentra un mejor movimiento (por ejemplo, todos son 0.0), se elige el primero
        if (bestAction == -1) {
            bestAction = possibleActions.first()
        }

        return bestAction
    }

    // --- Lógica de Aprendizaje (Actualización de Q-Table) ---

    /**
     * Actualiza la Q-Table usando la fórmula de Bellman (la esencia del Q-Learning).
     * Q(s, a) = Q(s, a) + α [r + γ * max(Q(s', a')) - Q(s, a)]
     *
     * @param prevState El estado del tablero antes del movimiento.
     * @param action El índice de la celda elegida (acción).
     * @param reward La recompensa recibida (r).
     * @param newState El estado del tablero después del movimiento (s').
     */
    fun updateQValue(prevState: String, action: Int, reward: Double, newState: String) {
        // 1. Obtener Q(s, a) actual
        val oldQValues = qTable[prevState] ?: initializeState(prevState)
        val oldQValue = oldQValues[action]

        // 2. Calcular max(Q(s', a')) (El mejor valor Q futuro)
        val maxFutureQ = getMaxQForState(newState) // Usamos la versión pública

        // 3. Aplicar la fórmula Q-Learning
        val newQValue = oldQValue + learningRate * (reward + discountFactor * maxFutureQ - oldQValue)

        // 4. Actualizar la Q-Table
        val updatedQValues = oldQValues.toMutableList()
        updatedQValues[action] = newQValue
        qTable = qTable.toMutableMap().apply {
            this[prevState] = updatedQValues
        }

        Log.d(TAG, "Aprendizaje en estado $prevState, acción $action. QViejo=$oldQValue, QNuevo=$newQValue")
    }

    /**
     * Calcula el máximo valor Q para un estado dado (max(Q(s', a'))).
     * 💡 CAMBIO: Hacemos esta función pública para que TicTacToeGameService la use en saveAiMemory.
     */
    fun getMaxQForState(state: String): Double {
        // Si el estado no está en la tabla, lo inicializamos y el max Q es 0.0
        val qValues = qTable[state] ?: initializeState(state)
        return qValues.maxOrNull() ?: 0.0
    }

    /**
     * Inicializa los valores Q para un nuevo estado (tablero).
     * @param state El estado del tablero.
     * @return Una lista de 9 Doubles (0.0), uno por cada celda posible.
     */
    private fun initializeState(state: String): List<Double> {
        val newQValues = List(9) { 0.0 }
        qTable = qTable.toMutableMap().apply {
            this[state] = newQValues
        }
        Log.d(TAG, "Inicializado nuevo estado: $state")
        return newQValues
    }

    // --- Getter de la Q-Table ---

    /**
     * Devuelve la Q-Table actual, incluyendo todas las actualizaciones de aprendizaje.
     */
    fun getCurrentQTable(): QTable = qTable
}